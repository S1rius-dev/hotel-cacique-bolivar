import { differenceInCalendarDays, addDays, format } from "date-fns";
import { es } from "date-fns/locale";
import {
  Trash2,
  User,
  Plus,
  Minus,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { formatCurrency } from "@/utils/format";
import { DETALLE_ESTADO_STYLES } from "../constants/reserva.constants";
import type {
  DetalleReservaResponse,
  DetalleReservaUpdateRequest,
} from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  detalle: DetalleReservaResponse;
  detalleEdit?: DetalleReservaUpdateRequest;
  noches: number;
  isLast: boolean;
  puedeRemover: boolean;
  puedeGestionarHuespedes: boolean;
  onDetalleChange: (detalleId: number, field: string, value: unknown) => void;
  onRemoveDetalle: (detalleId: number, roomNumber: string) => void;
  onAddGuest: (detalle: DetalleReservaResponse) => void;
  onRemoveGuest: (
    detalleId: number,
    detalleHuespedId: number,
    guestName: string,
  ) => void;
  checkIn?: string;
  checkOut?: string;
}

function EditarHabitacionCard({
  detalle,
  detalleEdit,
  noches,
  isLast,
  puedeRemover,
  puedeGestionarHuespedes,
  onDetalleChange,
  onRemoveDetalle,
  onAddGuest,
  onRemoveGuest,
  checkIn,
  checkOut,
}: Props) {
  const badge = detalle.estado ? DETALLE_ESTADO_STYLES[detalle.estado] : null;

  const capacidadBase = detalle.capacidadBase ?? 0;
  const capacidadMaxima = detalle.capacidadMaxima ?? 99;
  const precioPersonaExtra = detalle.precioPersonaExtra ?? 0;
  const precioPorNoche = detalle.precioPorNoche ?? 0;
  const ocupantes =
    detalleEdit?.cantidadOcupantes ?? detalle.cantidadOcupantes ?? 1;
  const personasExtras = Math.max(0, ocupantes - capacidadBase);
  const subtotal =
    precioPorNoche * noches + personasExtras * precioPersonaExtra * noches;
  const useCustomNoches = (detalleEdit as Record<string, unknown>)?.useCustomNoches === true;
  const customNoches = ((detalleEdit as Record<string, unknown>)?.customNoches as number) ?? 0;
  const reservaNoches = checkIn && checkOut ? differenceInCalendarDays(new Date(checkOut), new Date(checkIn)) : 0;

  return (
    <div className="rounded-xl border border-border bg-card overflow-hidden">
      <div className="flex items-center justify-between px-4 py-3 bg-muted/50 border-b border-border">
        <div className="flex items-center gap-3 min-w-0">
          <div className="size-8 rounded-lg bg-rose-700/10 flex items-center justify-center shrink-0">
            <span className="text-sm font-mono font-bold text-rose-800 dark:text-rose-300">
              #{detalle.habitacionNumero ?? "—"}
            </span>
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              {badge && (
                <span
                  className="inline-flex items-center gap-1 text-[10px] px-2 py-0.5 rounded-full border font-medium">
                  {badge.label}
                </span>
              )}
              <span className="text-[11px] text-muted-foreground">
                {formatCurrency(precioPorNoche)}
                <span className="text-muted-foreground/60"> /noche</span>
              </span>
            </div>
            <p className="text-[10px] text-muted-foreground/70 mt-0.5">
              Base {capacidadBase} pers. &middot; Extra {formatCurrency(precioPersonaExtra)}/pers.
            </p>
          </div>
        </div>

        {puedeRemover && !isLast && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() =>
              onRemoveDetalle(detalle.id!, detalle.habitacionNumero ?? "—")
            }
            className="text-muted-foreground hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/20 h-8 w-8 p-0 shrink-0"
          >
            <Trash2 className="w-3.5 h-3.5" />
          </Button>
        )}
      </div>

      <div className="p-4 space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="space-y-1">
            <label className="text-[10px] uppercase tracking-wider text-muted-foreground font-medium">
              Ocupantes
            </label>
            <div className="flex items-center gap-1.5">
              <button
                type="button"
                onClick={() =>
                  onDetalleChange(
                    detalle.id!,
                    "cantidadOcupantes",
                    Math.max(1, ocupantes - 1),
                  )
                }
                disabled={ocupantes <= 1}
                className="flex items-center justify-center size-8 rounded-lg border border-border bg-card text-muted-foreground hover:bg-accent disabled:opacity-30 disabled:pointer-events-none transition-colors"
              >
                <Minus className="w-3.5 h-3.5" />
              </button>
              <div className="flex-1 flex items-center justify-center h-8 rounded-lg border border-border bg-muted/50 px-3 text-sm font-mono font-semibold text-foreground tabular-nums">
                {ocupantes}
              </div>
              <button
                type="button"
                onClick={() =>
                  onDetalleChange(
                    detalle.id!,
                    "cantidadOcupantes",
                    Math.min(capacidadMaxima, ocupantes + 1),
                  )
                }
                disabled={ocupantes >= capacidadMaxima}
                className="flex items-center justify-center size-8 rounded-lg border border-border bg-card text-muted-foreground hover:bg-accent disabled:opacity-30 disabled:pointer-events-none transition-colors"
              >
                <Plus className="w-3.5 h-3.5" />
              </button>
            </div>
            {personasExtras > 0 && (
              <p className="text-[10px] text-rose-600 dark:text-rose-400 font-medium">
                +{personasExtras} extra x {formatCurrency(precioPersonaExtra)}
              </p>
            )}
          </div>

          <div className="space-y-1">
            <label className="text-[10px] uppercase tracking-wider text-muted-foreground font-medium">
              Subtotal
            </label>
            <p className="h-8 flex items-center text-sm font-mono font-bold text-foreground tabular-nums">
              {formatCurrency(subtotal)}
            </p>
            <p className="text-[10px] text-muted-foreground/70 leading-tight">
              {formatCurrency(precioPorNoche)} x {noches} {noches === 1 ? "noche" : "noches"}
              {personasExtras > 0 && (
                <> + {personasExtras} extra</>
              )}
            </p>
          </div>
        </div>

        {checkIn && reservaNoches > 0 && (
          <div className="pt-3 border-t border-border/50">
            <label className="flex items-center gap-2 cursor-pointer group py-1">
              <input
                type="checkbox"
                checked={!useCustomNoches}
                onChange={() => onDetalleChange(detalle.id!, "useCustomNoches", !useCustomNoches)}
                className="rounded border-border text-rose-700 focus:ring-rose-700/30"
              />
              <span className="text-xs text-muted-foreground group-hover:text-foreground transition-colors">
                Todas las noches ({reservaNoches} {reservaNoches === 1 ? "noche" : "noches"})
              </span>
            </label>

            {useCustomNoches && (
              <div className="mt-3 space-y-2">
                <div className="flex items-center justify-between rounded-lg bg-muted/50 border border-border px-3 py-2">
                  <span className="text-xs text-muted-foreground">Noches de estad&iacute;a</span>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => onDetalleChange(detalle.id!, "customNoches", Math.max(1, customNoches - 1))}
                      disabled={customNoches <= 1}
                      className="flex items-center justify-center size-7 rounded-lg border border-border bg-card text-muted-foreground hover:bg-accent disabled:opacity-30 disabled:pointer-events-none transition-colors"
                    >
                      <Minus className="w-3 h-3" />
                    </button>
                    <span className="w-8 text-center text-sm font-mono font-semibold text-foreground tabular-nums">
                      {customNoches}
                    </span>
                    <button
                      type="button"
                      onClick={() => onDetalleChange(detalle.id!, "customNoches", Math.min(reservaNoches, customNoches + 1))}
                      disabled={customNoches >= reservaNoches}
                      className="flex items-center justify-center size-7 rounded-lg border border-border bg-card text-muted-foreground hover:bg-accent disabled:opacity-30 disabled:pointer-events-none transition-colors"
                    >
                      <Plus className="w-3 h-3" />
                    </button>
                  </div>
                </div>
                <p className="text-[11px] text-muted-foreground/70 text-center">
                  {format(new Date(checkIn), "d MMM", { locale: es })} &rarr;{" "}
                  {format(addDays(new Date(checkIn), customNoches), "d MMM", { locale: es })}{" "}
                  ({customNoches} {customNoches === 1 ? "noche" : "noches"})
                </p>
              </div>
            )}
          </div>
        )}

        <div className="pt-3 border-t border-border/50">
          <div className="flex items-center justify-between mb-3">
            <p className="text-[10px] uppercase tracking-wider text-muted-foreground font-medium">
              Hu&eacute;spedes ({(detalle.huespedes ?? []).length}/{detalleEdit?.cantidadOcupantes ?? "—"})
            </p>
            {puedeGestionarHuespedes && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => onAddGuest(detalle)}
                className="text-rose-800 border-rose-200 hover:bg-rose-50 dark:text-rose-400 dark:border-rose-800 dark:hover:bg-rose-900/20 text-[10px] h-7 px-2.5"
              >
                <Plus className="w-3 h-3 mr-1" />
                Agregar
              </Button>
            )}
          </div>

          {detalle.huespedes && detalle.huespedes.length > 0 ? (
            <div className="space-y-2">
              {detalle.huespedes.map((h) => (
                <div
                  key={h.detalleHuespedId ?? h.huespedId}
                  className="flex items-start gap-3 p-3 rounded-lg border border-border bg-muted/30"
                >
                  <User className="w-4 h-4 text-muted-foreground mt-0.5 shrink-0" />
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-medium text-foreground">
                      {(h.nombreCompleto ?? "").trim() || "—"}
                    </p>
                    <div className="flex flex-wrap gap-x-4 gap-y-0.5 text-xs text-muted-foreground mt-0.5">
                      {h.cedula && (
                        <span className="font-mono text-[11px] tracking-tight">
                          {h.cedula}
                        </span>
                      )}
                      {h.tipoIdentificacion && (
                        <span className="text-muted-foreground/70">
                          {h.tipoIdentificacion}
                        </span>
                      )}
                    </div>
                  </div>
                  {puedeGestionarHuespedes && h.detalleHuespedId != null && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() =>
                        onRemoveGuest(
                          detalle.id!,
                          h.detalleHuespedId!,
                          (h.nombreCompleto ?? "").trim() || "este huésped",
                        )
                      }
                      className="text-muted-foreground hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/20 h-7 w-7 p-0 shrink-0"
                    >
                      <X className="w-3.5 h-3.5" />
                    </Button>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-muted-foreground italic">
              Sin hu&eacute;spedes registrados
            </p>
          )}
        </div>
      </div>
    </div>
  );
}

export default EditarHabitacionCard;
