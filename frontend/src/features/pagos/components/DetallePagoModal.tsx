import { X, ArrowLeftRight, ExternalLink, Receipt, Hash } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import PagoStatusBadge from "./PagoStatusBadge";
import PagoMetodoIcon from "./PagoMetodoIcon";
import DesglosePreview from "./DesglosePreview";
import { FORMAT_CURRENCY, getMetodoPagoLabel } from "../constants/pago.constants";
import type { PagoResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDateTimeWithAlas } from "@/utils/date";

interface Props {
  open: boolean;
  pago: PagoResponse;
  onClose: () => void;
  onReembolsar?: (pago: PagoResponse) => void;
}

const TIPO_PAGO_LABEL: Record<string, string> = {
  TOTAL: "Completo",
  PARCIAL: "Abono",
  ANTICIPO: "Anticipo",
  DEPOSITO_GARANTIA: "Dep. garantía",
};

function DetallePagoModal({ open, pago, onClose, onReembolsar }: Props) {
  const navigate = useNavigate();

  if (!open) return null;

  const metodoLabel = getMetodoPagoLabel(pago.metodoPago);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />

      <div className="relative w-full max-w-lg mx-4 max-h-[85vh] flex flex-col">
        <div className="bg-card rounded-2xl shadow-2xl border overflow-hidden flex flex-col max-h-full">
          {/* Header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100 dark:border-zinc-800 shrink-0">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-emerald-100 dark:bg-emerald-900/30">
                <Receipt className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
              </div>
              <div>
                <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                  Detalle del pago
                </h2>
                <p className="text-xs text-muted-foreground">
                  {pago.codigoPago ? `#${pago.codigoPago}` : `#${pago.id ?? "—"}`}
                </p>
              </div>
            </div>
            <button
              onClick={onClose}
              className="p-1 rounded-full hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors"
            >
              <X className="w-4 h-4 text-muted-foreground" />
            </button>
          </div>

          {/* Body */}
          <div className="p-5 overflow-y-auto space-y-4">
            {/* Monto principal */}
            <div className="text-center py-3">
              <p className="text-3xl font-bold text-zinc-900 dark:text-zinc-100">
                {FORMAT_CURRENCY.format(pago.monto ?? 0)}
              </p>
              <div className="flex items-center justify-center gap-2 mt-2 flex-wrap">
                <PagoStatusBadge estado={pago.estado} />
                {pago.tipoPago && (
                  <span className="text-xs font-medium text-muted-foreground bg-zinc-100 dark:bg-zinc-800 px-2 py-0.5 rounded">
                    {TIPO_PAGO_LABEL[pago.tipoPago] ?? pago.tipoPago}
                  </span>
                )}
              </div>
            </div>

            {/* Información del pago */}
            <div className="rounded-xl border bg-zinc-50 dark:bg-zinc-900/40 p-4 space-y-2.5">
              <h3 className="text-[11px] font-semibold uppercase tracking-widest text-muted-foreground/70">
                Información del pago
              </h3>
              <div className="space-y-2 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground flex items-center gap-2">
                    <PagoMetodoIcon metodo={pago.metodoPago} className="w-5 h-5" />
                    Método
                  </span>
                  <span className="font-medium text-zinc-800 dark:text-zinc-200">{metodoLabel}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground flex items-center gap-2">
                    <Hash className="w-3.5 h-3.5" />
                    Referencia
                  </span>
                  <span className="font-medium text-zinc-800 dark:text-zinc-200">
                    {pago.referenciaExterna ?? "—"}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-muted-foreground">Fecha</span>
                  <span className="font-medium text-zinc-800 dark:text-zinc-200">
                    {pago.fechaPago ? formatDateTimeWithAlas(pago.fechaPago) : "—"}
                  </span>
                </div>
                {pago.observaciones && (
                  <div className="border-t border-zinc-200 dark:border-zinc-700 pt-2 mt-2">
                    <p className="text-xs text-muted-foreground leading-relaxed">
                      {pago.observaciones}
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Reparto entre cajas. Solo lo tienen los pagos en efectivo. */}
            {pago.desglose && pago.desglose.length > 0 && (
              <DesglosePreview lineas={pago.desglose} titulo="Reparto entre cajas" />
            )}

            {/* Acceso a reserva */}
            {pago.reservaId && (
              <button
                type="button"
                onClick={() => {
                  onClose();
                  navigate(`/dashboard/reservas/${pago.reservaId}`);
                }}
                className="flex w-full items-center gap-3 rounded-xl border border-zinc-200/60 dark:border-zinc-800/60 bg-zinc-50/80 dark:bg-zinc-900/40 p-3 text-left transition-colors hover:bg-zinc-100 dark:hover:bg-zinc-800/60 group"
              >
                <div className="p-2 rounded-full bg-blue-100 dark:bg-blue-900/30 group-hover:bg-blue-200 dark:group-hover:bg-blue-900/50 transition-colors">
                  <ExternalLink className="w-4 h-4 text-blue-600 dark:text-blue-400" />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-zinc-800 dark:text-zinc-200">
                    Ver reserva
                  </p>
                  <p className="text-xs text-muted-foreground truncate mt-0.5">
                    {pago.codigoReserva
                      ? `#${pago.codigoReserva}`
                      : `#${pago.reservaId}`}
                  </p>
                </div>
                <ExternalLink className="w-4 h-4 text-muted-foreground shrink-0 opacity-0 group-hover:opacity-100 transition-opacity" />
              </button>
            )}
          </div>

          {/* Footer */}
          <div className="flex items-center justify-between px-5 py-4 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50 shrink-0">
            <Button variant="ghost" onClick={onClose} className="text-muted-foreground hover:text-foreground">
              Cerrar
            </Button>
            {pago.estado === "COMPLETADO" && onReembolsar && (
              <Button
                variant="destructive"
                size="sm"
                onClick={() => {
                  onReembolsar(pago);
                  onClose();
                }}
                className="gap-1.5"
              >
                <ArrowLeftRight className="w-3.5 h-3.5" />
                Reembolsar
              </Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default DetallePagoModal;