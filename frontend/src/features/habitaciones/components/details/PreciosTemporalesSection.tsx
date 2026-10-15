import {
  CalendarRange,
  BadgeDollarSign,
  Plus,
  Trash2,
  Loader2,
  CalendarDays,
  Users,
  Clock3,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { usePreciosTemporales } from "../../hooks/usePreciosTemporales";
import { useDeletePrecioTemporal } from "../../hooks/useDeletePrecioTemporal";
import { formatCurrency } from "@/utils/format";
import { formatDateShort } from "@/utils/date";
import { useState } from "react";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";

interface Props {
  tipoId: number;
  onAdd: () => void;
}

function getEstado(fechaInicio?: string, fechaFin?: string) {
  if (!fechaInicio || !fechaFin) return null;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const inicio = new Date(fechaInicio + "T12:00:00");
  const fin = new Date(fechaFin + "T12:00:00");
  if (fin < today)
    return {
      label: "Vencido",
      className:
        "bg-zinc-100 text-zinc-500 dark:bg-zinc-800 dark:text-zinc-400",
    };
  if (inicio <= today && fin >= today)
    return {
      label: "Vigente",
      className:
        "bg-emerald-500/10 text-emerald-600 border-emerald-500/20 dark:bg-emerald-500/15 dark:text-emerald-400",
    };
  return {
    label: "Próximo",
    className:
      "bg-amber-500/10 text-amber-600 border-amber-500/20 dark:bg-amber-500/15 dark:text-amber-400",
  };
}

export default function PreciosTemporalesSection({ tipoId, onAdd }: Props) {
  const { data: precios, isLoading } = usePreciosTemporales(tipoId);
  const deleteMutation = useDeletePrecioTemporal();
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const sorted = [...(precios ?? [])].sort((a, b) => {
    const da = a.fechaInicio ?? "";
    const db = b.fechaInicio ?? "";
    return da.localeCompare(db);
  });

  return (
    <>
      <section className="rounded-2xl border border-border/50 bg-card shadow-sm border-l-4 border-l-violet-500 overflow-hidden">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 sm:p-6 pb-3">
          <h3 className="flex items-center gap-2 text-base sm:text-lg font-semibold">
            <span className="flex items-center justify-center size-8 rounded-xl bg-violet-500/10 border border-violet-500/15">
              <CalendarRange className="size-4 text-violet-600" />
            </span>
            Precios temporales
            {!isLoading && sorted.length > 0 && (
              <span className="ml-1 inline-flex items-center rounded-full bg-violet-500/10 border border-violet-500/15 px-2.5 py-0.5 text-xs font-medium text-violet-600">
                {sorted.length}
              </span>
            )}
          </h3>
          <Button
            variant="outline"
            size="sm"
            onClick={onAdd}
            className="border-violet-500/40 text-violet-400 hover:bg-violet-500/10 hover:text-violet-300">
            <BadgeDollarSign data-icon="inline-start" />
            <span className="hidden sm:inline">Nuevo precio temporal</span>
          </Button>
        </div>

        <div className="px-4 sm:px-6 pb-4 sm:pb-6">
          {isLoading ? (
            <div className="flex items-center justify-center py-10">
              <Loader2 className="size-5 animate-spin text-muted-foreground" />
            </div>
          ) : sorted.length === 0 ? (
            <div className="flex flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-violet-200 bg-violet-50/30 dark:bg-violet-500/5 dark:border-violet-900/30 px-6 py-10 text-center">
              <div className="flex items-center justify-center size-12 rounded-2xl bg-white border border-violet-100 shadow-sm dark:bg-zinc-900 dark:border-violet-900/20">
                <BadgeDollarSign className="size-6 text-violet-500" />
              </div>
              <div className="space-y-1 max-w-sm">
                <p className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                  Sin precios temporales
                </p>
                <p className="text-sm text-muted-foreground leading-relaxed">
                  Crea un precio especial para temporadas altas, festivos o
                  promociones. Se aplicará automáticamente en el rango de
                  fechas.
                </p>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={onAdd}
                className="mt-2 border-violet-200 text-violet-600 hover:bg-violet-50 hover:text-violet-700 dark:border-violet-900/30 dark:text-violet-400">
                <Plus data-icon="inline-start" className="size-4" />
                Crear primer precio
              </Button>
            </div>
          ) : (
            <ul className="grid grid-cols-1 lg:grid-cols-2 gap-3">
              {sorted.map((p) => {
                const estado = getEstado(p.fechaInicio, p.fechaFin);
                const inicio = formatDateShort(p.fechaInicio);
                const fin = formatDateShort(p.fechaFin);
                return (
                  <li
                    key={p.id}
                    className="group relative flex flex-col gap-3 rounded-2xl border border-zinc-200/70 bg-white p-4 shadow-sm transition hover:shadow-md hover:border-violet-200 dark:bg-zinc-900 dark:border-zinc-800 dark:hover:border-violet-900/40">
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-center gap-2 text-sm font-medium text-zinc-900 dark:text-zinc-100 min-w-0">
                        <span className="flex items-center justify-center size-7 rounded-lg bg-violet-500/10 border border-violet-500/15 shrink-0">
                          <CalendarDays className="size-3.5 text-violet-600" />
                        </span>
                        <span className="truncate">
                          {inicio}{" "}
                          <span className="text-muted-foreground mx-1">→</span>{" "}
                          {fin}
                        </span>
                      </div>
                      {estado && (
                        <span
                          className={`inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-[11px] font-semibold tracking-wide shrink-0 ${estado.className}`}>
                          <Clock3 className="size-3" />
                          {estado.label}
                        </span>
                      )}
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div className="rounded-xl bg-zinc-50 border border-zinc-100 px-3 py-2.5 dark:bg-zinc-800/50 dark:border-zinc-800">
                        <p className="text-[11px] font-medium tracking-wide text-muted-foreground uppercase flex items-center gap-1">
                          <BadgeDollarSign className="size-3 text-violet-500" />
                          Por noche
                        </p>
                        <p className="text-sm font-bold text-violet-700 dark:text-violet-300 mt-0.5">
                          {p.precioNoche != null
                            ? `${formatCurrency(p.precioNoche)} COP`
                            : "—"}
                        </p>
                      </div>
                      <div className="rounded-xl bg-zinc-50 border border-zinc-100 px-3 py-2.5 dark:bg-zinc-800/50 dark:border-zinc-800">
                        <p className="text-[11px] font-medium tracking-wide text-muted-foreground uppercase flex items-center gap-1">
                          <Users className="size-3 text-violet-500" />
                          Persona extra
                        </p>
                        <p className="text-sm font-semibold text-zinc-800 dark:text-zinc-100 mt-0.5">
                          {p.precioPersonaExtra != null
                            ? `${formatCurrency(p.precioPersonaExtra)} COP`
                            : "—"}
                        </p>
                      </div>
                    </div>

                    <div className="flex justify-end -mb-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => p.id != null && setDeleteId(p.id)}
                        disabled={deleteMutation.isPending}
                        className="h-7 text-xs text-muted-foreground hover:text-destructive hover:bg-destructive/10 gap-1.5">
                        <Trash2 className="size-3.5" />
                        Eliminar
                      </Button>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </section>

      <ConfirmDeleteDialog
        open={deleteId != null}
        onClose={() => setDeleteId(null)}
        onConfirm={() => {
          if (deleteId != null) {
            deleteMutation.mutate(deleteId, {
              onSuccess: () => setDeleteId(null),
            });
          }
        }}
        isPending={deleteMutation.isPending}
        title="Eliminar precio temporal"
        description="¿Estás seguro de eliminar este precio temporal? Esta acción no se puede deshacer."
      />
    </>
  );
}
