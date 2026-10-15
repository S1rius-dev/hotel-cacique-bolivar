import { BedDouble, Loader2 } from "lucide-react";
import { Switch } from "@/components/ui/switch";
import { FORMAT_CURRENCY } from "@/features/pagos";
import type { DetalleReservaResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  detalles: DetalleReservaResponse[];
  /** Reserva en estado terminal: el backend rechaza el toggle con 400. */
  disabled?: boolean;
  pendingDetalleId?: number;
  onToggle: (detalleId: number, incluyeIva: boolean) => void;
}

function HabitacionesIvaList({
  detalles,
  disabled = false,
  pendingDetalleId,
  onToggle,
}: Props) {
  if (detalles.length === 0) return null;

  return (
    <div className="flex flex-col gap-3 rounded-lg bg-zinc-50/80 dark:bg-zinc-900/60 backdrop-blur-sm shadow-sm border border-zinc-200/60 dark:border-zinc-800/60 p-4">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground flex items-center gap-1.5">
          <BedDouble className="w-3.5 h-3.5 text-emerald-600" />
          Precio por habitación
        </p>
        <p className="text-[11px] text-muted-foreground/70 mt-0.5">
          Activa el IVA y los totales de la reserva se recalculan al instante.
        </p>
      </div>

      <div className="space-y-2 max-h-48 overflow-y-auto pr-1 thin-scrollbar">
        {detalles.map((detalle) => {
          const conIva = detalle.precioUsadoIncluyeIva ?? false;
          const importe = detalle.totalDetalle ?? detalle.subtotal ?? 0;
          const detalleId = detalle.id;
          const isPending = pendingDetalleId != null && detalleId === pendingDetalleId;

          return (
            <div
              key={detalleId ?? `${detalle.habitacionNumero}-${detalle.habitacionTipo}`}
              className="flex items-center justify-between gap-3 rounded-md border border-zinc-200/60 dark:border-zinc-800/60 bg-white/70 dark:bg-zinc-800/40 px-3 py-2.5">
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <p className="text-sm font-medium truncate">
                    Hab {detalle.habitacionNumero ?? "—"}
                  </p>
                  <span
                    className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${
                      conIva
                        ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-400"
                        : "bg-zinc-200 text-zinc-600 dark:bg-zinc-700 dark:text-zinc-300"
                    }`}>
                    {conIva ? "IVA incluido" : "Sin IVA"}
                  </span>
                  {isPending && (
                    <Loader2 className="w-3 h-3 animate-spin text-muted-foreground shrink-0" />
                  )}
                </div>
                <p className="text-[11px] text-muted-foreground truncate">
                  {detalle.habitacionTipo}
                  {detalle.totalDetalle != null &&
                    ` · Subtotal ${FORMAT_CURRENCY.format(detalle.subtotalDetalle ?? 0)}` +
                      ((detalle.ivaTotalDetalle ?? 0) > 0
                        ? ` · IVA ${FORMAT_CURRENCY.format(detalle.ivaTotalDetalle ?? 0)}`
                        : "")}
                </p>
              </div>

              <div className="flex items-center gap-3 shrink-0">
                <span className="text-sm font-semibold tabular-nums">
                  {FORMAT_CURRENCY.format(importe)}
                </span>
                <Switch
                  checked={conIva}
                  disabled={disabled || pendingDetalleId != null}
                  onCheckedChange={(checked) =>
                    detalleId != null && onToggle(detalleId, checked)
                  }
                  aria-label={`Cambiar modo de precio de la habitación ${detalle.habitacionNumero ?? ""}`}
                />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default HabitacionesIvaList;
