"use client";

import { CalendarIcon, MoonIcon, UserIcon, BedDoubleIcon } from "lucide-react";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";
import type {
  HabitacionHuespedForm,
  SelectedRoomSummary,
} from "@/features/reservas/types/reservas.types";
import ResumenReserva from "@/features/reservas/components/ResumenReserva";
import { formatDateShort } from "@/utils/date";

interface Step2ReservaProps {
  checkIn: string;
  checkOut: string;
  noches: number;
  titular: ClienteResponse | null;
  habitacionesConHuespedes: HabitacionHuespedForm[];
  selectedRooms: SelectedRoomSummary[];
  onConfirm: () => void;
  onPrevStep: () => void;
  isSubmitting?: boolean;
}

export const Step2Reserva: React.FC<Step2ReservaProps> = ({
  checkIn,
  checkOut,
  noches,
  titular,
  habitacionesConHuespedes,
  selectedRooms,
  onConfirm,
  onPrevStep,
  isSubmitting = false,
}) => {
  const totalHuespedes = habitacionesConHuespedes.reduce(
    (acc, rg) =>
      acc + (rg.habitacion.adultos || 0) + (rg.habitacion.ninos || 0),
    0,
  );

  const getInitials = (nombre?: string) => {
    if (!nombre) return "?";
    const parts = nombre.split(" ");
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return nombre.substring(0, 2).toUpperCase();
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
      <div className="lg:col-span-8 space-y-6">
        <div className="bg-card border rounded-2xl p-5 space-y-5">
          <div className="flex items-center gap-2 pb-3 border-b border-zinc-200 dark:border-zinc-700">
            <CalendarIcon className="size-5 text-rose-600" />
            <h3 className="font-semibold text-zinc-900 dark:text-white">
              Confirmación de reserva
            </h3>
          </div>

          <div className="flex items-center gap-4 flex-wrap">
            <div className="flex items-center gap-3 p-3 bg-zinc-50 dark:bg-zinc-800/50 rounded-xl flex-1 min-w-50">
              <CalendarIcon className="size-5 text-rose-600 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Check-in</p>
                <p className="font-medium text-zinc-900 dark:text-white">
                  {formatDateShort(checkIn)}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <div className="bg-zinc-100 dark:bg-zinc-800 rounded-full p-1.5">
                <MoonIcon className="size-4 text-rose-600" />
              </div>
              <span className="text-sm font-semibold text-rose-600 whitespace-nowrap">
                {noches} {noches === 1 ? "noche" : "noches"}
              </span>
            </div>
            <div className="flex items-center gap-3 p-3 bg-zinc-50 dark:bg-zinc-800/50 rounded-xl flex-1 min-w-50">
              <CalendarIcon className="size-5 text-rose-600 shrink-0" />
              <div>
                <p className="text-xs text-muted-foreground">Check-out</p>
                <p className="font-medium text-zinc-900 dark:text-white">
                  {formatDateShort(checkOut)}
                </p>
              </div>
            </div>
          </div>

          <hr className="border-zinc-200 dark:border-zinc-700" />

          {titular && (
            <div>
              <div className="flex items-center gap-2 mb-3">
                <UserIcon className="size-4 text-emerald-600" />
                <span className="text-sm font-medium text-zinc-900 dark:text-white">
                  Titular
                </span>
              </div>
              <div className="flex items-center gap-3 p-3 bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 rounded-xl w-fit">
                <div className="size-10 rounded-full bg-emerald-200 dark:bg-emerald-800 flex items-center justify-center">
                  <span className="text-emerald-700 dark:text-emerald-300 font-semibold text-sm">
                    {getInitials(titular.nombreCompleto)}
                  </span>
                </div>
                <div>
                  <p className="font-medium text-zinc-900 dark:text-white">
                    {titular.nombreCompleto}
                  </p>
                  <p className="text-sm text-muted-foreground">
                    CC {titular.cedula}
                    {titular.usuarioEmail && ` · ${titular.usuarioEmail}`}
                  </p>
                </div>
              </div>
            </div>
          )}

          <hr className="border-zinc-200 dark:border-zinc-700" />

          <div>
            <div className="flex items-center gap-2 mb-3">
              <BedDoubleIcon className="size-4 text-violet-600" />
              <span className="text-sm font-medium text-zinc-900 dark:text-white">
                Habitaciones ({habitacionesConHuespedes.length})
              </span>
            </div>
            <div className="space-y-3">
              {habitacionesConHuespedes.map((rg, idx) => (
                <div
                  key={rg.habitacion.numero}
                  className="p-4 bg-zinc-50 dark:bg-zinc-800/50 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="size-7 rounded-lg bg-violet-100 dark:bg-violet-900/30 flex items-center justify-center">
                        <span className="text-violet-700 dark:text-violet-300 font-semibold text-xs">
                          {idx + 1}
                        </span>
                      </span>
                      <span className="font-medium text-zinc-900 dark:text-white">
                        #{rg.habitacion.numero}
                      </span>
                    </div>
                    <span className="text-sm text-muted-foreground">
                      {rg.habitacion.tipo}
                    </span>
                  </div>
                  <div className="flex items-center gap-4 text-xs text-muted-foreground mb-2 ml-9">
                    <span>
                      {rg.habitacion.adultos} adulto
                      {rg.habitacion.adultos !== 1 ? "s" : ""}
                    </span>
                    {rg.habitacion.ninos > 0 && (
                      <span>
                        {rg.habitacion.ninos} niño
                        {rg.habitacion.ninos !== 1 ? "s" : ""}
                      </span>
                    )}
                  </div>
                  <div className="ml-9 space-y-1">
                    <p className="text-xs text-muted-foreground mb-1">
                      Huespedes ({rg.huespedes.length}):
                    </p>
                    {rg.huespedes.filter((h) => h.nombre).length > 0 ? (
                      rg.huespedes
                        .filter((h) => h.nombre)
                        .map((h, i) => (
                          <p
                            key={i}
                            className="text-sm text-zinc-700 dark:text-zinc-300">
                            {h.nombre} {h.documento ? `- ${h.documento}` : ""}
                          </p>
                        ))
                    ) : (
                      <p className="text-sm text-muted-foreground italic">
                        Sin huéspedes registrados
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
            <div className="mt-3 p-3 bg-violet-50 dark:bg-violet-950/30 rounded-lg border border-violet-200 dark:border-violet-800">
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">
                  Total huéspedes
                </span>
                <span className="font-semibold text-violet-700 dark:text-violet-300">
                  {totalHuespedes}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="lg:col-span-4 h-fit">
        <ResumenReserva
          currentStep={2}
          checkIn={checkIn}
          checkOut={checkOut}
          noches={noches}
          selectedRooms={selectedRooms}
          titular={
            titular
              ? {
                  nombre: titular.nombreCompleto || "",
                  documento: titular.cedula || "",
                }
              : null
          }
          habitacionesConHuespedes={habitacionesConHuespedes.map((rg) => ({
            numero: rg.habitacion.numero,
            huespedes: rg.huespedes
              .filter((h) => h.nombre)
              .map((h) => h.nombre),
          }))}
          onNextStep={onConfirm}
          onPrevStep={onPrevStep}
          isNextDisabled={isSubmitting}
        />
      </div>
    </div>
  );
};
