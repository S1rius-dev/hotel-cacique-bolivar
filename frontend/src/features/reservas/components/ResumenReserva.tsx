import {
  CalendarIcon,
  MoonIcon,
  UserIcon,
  BedDoubleIcon,
  XIcon,
  ArrowLeftIcon,
  ArrowRightIcon,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { formatCurrency } from "@/utils/format";
import type { SelectedRoomSummary } from "@/features/reservas/types/reservas.types";

interface HabitacionHuesped {
  numero: string;
  huespedes: string[];
}

interface ResumenReservaProps {
  currentStep: 1 | 2;
  checkIn: string;
  checkOut: string;
  noches: number;
  selectedRooms?: SelectedRoomSummary[];
  onRemoveRoom?: (numero: string) => void;
  onClearRooms?: () => void;
  /** Activa el modo "precio con IVA" de una habitación seleccionada (paso 1). */
  onToggleIva?: (numero: string) => void;
  titular?: { nombre: string; documento: string } | null;
  habitacionesConHuespedes?: HabitacionHuesped[];
  onNextStep: () => void;
  onPrevStep?: () => void;
  isNextDisabled?: boolean;
  nextDisabledReason?: string;
}

const ResumenReserva: React.FC<ResumenReservaProps> = ({
  currentStep,
  checkIn,
  checkOut,
  noches,
  selectedRooms = [],
  onRemoveRoom,
  onClearRooms,
  onToggleIva,
  titular,
  habitacionesConHuespedes = [],
  onNextStep,
  onPrevStep,
  isNextDisabled = false,
  nextDisabledReason,
}) => {
  const adultos = selectedRooms.reduce((acc, r) => acc + (r.adultos || 0), 0);
  const ninos = selectedRooms.reduce((acc, r) => acc + (r.ninos || 0), 0);

  const totalHuespedes =
    currentStep === 2
      ? selectedRooms.reduce(
          (acc, r) => acc + (r.adultos || 0) + (r.ninos || 0),
          0,
        )
      : habitacionesConHuespedes.reduce(
          (acc, h) => acc + h.huespedes.length,
          0,
        );

  const total = selectedRooms.reduce(
    (acc, room) => acc + (room.precio || 0) * noches,
    0,
  );

  return (
    <div className="sticky top-6 space-y-4">
      {currentStep === 2 && (
        <div className="bg-card border rounded-2xl p-5 space-y-3">
          <div className="flex items-center gap-2 pb-3 border-b border-zinc-200 dark:border-zinc-700">
            <CalendarIcon className="size-5 text-rose-600" />
            <h3 className="font-semibold text-zinc-900 dark:text-white">
              Resumen
            </h3>
          </div>

          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Check-in</span>
            <span className="font-medium">{checkIn || "-"}</span>
          </div>
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Check-out</span>
            <span className="font-medium">{checkOut || "-"}</span>
          </div>
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted-foreground">Noches</span>
            <span className="font-medium flex items-center gap-1">
              <MoonIcon className="size-3.5" />
              {noches}
            </span>
          </div>

          {titular?.nombre && (
            <div className="pt-3 border-t border-zinc-200 dark:border-zinc-700">
              <div className="flex items-center gap-2 mb-2">
                <UserIcon className="size-4 text-emerald-600" />
                <span className="text-sm font-medium">{titular.nombre}</span>
              </div>
              <p className="text-xs text-muted-foreground ml-6">
                CC {titular.documento}
              </p>
            </div>
          )}
        </div>
      )}

      {currentStep === 1 && (
        <div className="bg-card border rounded-2xl p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h4 className="font-medium text-zinc-900 dark:text-white flex items-center gap-2">
              <BedDoubleIcon className="size-4" />
              Habitaciones ({selectedRooms.length})
            </h4>
            {selectedRooms.length > 0 && onClearRooms && (
              <button
                onClick={onClearRooms}
                className="text-xs text-rose-600 hover:text-rose-700">
                Limpiar todo
              </button>
            )}
          </div>

          {selectedRooms.length === 0 ? (
            <div className="text-center py-6 bg-zinc-50 dark:bg-zinc-800/50 rounded-lg border border-dashed border-zinc-300 dark:border-zinc-700">
              <BedDoubleIcon className="size-8 mx-auto text-zinc-300 dark:text-zinc-600 mb-2" />
              <p className="text-sm text-muted-foreground">
                Seleccione habitaciones disponibles
              </p>
            </div>
          ) : (
            <>
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {selectedRooms.map((room) => (
                  <div
                    key={room.numero}
                    className="p-2 bg-zinc-50 dark:bg-zinc-800/50 rounded-lg">
                    <div className="flex items-center justify-between">
                      <div className="flex items-baseline gap-1.5 flex-wrap">
                        <span className="text-sm font-medium text-zinc-900 dark:text-white">
                          #{room.numero}
                        </span>
                        <span className="text-xs text-muted-foreground">
                          {room.tipo}
                        </span>
                        <span className="text-xs text-muted-foreground">
                          ({room.adultos} A — {room.ninos} N)
                        </span>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-medium text-rose-700 dark:text-rose-400">
                          {formatCurrency(room.precio * noches)}
                        </span>
                        {onRemoveRoom && (
                          <button
                            onClick={() => onRemoveRoom(room.numero)}
                            className="size-5 rounded-full bg-zinc-200 dark:bg-zinc-700 flex items-center justify-center hover:bg-rose-100 dark:hover:bg-rose-900/30 transition-colors">
                            <XIcon className="size-3 text-zinc-500" />
                          </button>
                        )}
                      </div>
                    </div>

                    {onToggleIva && (
                      <div className="flex items-center justify-between mt-1.5 pt-1.5 border-t border-zinc-200/70 dark:border-zinc-700/70">
                        <span className="text-[11px] text-muted-foreground">
                          Precio con IVA
                        </span>
                        <Switch
                          checked={room.precioIncluyeIva ?? false}
                          onCheckedChange={() => onToggleIva(room.numero)}
                          aria-label={`Cambiar modo de precio de la habitación ${room.numero}`}
                        />
                      </div>
                    )}
                  </div>
                ))}
              </div>
              <div className="flex items-center gap-4 text-sm pt-2 border-t border-zinc-200 dark:border-zinc-700">
                <span className="flex items-center gap-1.5 text-muted-foreground">
                  <UserIcon className="size-4 text-rose-600" />
                  {adultos} adulto{adultos !== 1 ? "s" : ""}
                </span>
                <span className="flex items-center gap-1.5 text-muted-foreground">
                  <UserIcon className="size-4 text-amber-600" />
                  {ninos} niño{ninos !== 1 ? "s" : ""}
                </span>
              </div>
            </>
          )}
        </div>
      )}

      {currentStep === 2 && (
        <div className="bg-card border rounded-2xl p-5 space-y-4">
          <div className="flex items-center gap-2">
            <BedDoubleIcon className="size-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">Habitaciones</span>
          </div>
          <div className="space-y-2 max-h-40 overflow-y-auto">
            {habitacionesConHuespedes.map((hab) => (
              <div
                key={hab.numero}
                className="p-2 bg-zinc-50 dark:bg-zinc-800/50 rounded-lg">
                <span className="text-xs font-medium text-zinc-900 dark:text-white block mb-1">
                  Habitación {hab.numero}
                </span>
                <div className="space-y-0.5">
                  {hab.huespedes.length > 0 ? (
                    hab.huespedes.map((h, i) => (
                      <p key={i} className="text-xs text-muted-foreground">
                        • {h}
                      </p>
                    ))
                  ) : (
                    <p className="text-xs text-muted-foreground italic">
                      Sin huéspedes
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
          <div className="flex items-center gap-4 text-sm pt-2 border-t border-zinc-200 dark:border-zinc-700">
            <span className="flex items-center gap-1.5 text-muted-foreground">
              <UserIcon className="size-4 text-rose-600" />
              {adultos} adulto{adultos !== 1 ? "s" : ""}
            </span>
            <span className="flex items-center gap-1.5 text-muted-foreground">
              <UserIcon className="size-4 text-amber-600" />
              {ninos} niño{ninos !== 1 ? "s" : ""}
            </span>
          </div>
          <div className="pt-2 border-t border-zinc-200 dark:border-zinc-700">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-zinc-900 dark:text-white">
                Total huéspedes
              </span>
              <span className="text-sm font-semibold text-rose-700 dark:text-rose-400">
                {totalHuespedes}
              </span>
            </div>
          </div>
        </div>
      )}

      {currentStep === 2 && selectedRooms.length > 0 && (
        <div className="bg-card border border-rose-200 dark:border-rose-800/50 rounded-2xl p-5">
          <div className="flex items-center justify-between">
            <span className="font-semibold text-zinc-900 dark:text-white">
              Total
            </span>
            <span className="text-xl font-bold text-rose-700 dark:text-rose-400">
              {formatCurrency(total)}
            </span>
          </div>
        </div>
      )}

      <div className="space-y-2">
        {currentStep === 1 ? (
          <Button
            className="w-full bg-rose-800 hover:bg-rose-900 text-white disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
            onClick={onNextStep}
            disabled={isNextDisabled}
            title={nextDisabledReason}>
            Continuar
            <ArrowRightIcon className="size-4 ml-2" />
          </Button>
        ) : (
          <div className="flex gap-2">
            {onPrevStep && (
              <Button
                variant="outline"
                onClick={onPrevStep}
                className="flex-1 border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-800 cursor-pointer">
                <ArrowLeftIcon className="size-4 mr-1" />
                Volver
              </Button>
            )}
            <Button
              onClick={onNextStep}
              className="flex-1 bg-rose-800 hover:bg-rose-900 text-white cursor-pointer">
              Continuar
              <ArrowRightIcon className="size-4 ml-1" />
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ResumenReserva;
