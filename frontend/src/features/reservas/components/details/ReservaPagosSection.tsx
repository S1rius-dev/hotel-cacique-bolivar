import { useState } from "react";
import { Plus, History, ArrowLeftRight, Coins } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  FORMAT_CURRENCY,
  PagoStatusBadge,
  PagoMetodoIcon,
  HistorialPagosModal,
  ReembolsarPagoModal,
  DetallePagoModal,
} from "@/features/pagos";
import { RegistrarPagoModal, AjusteCard } from "@/features/pagos";
import { useEliminarAjuste } from "../../hooks/useReservaDetalle";
import type { PagoResponse, AjusteResponse, PricingBreakdown } from "@/api/generated/openAPIDefinition.schemas";
import { formatDate } from "@/utils/date";

interface Props {
  reservaId: number;
  total: number;
  desglose?: PricingBreakdown;
  totalPagado?: number;
  saldoPendiente?: number;
  ajustes?: AjusteResponse[];
  pagos?: PagoResponse[];
}

function ReservaPagosSection({ reservaId, total, desglose, totalPagado: totalPagadoProp, saldoPendiente: saldoPendienteProp, pagos: pagosProp = [], ajustes = [] }: Props) {
  const [modalPagoOpen, setModalPagoOpen] = useState(false);
  const [modalHistorialOpen, setModalHistorialOpen] = useState(false);
  const [reembolsarPago, setReembolsarPago] = useState<PagoResponse | null>(null);
  const [detallePago, setDetallePago] = useState<PagoResponse | null>(null);

  const eliminarAjuste = useEliminarAjuste();

  const ajustesActivos = ajustes.filter(a => a.activo !== false);
  // Solo para etiquetar "(fijado)"; el importe total viene del backend.
  const lastTotal = [...ajustesActivos].reverse().find((a) => a.modoCalculo === "TOTAL");
  const subtotalReal = desglose?.subtotal ?? total;

  // `total` ya es el PricingBreakdown.totalFinal del backend: ajustes y consumos
  // de tienda van incluidos. `saldoPendiente` es total - totalPagado. No se
  // recalcula aqui para no divergir del cobro real.
  const totalPagado = totalPagadoProp ?? 0;
  const saldoPendiente = saldoPendienteProp ?? Math.max(0, total - totalPagado);
  const pct = total > 0 ? Math.min(100, Math.round((totalPagado / total) * 100)) : 0;
  const ultimosPagos = pagosProp.slice(-3);

  const handleEliminarAjuste = (ajuste: AjusteResponse) => {
    if (!ajuste.id) return;
    eliminarAjuste.mutate({ reservaId, ajusteId: ajuste.id });
  };

  return (
    <>
      <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-emerald-500">
        <div className="flex items-center justify-between mb-4">
          <h3 className="flex items-center gap-2 text-lg font-semibold">
            <Coins className="h-5 w-5 text-emerald-600" />
            Pagos
          </h3>
          <div className="flex items-center gap-1 sm:gap-2 flex-wrap justify-end">
            {pagosProp.length > 0 && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setModalHistorialOpen(true)}
                className="gap-1.5 text-xs"
              >
                <History className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Ver historial</span>
              </Button>
            )}
            {saldoPendiente > 0 ? (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setModalPagoOpen(true)}
                className="gap-1.5"
              >
                <Plus className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Pagar</span>
              </Button>
            ) : (
              <Button
                variant="secondary"
                size="sm"
                onClick={() => setModalPagoOpen(true)}
                className="gap-1.5"
              >
                <Plus className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Ajustar pago</span>
              </Button>
            )}
          </div>
        </div>

        <div className="space-y-4">
            {/* Resumen */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Total {lastTotal ? "(fijado)" : ""}</p>
                <p className="text-lg font-bold text-zinc-900 dark:text-zinc-100">
                  {FORMAT_CURRENCY.format(total)}
                </p>
                {lastTotal && (
                  <p className="text-[10px] text-emerald-600 dark:text-emerald-400">
                    Subtotal: {FORMAT_CURRENCY.format(subtotalReal)} · {lastTotal.concepto ?? "Total fijado"}
                  </p>
                )}
              </div>
              <div>
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Pagado</p>
                <p className="text-lg font-bold text-emerald-600">
                  {FORMAT_CURRENCY.format(totalPagado)}
                </p>
              </div>
              <div>
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Pendiente</p>
                <p className={`text-lg font-bold ${saldoPendiente > 0 ? "text-amber-600" : "text-zinc-400"}`}>
                  {saldoPendiente > 0 ? FORMAT_CURRENCY.format(saldoPendiente) : "—"}
                </p>
              </div>
            </div>

            {/* Progress bar */}
            {total > 0 && (
              <div className="w-full h-2.5 rounded-full bg-zinc-100 dark:bg-zinc-800 overflow-hidden">
                <div
                  className="h-full rounded-full bg-gradient-to-r from-emerald-500 to-emerald-400 transition-all duration-500"
                  style={{ width: `${pct}%` }}
                />
              </div>
            )}

            {/* Ajustes de facturación */}
            {ajustesActivos.length > 0 && (
              <div className="space-y-2">
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                  Ajustes de facturación
                </p>
                <div className="space-y-1.5">
                  {ajustesActivos.map((ajuste) => (
                    <AjusteCard
                      key={ajuste.id}
                      ajuste={ajuste}
                      onDelete={handleEliminarAjuste}
                    />
                  ))}
                </div>
              </div>
            )}

            {/* Últimos pagos */}
            {ultimosPagos.length > 0 && (
              <div className="space-y-2">
                <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
                  Últimos pagos
                </p>
                <div className="space-y-1.5">
                  {ultimosPagos.map((pago) => (
                    <div
                      key={pago.id}
                      onClick={() => setDetallePago(pago)}
                      className="flex items-center gap-3 px-3 py-2.5 rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 cursor-pointer hover:bg-zinc-100 dark:hover:bg-zinc-800/60 transition-colors"
                    >
                      <span className="text-muted-foreground shrink-0">
                        <PagoMetodoIcon metodo={pago.metodoPago} />
                      </span>
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
                            {FORMAT_CURRENCY.format(pago.monto ?? 0)}
                          </span>
                          <PagoStatusBadge estado={pago.estado} />
                          {pago.tipoPago && (
                            <span className="text-[10px] text-muted-foreground bg-zinc-200/60 dark:bg-zinc-800 px-1.5 py-0.5 rounded">
                              {pago.tipoPago === "TOTAL" ? "Completo" : pago.tipoPago === "PARCIAL" ? "Abono" : pago.tipoPago === "ANTICIPO" ? "Anticipo" : "Dep. garantía"}
                            </span>
                          )}
                        </div>
                        {pago.referenciaExterna && (
                          <p className="text-[11px] text-muted-foreground truncate">
                            Ref: {pago.referenciaExterna}
                          </p>
                        )}
                      </div>
                      <div className="flex items-center gap-2 shrink-0">
                        <span className="text-[11px] text-muted-foreground">
                          {pago.fechaPago
                            ? formatDate(pago.fechaPago)
                            : "—"}
                        </span>
                        {pago.estado === "COMPLETADO" && (
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              setReembolsarPago(pago);
                            }}
                            className="p-1 rounded hover:bg-red-50 dark:hover:bg-red-900/20 text-muted-foreground hover:text-red-600 transition-colors"
                            title="Reembolsar"
                          >
                            <ArrowLeftRight className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {pagosProp.length === 0 && (
              <p className="text-sm text-muted-foreground italic">
                Sin pagos registrados
              </p>
            )}
          </div>
      </section>

      <RegistrarPagoModal
        open={modalPagoOpen}
        reservaId={reservaId}
        total={total}
        totalPagado={totalPagado}
        saldoPendiente={saldoPendiente}
        ajustes={ajustes}
        onClose={() => setModalPagoOpen(false)}
      />

      <HistorialPagosModal
        open={modalHistorialOpen}
        reservaId={reservaId}
        onClose={() => setModalHistorialOpen(false)}
        onReembolsar={setReembolsarPago}
      />

      {reembolsarPago && (
        <ReembolsarPagoModal
          open
          pago={reembolsarPago}
          onClose={() => setReembolsarPago(null)}
        />
      )}

      {detallePago && (
        <DetallePagoModal
          open
          pago={detallePago}
          onClose={() => setDetallePago(null)}
          onReembolsar={setReembolsarPago}
        />
      )}
    </>
  );
}

export default ReservaPagosSection;
