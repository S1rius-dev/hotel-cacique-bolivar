import { motion } from "motion/react";
import { BedDouble, Check, User, Building2, Users, Ticket } from "lucide-react";
import { Button } from "@/components/ui/button";
import { format, isToday } from "date-fns";
import RoomPaymentLabel from "./RoomPaymentLabel";
import { ESTADO_HABITACION_STYLES } from "../constants/disponibilidad.constants";
import type { HabitacionEstadoDiario } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  habitacion: HabitacionEstadoDiario;
  selectedDate: Date;
  selectionMode?: boolean;
  selected?: boolean;
  onToggleSelect?: () => void;
  onAsignar?: (habitacion: HabitacionEstadoDiario) => void;
  onCheckIn?: (habitacion: HabitacionEstadoDiario) => void;
  onCheckOut?: (habitacion: HabitacionEstadoDiario) => void;
  onLimpiar?: (habitacion: HabitacionEstadoDiario) => void;
  onExtender?: (habitacion: HabitacionEstadoDiario) => void;
  onVerDetalle?: (habitacion: HabitacionEstadoDiario) => void;
  onVender?: (habitacion: HabitacionEstadoDiario) => void;
}

const MAX_VISIBLE_GUESTS = 2;

function RoomCard({
  habitacion,
  selectedDate,
  selectionMode,
  selected,
  onToggleSelect,
  onAsignar,
  onCheckIn,
  onCheckOut,
  onLimpiar,
  onExtender,
  onVerDetalle,
  onVender,
}: Props) {
  const style = ESTADO_HABITACION_STYLES[habitacion.estado ?? "DISPONIBLE"] ?? {
    dot: "bg-zinc-400",
    label: habitacion.estado ?? "DISPONIBLE",
    className: "bg-zinc-100 text-zinc-700 border-zinc-200",
    glow: "border-zinc-400/10 dark:border-zinc-400/20 shadow-[0_0_18px_rgba(161,161,170,0.06)] dark:shadow-[0_0_18px_rgba(161,161,170,0.12)] hover:shadow-[0_0_25px_rgba(161,161,170,0.12)] dark:hover:shadow-[0_0_25px_rgba(161,161,170,0.2)]",
  };

  const huespedCount = habitacion.reservaActiva?.huespedes?.length ?? 0;
  const limiteOcupantes =
    habitacion.reservaActiva?.cantidadOcupantes ??
    habitacion.capacidadMaxima ??
    0;
  const puedeAgregarHuesped =
    habitacion.estado === "OCUPADA" &&
    onCheckIn &&
    habitacion.reservaActiva?.huespedes != null &&
    huespedCount < limiteOcupantes;

  const handleClick = selectionMode
    ? () => onToggleSelect?.()
    : () => onVerDetalle?.(habitacion);

  const selectedClasses = selected
    ? "ring-2 ring-rose-500/60 dark:ring-rose-400/60 shadow-[0_0_20px_rgba(244,63,94,0.12)] dark:shadow-[0_0_20px_rgba(244,63,94,0.2)] scale-[1.02]"
    : selectionMode
      ? "ring-1 ring-sky-300/40 dark:ring-sky-600/40"
      : "";

  const esDiaActual = isToday(selectedDate);

  return (
    <motion.div
      whileHover={{
        y: -3,
        transition: { type: "spring", stiffness: 300, damping: 20 },
      }}
      className={`rounded-xl border bg-white dark:bg-zinc-900/60 overflow-hidden transition-all cursor-pointer flex flex-col relative ${style.glow} ${selectedClasses}`}
      onClick={handleClick}>
      {selected && (
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          className="absolute top-2 right-2 z-10 w-6 h-6 rounded-full bg-rose-500 flex items-center justify-center shadow-md">
          <Check className="w-3.5 h-3.5 text-white" />
        </motion.div>
      )}
      {selected && (
        <div className="absolute inset-0 rounded-xl bg-rose-500/5 dark:bg-rose-500/10 pointer-events-none z-1" />
      )}
      <div className="p-3 sm:p-4 flex flex-col gap-2 sm:gap-3 flex-1 relative z-2">
        {/* Header: room info + status badge */}
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-start gap-3 min-w-0 shrink-0">
            <div className="p-2 rounded-lg bg-zinc-100 dark:bg-zinc-800 shrink-0">
              <BedDouble className="w-5 h-5 text-zinc-600 dark:text-zinc-400" />
            </div>
            <div className="shrink-0">
              <div className="flex items-center gap-1.5">
                <p className="text-lg font-bold text-zinc-900 dark:text-zinc-100 leading-none">
                  {habitacion.numero}
                </p>
                <span
                  className={`w-2 h-2 rounded-full shrink-0 ${style.dot}`}
                />
              </div>
              <p className="text-xs text-muted-foreground mt-0.5 truncate">
                <Building2 className="w-3 h-3 inline mr-0.5" />
                {habitacion.tipoNombre}
              </p>
              <p className="text-[10px] text-muted-foreground/70 flex items-center gap-0.5">
                <Users className="w-3 h-3" />
                {habitacion.capacidadBase}-{habitacion.capacidadMaxima} .Máx
              </p>
            </div>
          </div>

          <span
            className={`inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full border font-medium truncate max-w-25 ${style.className}`}>
            {style.label}
          </span>
        </div>

        {/* Body */}
        {habitacion.estado === "OCUPADA" && (
          <div className="ml-0 sm:ml-12 space-y-1.5">
            {huespedCount > 0 && (
              <div className="text-xs flex items-center gap-1">
                <User className="w-3.5 h-3.5 text-muted-foreground shrink-0" />
                <span className="font-medium text-zinc-700 dark:text-zinc-300">
                  {huespedCount} {huespedCount === 1 ? "huésped" : "huéspedes"}
                </span>
              </div>
            )}
            {habitacion
              .reservaActiva!.huespedes!.slice(0, MAX_VISIBLE_GUESTS)
              .map((h, i) => (
                <div
                  key={i}
                  className="flex items-center gap-2 text-sm text-zinc-700 dark:text-zinc-300">
                  <div className="w-3.5 shrink-0" />
                  <span className="font-medium truncate">
                    {h.nombreCompleto}
                  </span>
                  <span className="text-[11px] text-muted-foreground shrink-0">
                    {h.cedula}
                  </span>
                </div>
              ))}
            {huespedCount > MAX_VISIBLE_GUESTS && (
              <p className="text-[11px] text-muted-foreground ml-3.5">
                +{huespedCount - MAX_VISIBLE_GUESTS} más
              </p>
            )}
            {habitacion.reservaActiva && (
              <>
                <div className="flex items-center gap-2 text-xs text-muted-foreground mt-1">
                  <Ticket className="w-3 h-3 shrink-0" />
                  <span className="font-mono truncate">
                    {habitacion.reservaActiva.codigoReserva}
                  </span>
                </div>
                <RoomPaymentLabel
                  total={habitacion.reservaActiva.total ?? 0}
                  totalPagado={habitacion.reservaActiva.totalPagado ?? 0}
                  saldoPendiente={habitacion.reservaActiva.saldoPendiente ?? 0}
                />
              </>
            )}
          </div>
        )}

        {habitacion.estado === "RESERVADA" && habitacion.reservaActiva && (
          <div className="ml-0 sm:ml-12 space-y-1">
            <div className="flex items-center gap-1 text-xs text-muted-foreground">
              <User className="w-3 h-3 shrink-0" />
              <span>Sin huéspedes</span>
            </div>
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <Ticket className="w-3 h-3 shrink-0" />
              <span className="font-mono truncate">
                {habitacion.reservaActiva.codigoReserva}
              </span>
            </div>
            <RoomPaymentLabel
              total={habitacion.reservaActiva.total ?? 0}
              totalPagado={habitacion.reservaActiva.totalPagado ?? 0}
              saldoPendiente={habitacion.reservaActiva.saldoPendiente ?? 0}
            />
          </div>
        )}

        {/* Spacer + Action buttons bottom-right */}
        <div className="flex-1 min-h-2" />
        <div className="flex justify-end gap-2 flex-wrap">
          {habitacion.estado === "DISPONIBLE" && onAsignar && (
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onAsignar(habitacion);
              }}
              className="text-emerald-700 border-emerald-200 hover:bg-emerald-50 dark:text-emerald-400 dark:border-emerald-800 dark:hover:bg-emerald-900/20">
              {isToday(selectedDate) ? "Ocupar" : "Reservar"}
            </Button>
          )}
          {habitacion.estado === "EN_LIMPIEZA" && onLimpiar && (
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onLimpiar(habitacion);
              }}
              className="text-sky-700 border-sky-200 hover:bg-sky-50 dark:text-sky-400 dark:border-sky-800 dark:hover:bg-sky-900/20">
              Limpiar
            </Button>
          )}
          {habitacion.estado === "RESERVADA" && esDiaActual && onCheckIn && (
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onCheckIn(habitacion);
              }}
              className="text-amber-700 border-amber-200 hover:bg-amber-50 dark:text-amber-400 dark:border-amber-800 dark:hover:bg-amber-900/20">
              Entrada
            </Button>
          )}
          {habitacion.estado === "OCUPADA" && onCheckOut && (
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onCheckOut(habitacion);
              }}
              className="text-blue-700 border-blue-200 hover:bg-blue-50 dark:text-blue-400 dark:border-blue-800 dark:hover:bg-blue-900/20">
              Salida
            </Button>
          )}
          {habitacion.estado === "OCUPADA" &&
            onExtender &&
            habitacion.reservaActiva?.checkOut &&
            format(selectedDate, "yyyy-MM-dd") ===
              habitacion.reservaActiva.checkOut && (
              <Button
                variant="outline"
                size="sm"
                onClick={(e) => {
                  e.stopPropagation();
                  onExtender(habitacion);
                }}
                className="text-blue-700 border-blue-200 hover:bg-blue-50 dark:text-blue-400 dark:border-blue-800 dark:hover:bg-blue-900/20">
                Extender
              </Button>
            )}
          {puedeAgregarHuesped && (
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onCheckIn!(habitacion);
              }}
              className="text-amber-700 border-amber-200 hover:bg-amber-50 dark:text-amber-400 dark:border-amber-800 dark:hover:bg-amber-900/20">
              Agregar huésped
            </Button>
          )}
          {habitacion.estado === "OCUPADA" && habitacion.reservaActiva?.estadoReserva === "EN_CURSO" && onVender && (
            <Button
              variant="outline"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onVender(habitacion);
              }}
              className="text-rose-700 border-rose-200 hover:bg-rose-50 dark:text-rose-400 dark:border-rose-800 dark:hover:bg-rose-900/20">
              Vender
            </Button>
          )}
        </div>
      </div>
    </motion.div>
  );
}

export default RoomCard;
