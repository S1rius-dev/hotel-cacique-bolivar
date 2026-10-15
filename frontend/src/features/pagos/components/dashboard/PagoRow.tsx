import { useState, useMemo } from "react";
import { ChevronDown, Banknote } from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import { getMetodoPagoLabel } from "@/features/pagos/constants/pago.constants";
import PagoMetodoIcon from "@/features/pagos/components/PagoMetodoIcon";
import {
  esCanalOnline,
  gatewayLogo,
  nombreCanal,
} from "@/features/payments/constants/gateway";
import type { PagoDashboard } from "../../types/dashboard.types";
import { usePagoEventos } from "../../hooks/usePagosDashboard";
import PagoTimeline from "./PagoTimeline";
import { buildTimeline } from "../../utils/timeline";

function EstadoBadge({ estado }: { estado: PagoDashboard["estado"] }) {
  const map: Record<string, string> = {
    aprobada: "bg-[#10b981]/15 text-[#10b981] border-[#10b981]/30",
    pendiente: "bg-[#f59e0b]/15 text-[#f59e0b] border-[#f59e0b]/30",
    rechazada: "bg-[#ef4444]/15 text-[#ef4444] border-[#ef4444]/30",
  };
  const label: Record<string, string> = {
    aprobada: "Aprobada",
    pendiente: "Pendiente",
    rechazada: "Rechazada",
  };
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border ${map[estado]}`}>
      <span
        className={`size-1.5 rounded-full ${estado === "aprobada" ? "bg-[#10b981]" : estado === "pendiente" ? "bg-[#f59e0b]" : "bg-[#ef4444]"}`}
      />
      {label[estado]}
    </span>
  );
}

interface CanalBadgeProps {
  canal: PagoDashboard["canal"] | string;
  gateway: string | undefined;
  gatewayDisplayName: string;
}

function CanalBadge({ canal, gateway, gatewayDisplayName }: CanalBadgeProps) {
  return (
    <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium border bg-muted border-border text-muted-foreground">
      {nombreCanal(canal, gateway, gatewayDisplayName)}
    </span>
  );
}

function formatFecha(iso: string) {
  const d = new Date(iso);
  const fecha = d.toLocaleDateString("es-CO", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
  const hora = d.toLocaleTimeString("es-CO", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
  return `${fecha} · ${hora}`;
}

function formatWompiMethod(paymentMethod?: string): string {
  if (!paymentMethod) return "—";
  const normalized = paymentMethod.trim().toUpperCase();
  // Si es enum crudo (TARJETA_CREDITO etc), usar label centralizado
  const enumLabel = getMetodoPagoLabel(normalized);
  // getMetodoPagoLabel ya humaniza, pero para valores libres como "Tarjeta Visa *4242" devolvería Title case feo,
  // así que solo lo usamos si el valor normalizado coincide con una opción conocida
  const isEnum = ["EFECTIVO","TARJETA_CREDITO","TARJETA_DEBITO","TRANSFERENCIA","CHEQUE","NEQUI","PSE","OTRO"].includes(normalized);
  if (isEnum) return enumLabel;
  // Para valores libres tipo "PSE · Bancolombia" o "Tarjeta Visa *4242", devolver tal cual pero capitalizando primera letra si viene en mayúsculas crudas
  if (paymentMethod.includes("·") || paymentMethod.includes("*")) return paymentMethod;
  // Fallback: humanizar snake case
  return getMetodoPagoLabel(paymentMethod);
}

interface Props {
  pago: PagoDashboard;
  gateway?: string;
  gatewayDisplayName?: string;
}

export default function PagoRow({ pago, gateway, gatewayDisplayName = "Pasarela online" }: Props) {
  const [open, setOpen] = useState(false);
  const esOnline = esCanalOnline(pago.canal);
  const { data: eventosRemote } = usePagoEventos(open ? pago.pagoId : undefined, open);
  const pagoEventos = pago.eventos;
  // El listado no trae trazabilidad: llega por endpoint dedicado. Los mocks si
  // la traen en línea, asi que se preferen los remotos cuando existen.
  const eventosTimeline = eventosRemote?.length ? eventosRemote : pagoEventos;
  // Timeline derivado: garantiza que aprobada/rechazada se refleje aunque el backend solo tenga "Transacción creada"
  const eventos = useMemo(
    () => buildTimeline(pago, eventosTimeline, gatewayDisplayName),
    [pago, eventosTimeline, gatewayDisplayName],
  );

  return (
    <div className="rounded-xl border bg-card border-border overflow-hidden transition-colors hover:border-border/80">
      {/* Main row */}
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="w-full text-left flex items-center gap-3 sm:gap-4 p-4 cursor-pointer">
        {/* Icon canal */}
        <span
          className={`size-10 rounded-xl flex items-center justify-center shrink-0 border
          ${esOnline ? " text-[#3b82f6]" : " text-[#22c55e]"}`}>
          {esOnline ? (
            <img
              src={gatewayLogo(gateway)}
              alt=""
              className="size-6 object-contain"
            />
          ) : (
            <Banknote className="size-5" />
          )}
        </span>

        {/* Centro - cliente + habitacion + metodo */}
        <div className="flex-1 min-w-0 grid grid-cols-1 lg:grid-cols-[1.4fr_0.9fr] gap-1 lg:gap-4 items-center">
          <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <p className="text-sm font-semibold text-foreground truncate">
                {pago.clienteNombre}
              </p>
              <CanalBadge
                canal={pago.canal}
                gateway={gateway}
                gatewayDisplayName={gatewayDisplayName}
              />
            </div>
            <p className="text-xs text-muted-foreground truncate flex items-center gap-1.5">
              <span>{pago.habitacion} ·</span>
              {esOnline ? (
                <span className="inline-flex items-center gap-1">
                  <PagoMetodoIcon metodo={pago.paymentMethod} className="size-4" />
                  {formatWompiMethod(pago.paymentMethod)}
                </span>
              ) : (
                <span>Recibo {pago.numeroRecibo}</span>
              )}
            </p>
          </div>

          {/* Monto + ID + fecha - en mobile va debajo, en desktop a la derecha */}
          <div className="flex flex-col lg:items-end gap-0.5 min-w-0">
            <p className="text-sm font-bold text-foreground tabular-nums">
              {FORMAT_CURRENCY.format(pago.monto)}
            </p>
            <p className="text-[11px] font-mono text-muted-foreground truncate">
              {esOnline ? pago.wompiTransactionId : pago.numeroRecibo} ·{" "}
              {formatFecha(pago.createdAt)}
            </p>
          </div>
        </div>

        {/* Estado + chevron */}
        <div className="flex items-center gap-3 shrink-0">
          <EstadoBadge estado={pago.estado} />
          <ChevronDown
            className={`size-4 text-muted-foreground transition-transform ${open ? "rotate-180" : ""}`}
          />
        </div>
      </button>

      {/* Expandible timeline */}
      {open && (
        <div className="border-t border-border bg-muted/20 px-4 sm:px-6 py-4">
          <div className="grid grid-cols-1 lg:grid-cols-[1.2fr_1fr] gap-6">
            <div>
              <p className="text-xs font-medium tracking-widest uppercase text-muted-foreground mb-3">
                Trazabilidad
              </p>
              <PagoTimeline eventos={eventos} />
            </div>
            <div className="space-y-3">
              <div className="rounded-lg border border-border bg-card p-3 space-y-2">
                <p className="text-xs font-medium text-muted-foreground">
                  Detalle
                </p>
                <div className="text-xs space-y-1">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Reserva</span>
                    <span className="text-foreground font-mono">
                      #{pago.reservaId}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Canal</span>
                    <span className="text-foreground">
                      {esOnline
                        ? `${nombreCanal(pago.canal, gateway, gatewayDisplayName)} (online)`
                        : "Efectivo (caja)"}
                    </span>
                  </div>
                  {esOnline ? (
                    <>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">Método</span>
                        <span className="text-foreground inline-flex items-center gap-1.5">
                          <PagoMetodoIcon metodo={pago.paymentMethod} className="size-4" />
                          {formatWompiMethod(pago.paymentMethod)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">
                          Transacción
                        </span>
                        <span className="text-foreground font-mono text-[11px] truncate max-w-37.5">
                          {pago.wompiTransactionId}
                        </span>
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">
                          Recibido por
                        </span>
                        <span className="text-foreground">
                          {pago.recibidoPor}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">Recibo</span>
                        <span className="text-foreground font-mono">
                          {pago.numeroRecibo}
                        </span>
                      </div>
                    </>
                  )}
                </div>
              </div>
              {pago.estado === "rechazada" && pago.motivoRechazo && (
                <div className="rounded-lg border border-[#ef4444]/30 bg-[#ef4444]/10 p-3">
                  <p className="text-xs font-medium text-[#ef4444]">
                    Motivo rechazo
                  </p>
                  <p className="text-xs text-foreground mt-1">
                    {pago.motivoRechazo}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
