import { esCanalOnline } from "@/features/payments/constants/gateway";
import type { PagoDashboard, PagoEvento } from "../types/dashboard.types";

function isoAddMinutes(baseIso: string, minutes: number): string {
  const d = new Date(baseIso);
  d.setMinutes(d.getMinutes() + minutes);
  return d.toISOString();
}

function contains(eventos: PagoEvento[], substr: string): boolean {
  const s = substr.toLowerCase();
  return eventos.some((e) => String(e.descripcion).toLowerCase().includes(s));
}

/**
 * Construye timeline derivado para trazabilidad de la pasarela online.
 * Si el backend solo devuelve "Transacción creada", sintetiza pasos faltantes
 * según pago.estado / wompiStatusRaw para que el timeline refleje aprobado/rechazado.
 * Para canal efectivo no sintetiza pasos de pasarela.
 */
export function buildTimeline(
  pago: PagoDashboard,
  rawEventos: PagoEvento[] | null | undefined,
  gatewayDisplayName = "la pasarela online",
): PagoEvento[] {
  const eventos: PagoEvento[] = Array.isArray(rawEventos) ? [...rawEventos] : [];

  // Ordenar por timestamp ascendente
  eventos.sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

  const esOnline = esCanalOnline(pago.canal);
  const estado = String(pago.estado ?? "").toLowerCase(); // aprobada | pendiente | rechazada
  const raw = String(pago.wompiStatusRaw ?? "").toUpperCase(); // APPROVED | DECLINED etc

  const isAprobada = estado === "aprobada" || raw === "APPROVED";
  const isRechazada = estado === "rechazada" || raw === "REJECTED" || raw === "DECLINED" || raw === "ERROR";
  const isExpired = raw === "EXPIRED" || raw === "VOIDED";
  const isPendiente = !isAprobada && !isRechazada && !isExpired;

  // Canal efectivo: si no hay eventos, generar fallback mínimo
  if (!esOnline) {
    if (eventos.length === 0) {
      const base = pago.createdAt;
      if (isAprobada) {
        return [
          { id: "syn-1", descripcion: "Pago recibido en caja", timestamp: base },
          { id: "syn-2", descripcion: `Registrado por ${pago.recibidoPor ?? "caja"}`, timestamp: isoAddMinutes(base, 1) },
          { id: "syn-3", descripcion: `Recibo ${pago.numeroRecibo ?? ""} generado`, timestamp: isoAddMinutes(base, 2) },
        ];
      }
      if (eventos.length === 0) {
        return [
          { id: "syn-1", descripcion: "Pago pendiente", timestamp: base },
        ];
      }
    }
    return eventos;
  }

  // Pasarela online: timeline esperado 3 pasos
  const baseTime = eventos[0]?.timestamp ?? pago.createdAt;
  const methodSub = pago.paymentMethod ?? "";

  // 1. Transacción creada
  if (!contains(eventos, "transacción creada") && !contains(eventos, "transaccion creada")) {
    eventos.unshift({ id: "syn-created", descripcion: "Transacción creada", timestamp: baseTime });
  }

  // 2. Enviada a la pasarela
  // El match es genérico ("enviada a") para no duplicar el paso cuando el backend
  // ya lo devolvió con el nombre histórico de otra pasarela.
  if (!contains(eventos, "enviada a")) {
    // insertar como segundo
    const idx = eventos.findIndex((e) => e.descripcion.toLowerCase().includes("transacción creada")) ?? 0;
    eventos.splice(idx + 1, 0, {
      id: "syn-sent",
      descripcion: `Enviada a ${gatewayDisplayName}`,
      subtitulo: methodSub || undefined,
      timestamp: isoAddMinutes(baseTime, 1),
    });
  }

  // 3. Estado final según pago.estado / raw
  if (isAprobada) {
    if (!contains(eventos, "aprobada")) {
      eventos.push({
        id: "syn-approved",
        descripcion: "Aprobada por el banco",
        timestamp: isoAddMinutes(baseTime, 2),
      });
    }
  } else if (isRechazada) {
    if (!contains(eventos, "rechazada")) {
      const motivo = pago.motivoRechazo ? `: ${pago.motivoRechazo}` : "";
      eventos.push({
        id: "syn-declined",
        descripcion: `Rechazada${motivo}`,
        timestamp: isoAddMinutes(baseTime, 2),
      });
    }
  } else if (isExpired) {
    if (!contains(eventos, "expirada") && !contains(eventos, "expiró")) {
      eventos.push({
        id: "syn-expired",
        descripcion: "Expirada",
        timestamp: isoAddMinutes(baseTime, 2),
      });
    }
  } else if (isPendiente) {
    // pendiente: dejar 2 pasos (creada + enviada), no añadir aprobado
  }

  // Dedup por descripcion y re-ordenar
  const seen = new Set<string>();
  const dedup: PagoEvento[] = [];
  for (const ev of eventos) {
    const key = String(ev.descripcion).toLowerCase();
    if (seen.has(key)) continue;
    seen.add(key);
    dedup.push(ev);
  }
  dedup.sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());
  return dedup;
}
