import { useState } from "react";
import { Plus, History, Wallet } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  usePagosDeReserva,
  FORMAT_CURRENCY,
  PagoStatusBadge,
  PagoMetodoIcon,
  HistorialPagosModal,
  DetallePagoModal,
} from "@/features/pagos";
import { RegistrarPagoModal } from "@/features/pagos";
import type { PagoResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDateDayMonth } from "@/utils/date";

interface Props {
  reservaId: number;
  total: number;
  totalPagado: number;
  saldoPendiente: number;
}

function RoomPagosCard({ reservaId, total, totalPagado, saldoPendiente }: Props) {
  const { data: pagos } = usePagosDeReserva(reservaId);

  const [pagoModalOpen, setPagoModalOpen] = useState(false);
  const [historialOpen, setHistorialOpen] = useState(false);
  const [detallePago, setDetallePago] = useState<PagoResponse | null>(null);
  const pct = total > 0 ? Math.min(100, Math.round((totalPagado / total) * 100)) : 0;
  const ultimosPagos = (pagos ?? []).slice(-3);

  return (
    <>
      <div className="rounded-lg border border-emerald-200 dark:border-emerald-800 bg-emerald-50/40 dark:bg-emerald-950/20 p-3 flex flex-col justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground flex items-center gap-1.5 mb-2">
            <Wallet className="w-3 h-3 text-emerald-600" />
            Pagos
          </p>

          <div className="flex items-center gap-3 text-xs flex-wrap">
            <span className="text-muted-foreground">Total</span>
            <span className="font-semibold">{FORMAT_CURRENCY.format(total)}</span>
            <span className="text-muted-foreground">|</span>
            <span className="text-muted-foreground">Pagado</span>
            <span className="font-semibold text-emerald-600">{FORMAT_CURRENCY.format(totalPagado)}</span>
            <span className="text-muted-foreground">|</span>
            <span className="text-muted-foreground">Pendiente</span>
            <span className={`font-semibold ${saldoPendiente > 0 ? "text-amber-600" : "text-emerald-600"}`}>
              {FORMAT_CURRENCY.format(saldoPendiente)}
            </span>
          </div>

          {total > 0 && (
            <div className="w-full h-1.5 rounded-full bg-zinc-200 dark:bg-zinc-700 overflow-hidden mt-2">
              <div
                className={`h-full rounded-full transition-all ${
                  pct >= 100
                    ? "bg-emerald-500"
                    : pct > 0
                      ? "bg-linear-to-r from-amber-400 to-emerald-500"
                      : "bg-zinc-300 dark:bg-zinc-600"
                }`}
                style={{ width: `${Math.max(pct, 4)}%` }}
              />
            </div>
          )}

          {ultimosPagos.length > 0 && (
            <div className="space-y-1 mt-2">
              {ultimosPagos.map((p) => (
                <div
                  key={p.id}
                  onClick={() => setDetallePago(p)}
                  className="flex items-center justify-between text-xs bg-white dark:bg-zinc-950/40 rounded-md px-2 py-1 border cursor-pointer hover:bg-zinc-100 dark:hover:bg-zinc-800/60 transition-colors">
                  <div className="flex items-center gap-1.5 min-w-0">
                    <PagoMetodoIcon metodo={p.metodoPago} className="w-3 h-3 shrink-0" />
                    <span className="font-medium truncate">{FORMAT_CURRENCY.format(p.monto ?? 0)}</span>
                    <PagoStatusBadge estado={p.estado} />
                  </div>
                  <span className="text-muted-foreground shrink-0 ml-1">
                    {p.fechaPago
                      ? formatDateDayMonth(p.fechaPago)
                      : "—"}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex flex-wrap items-center gap-2 mt-3">
          <Button
            variant={saldoPendiente > 0 ? "default" : "secondary"}
            size="sm"
            onClick={() => setPagoModalOpen(true)}
            className="gap-1.5 text-xs"
          >
            <Plus className="w-3.5 h-3.5" />
            {saldoPendiente > 0 ? "Pagar" : "Ajustar"}
          </Button>
          {pagos && pagos.length > 0 && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setHistorialOpen(true)}
              className="gap-1.5 text-xs"
            >
              <History className="w-3.5 h-3.5" />
              Ver historial
            </Button>
          )}
        </div>
      </div>

      {pagoModalOpen && (
        <RegistrarPagoModal
          open={pagoModalOpen}
          reservaId={reservaId}
          total={total}
          totalPagado={totalPagado}
          saldoPendiente={saldoPendiente}
          onClose={() => setPagoModalOpen(false)}
        />
      )}

      {historialOpen && (
        <HistorialPagosModal
          open={historialOpen}
          reservaId={reservaId}
          onClose={() => setHistorialOpen(false)}
        />
      )}

      {detallePago && (
        <DetallePagoModal
          open
          pago={detallePago}
          onClose={() => setDetallePago(null)}
        />
      )}
    </>
  );
}

export default RoomPagosCard;
