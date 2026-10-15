import { useState } from "react";
import { X, Loader2, ArrowLeftRight, CalendarDays } from "lucide-react";
import { Button } from "@/components/ui/button";
import { usePagosDeReserva } from "../hooks/usePagos";
import PagoStatusBadge from "./PagoStatusBadge";
import PagoMetodoIcon from "./PagoMetodoIcon";
import DetallePagoModal from "./DetallePagoModal";
import { FORMAT_CURRENCY, getMetodoPagoLabel } from "../constants/pago.constants";
import type { PagoResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDateTimeFull } from "@/utils/date";

interface Props {
  open: boolean;
  reservaId: number;
  onClose: () => void;
  onReembolsar?: (pago: PagoResponse) => void;
}

const TIPO_PAGO_LABEL: Record<string, string> = {
  TOTAL: "Completo",
  PARCIAL: "Abono",
  ANTICIPO: "Anticipo",
  DEPOSITO_GARANTIA: "Dep. garantía",
};

function HistorialPagosModal({ open, reservaId, onClose, onReembolsar }: Props) {
  const [detallePago, setDetallePago] = useState<PagoResponse | null>(null);
  const { data: pagos, isLoading } = usePagosDeReserva(reservaId);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm" onClick={onClose} />

      <div className="relative w-full max-w-lg mx-4 max-h-[85vh] flex flex-col">
        <div className="bg-card rounded-2xl shadow-2xl border overflow-hidden flex flex-col max-h-full">
          {/* Header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100 dark:border-zinc-800 shrink-0">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-emerald-100 dark:bg-emerald-900/30">
                <CalendarDays className="w-5 h-5 text-emerald-600 dark:text-emerald-400" />
              </div>
              <div>
                <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                  Historial de pagos
                </h2>
                <p className="text-xs text-muted-foreground">
                  {pagos?.length ?? 0} pago{(pagos?.length ?? 0) !== 1 ? "s" : ""} registrado{(pagos?.length ?? 0) !== 1 ? "s" : ""}
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
          <div className="p-5 overflow-y-auto">
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
              </div>
            ) : pagos && pagos.length > 0 ? (
              <div className="space-y-3">
                {pagos.map((pago, idx) => (
                  <div key={pago.id} className="relative pl-8 cursor-pointer" onClick={() => setDetallePago(pago)}>
                    {/* Timeline dot + line */}
                    <div className="absolute left-3 top-2 -translate-x-1/2 flex flex-col items-center">
                      <div className={`w-3 h-3 rounded-full border-2 ${
                        pago.estado === "COMPLETADO"
                          ? "bg-emerald-500 border-emerald-500"
                          : pago.estado === "REEMBOLSADO"
                            ? "bg-red-500 border-red-500"
                            : "bg-amber-500 border-amber-500"
                      }`} />
                      {idx < pagos.length - 1 && (
                        <div className="w-0.5 h-full min-h-[40px] bg-zinc-200 dark:bg-zinc-700" />
                      )}
                    </div>

                    {/* Card */}
                    <div className="rounded-xl border bg-white dark:bg-zinc-950/40 p-4 space-y-2.5">
                      <div className="flex items-start justify-between gap-3">
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="text-base font-bold text-zinc-900 dark:text-zinc-100">
                              {FORMAT_CURRENCY.format(pago.monto ?? 0)}
                            </span>
                            <PagoStatusBadge estado={pago.estado} />
                            {pago.tipoPago && (
                              <span className="text-[10px] font-medium text-muted-foreground bg-zinc-100 dark:bg-zinc-800 px-1.5 py-0.5 rounded">
                                {TIPO_PAGO_LABEL[pago.tipoPago] ?? pago.tipoPago}
                              </span>
                            )}
                          </div>

                          <div className="flex items-center gap-2 mt-1 text-xs text-muted-foreground">
                            <PagoMetodoIcon metodo={pago.metodoPago} className="w-5 h-5" />
                            <span>{getMetodoPagoLabel(pago.metodoPago)}</span>
                          </div>
                        </div>

                        {pago.estado === "COMPLETADO" && onReembolsar && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              onReembolsar(pago);
                              onClose();
                            }}
                            className="shrink-0 text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-900/20 text-xs h-7 px-2"
                          >
                            <ArrowLeftRight className="w-3 h-3 mr-1" />
                            Reembolsar
                          </Button>
                        )}
                      </div>

                      <div className="flex flex-wrap gap-x-4 gap-y-1 text-[11px] text-muted-foreground">
                        <span>#{pago.codigoPago ?? pago.id}</span>
                        {pago.referenciaExterna && <span>Ref: {pago.referenciaExterna}</span>}
                        {pago.fechaPago && (
                          <span>
                            {formatDateTimeFull(pago.fechaPago)}
                          </span>
                        )}
                      </div>

                      {pago.observaciones && (
                        <p className="text-[11px] text-muted-foreground italic border-t border-zinc-100 dark:border-zinc-800 pt-2 mt-1">
                          {pago.observaciones}
                        </p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-10 text-center">
                <div className="p-3 rounded-full bg-zinc-100 dark:bg-zinc-800 mb-3">
                  <CalendarDays className="w-6 h-6 text-muted-foreground" />
                </div>
                <p className="text-sm font-medium text-zinc-900 dark:text-zinc-100">
                  Sin pagos registrados
                </p>
                <p className="text-xs text-muted-foreground mt-1">
                  No hay pagos registrados para esta reserva.
                </p>
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="flex items-center justify-end px-5 py-4 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50 shrink-0">
            <Button variant="ghost" onClick={onClose} className="text-muted-foreground hover:text-foreground">
              Cerrar
            </Button>
          </div>
        </div>
      </div>

      {detallePago && (
        <DetallePagoModal
          open
          pago={detallePago}
          onClose={() => setDetallePago(null)}
          onReembolsar={onReembolsar}
        />
      )}
    </div>
  );
}

export default HistorialPagosModal;
