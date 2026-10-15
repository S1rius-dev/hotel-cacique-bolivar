import type {
  PagoDashboardResponse,
  PagoEventoResponse,
} from "@/api/generated/openAPIDefinition.schemas";
import type {
  Canal,
  EstadoPago,
  PagoDashboard,
  PagoEvento,
} from "../types/dashboard.types";

const ESTADOS: readonly EstadoPago[] = ["aprobada", "pendiente", "rechazada"];

function toEstado(value: string | undefined): EstadoPago {
  const normalizado = String(value ?? "").trim().toLowerCase() as EstadoPago;
  return ESTADOS.includes(normalizado) ? normalizado : "pendiente";
}

function toCanal(value: string | undefined): Canal {
  const normalizado = String(value ?? "").trim().toLowerCase();
  return (normalizado || "efectivo") as Canal;
}

/**
 * El DTO del backend declara todos sus campos como opcionales, mientras que
 * `PagoDashboard` los exige para que la vista pueda renderizar sin guards.
 * Este normalizador centraliza los defaults en un solo lugar.
 */
/** El DTO de eventos tiene todo opcional y el id numerico; la vista lo usa como string. */
export function toPagoEvento(dto: PagoEventoResponse): PagoEvento {
  return {
    id: dto.id != null ? String(dto.id) : `evt_${dto.timestamp ?? "sin-id"}`,
    descripcion: dto.descripcion ?? "",
    timestamp: dto.timestamp ?? new Date(0).toISOString(),
    subtitulo: dto.subtitulo,
  };
}

export function toPagoDashboard(dto: PagoDashboardResponse): PagoDashboard {
  const pagoId = dto.id != null ? Number(dto.id) : undefined;
  return {
    id: pagoId != null ? String(dto.id) : `pago_${dto.codigoReserva ?? "sin-id"}`,
    pagoId: Number.isFinite(pagoId) ? pagoId : undefined,
    reservaId: Number(dto.reservaId ?? 0),
    clienteNombre: dto.clienteNombre ?? "Cliente sin nombre",
    habitacion: dto.habitacion ?? "Habitación sin asignar",
    monto: Number(dto.monto ?? 0),
    canal: toCanal(dto.canal),
    estado: toEstado(dto.estado),
    createdAt: dto.createdAt ?? new Date(0).toISOString(),
    wompiTransactionId: dto.wompiTransactionId,
    paymentMethod: dto.paymentMethod,
    wompiStatusRaw: dto.wompiStatusRaw,
    motivoRechazo: dto.motivoRechazo,
    recibidoPor: dto.recibidoPor,
    numeroRecibo: dto.numeroRecibo,
    // El timeline llega por endpoint dedicado (usePagoEventos), no en el listado.
    eventos: [],
  };
}
