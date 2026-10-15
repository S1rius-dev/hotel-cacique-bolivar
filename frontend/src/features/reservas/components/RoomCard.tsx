import { BedDoubleIcon, CheckIcon, UsersIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import type { DisponibilidadResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatPrice } from "../utils/room.utils";

interface RoomCardProps {
  habitacion: DisponibilidadResponse;
  isSelected: boolean;
  isDisabled: boolean;
  onSelect: () => void;
  onDeselect: () => void;
}

export const RoomCard: React.FC<RoomCardProps> = ({
  habitacion,
  isSelected,
  isDisabled,
  onSelect,
  onDeselect,
}) => {
  const numero = habitacion.numero ?? "";
  const tipoNombre = habitacion.tipoNombre ?? "Sin tipo";
  const capacidad = habitacion.capacidadMaxima ?? 0;
  const precio = habitacion.precioBase ?? 0;

  const handleClick = () => {
    if (isSelected) {
      onDeselect();
    } else {
      onSelect();
    }
  };

  return (
    <div
      className={cn(
        "bg-card border rounded-xl overflow-hidden transition-all duration-300 w-full max-w-60 mx-auto",
        isSelected
          ? "border-rose-500 ring-2 ring-rose-500/20"
          : "border-zinc-200 dark:border-zinc-800",
        isDisabled ? "opacity-60" : "hover:shadow-md hover:border-rose-800/30",
      )}>
      <div className="h-20 bg-linear-to-br from-zinc-100 to-zinc-200 dark:from-zinc-800 dark:to-zinc-700 flex items-center justify-center">
        <BedDoubleIcon className="size-8 text-zinc-400 dark:text-zinc-500" />
      </div>

      <div className="p-3 space-y-3">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-2">
              <h4 className="font-bold text-base text-zinc-900 dark:text-white">
                N° {numero}
              </h4>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-medium whitespace-nowrap bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400">
                Disponible
              </span>
            </div>
            <p className="text-sm font-medium text-rose-700 dark:text-rose-400 mt-0.5">
              {tipoNombre}
            </p>
          </div>
        </div>

        <div className="bg-zinc-50 dark:bg-zinc-800/50 rounded-lg p-2 space-y-1.5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <UsersIcon className="size-3.5 text-muted-foreground" />
              <span className="text-xs text-muted-foreground">Capacidad:</span>
            </div>
            <span className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
              {capacidad} pers.
            </span>
          </div>
          {(habitacion.capacidadBase ?? 0) > 0 && (
            <div className="flex items-center justify-between">
              <span className="text-xs text-muted-foreground">Incluye:</span>
              <span className="text-xs text-zinc-600 dark:text-zinc-400">
                {habitacion.capacidadBase} pers. base
              </span>
            </div>
          )}
          {(habitacion.precioPersonaExtra ?? 0) > 0 && (
            <div className="flex items-center justify-between">
              <span className="text-xs text-muted-foreground">Extra/pers.:</span>
              <span className="text-xs font-medium text-amber-600 dark:text-amber-400">
                +{formatPrice(habitacion.precioPersonaExtra)}
              </span>
            </div>
          )}
          <div className="flex items-center justify-between">
            <span className="text-xs text-muted-foreground">Precio base:</span>
            <span className="text-sm font-bold text-rose-700 dark:text-rose-400">
              {formatPrice(precio)}
            </span>
          </div>
        </div>

        <div className="flex justify-end pt-1">
          <Button
            size="sm"
            disabled={isDisabled}
            onClick={handleClick}
            className={cn(
              "h-8 px-3 text-xs cursor-pointer",
              isSelected
                ? "bg-rose-100 text-rose-800 hover:bg-rose-200 dark:bg-rose-900/30 dark:text-rose-400"
                : "bg-rose-800 hover:bg-rose-900 text-white",
            )}>
            {isSelected ? (
              <>
                <CheckIcon className="size-3 mr-1" />
                Seleccionada
              </>
            ) : (
              "Seleccionar"
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};
