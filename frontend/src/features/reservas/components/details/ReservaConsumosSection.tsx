import { ShoppingBag, Trash2, Receipt } from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import { useConsumos, useAnularConsumo } from "@/features/tienda/hooks/useConsumos";
import { formatDate } from "@/utils/date";

interface Props {
  reservaId: number;
}

export default function ReservaConsumosSection({ reservaId }: Props) {
  const { data: consumos, isLoading } = useConsumos(reservaId);
  const anular = useAnularConsumo();

  const list = consumos ?? [];
  const total = list.reduce((acc, c) => acc + (c.montoTotal ?? 0), 0);

  if (isLoading) {
    return (
      <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm animate-pulse h-32" />
    );
  }

  return (
    <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-rose-500">
      <div className="flex items-center justify-between mb-4">
        <h3 className="flex items-center gap-2 text-lg font-semibold">
          <Receipt className="h-5 w-5 text-rose-600" />
          Consumos de tienda
        </h3>
        <span className="text-xs px-2 py-1 rounded-full bg-muted text-muted-foreground border">
          {list.length} {list.length === 1 ? "producto" : "productos"}
        </span>
      </div>

      {list.length === 0 ? (
        <p className="text-sm text-muted-foreground italic">Sin consumos — Los cargos de tienda aparecerán aquí como recibo.</p>
      ) : (
        <div className="space-y-2">
          <div className="rounded-lg border bg-muted/20 p-2 text-xs font-mono">
            <p className="font-semibold">Recibo · Tienda Hit</p>
            <p className="text-muted-foreground">Productos cargados a esta reserva</p>
          </div>
          {list.map((c) => (
            <div key={c.id} className="flex items-center gap-3 px-3 py-2.5 rounded-lg border bg-card hover:bg-muted/20">
              <span className="size-8 rounded-lg bg-rose-500/10 text-rose-600 flex items-center justify-center shrink-0 border border-rose-500/20">
                <ShoppingBag className="size-4" />
              </span>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">
                  {c.productoNombre} <span className="text-muted-foreground font-normal">×{c.cantidad}</span>
                  {c.detalleHabitacion && <span className="text-xs text-muted-foreground"> · Hab {c.detalleHabitacion}</span>}
                </p>
                <p className="text-xs text-muted-foreground truncate">
                  {c.skuBarcode ? `${c.skuBarcode} · ` : ""}{c.precioUnitario ? `${FORMAT_CURRENCY.format(c.precioUnitario)} c/u` : ""} {c.createdAt ? `· ${formatDate(c.createdAt)}` : ""}
                  {c.observaciones ? ` · ${c.observaciones}` : ""}
                </p>
              </div>
              <div className="text-right shrink-0">
                <p className="text-sm font-bold">{FORMAT_CURRENCY.format(c.montoTotal ?? 0)}</p>
                <button
                  onClick={() => c.id && anular.mutate({ reservaId, consumoId: c.id })}
                  className="text-[11px] text-red-500 hover:text-red-600 flex items-center gap-1 ml-auto">
                  <Trash2 className="size-3" /> Anular
                </button>
              </div>
            </div>
          ))}
          <div className="flex items-center justify-between pt-3 border-t border-border font-semibold">
            <span className="text-sm text-muted-foreground">Total consumos</span>
            <span className="text-base">{FORMAT_CURRENCY.format(total)}</span>
          </div>
          <p className="text-[11px] text-muted-foreground">Se suma automáticamente al total de la reserva y al saldo pendiente.</p>
        </div>
      )}
    </section>
  );
}
