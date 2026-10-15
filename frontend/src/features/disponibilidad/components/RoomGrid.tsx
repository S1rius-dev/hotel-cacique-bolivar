import { motion } from "motion/react";
import { BedDouble } from "lucide-react";
import RoomCard from "./RoomCard";
import type { HabitacionEstadoDiario } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  habitaciones: HabitacionEstadoDiario[];
  selectedDate: Date;
  selectionMode?: boolean;
  selectedRoomIds?: Set<number>;
  onToggleSelect?: (id: number) => void;
  onAsignar?: (habitacion: HabitacionEstadoDiario) => void;
  onCheckIn?: (habitacion: HabitacionEstadoDiario) => void;
  onCheckOut?: (habitacion: HabitacionEstadoDiario) => void;
  onLimpiar?: (habitacion: HabitacionEstadoDiario) => void;
  onExtender?: (habitacion: HabitacionEstadoDiario) => void;
  onVerDetalle?: (habitacion: HabitacionEstadoDiario) => void;
  onVender?: (habitacion: HabitacionEstadoDiario) => void;
}

const ORDER = [
  "OCUPADA",
  "RESERVADA",
  "EN_LIMPIEZA",
  "DISPONIBLE",
  "MANTENIMIENTO",
  "INHABILITADA",
] as const;

function RoomGrid({ habitaciones, selectedDate, selectionMode, selectedRoomIds, onToggleSelect, onAsignar, onCheckIn, onCheckOut, onLimpiar, onExtender, onVerDetalle, onVender }: Props) {
  const sorted = [...habitaciones].sort(
    (a, b) =>
      ORDER.indexOf(a.estado as (typeof ORDER)[number]) -
      ORDER.indexOf(b.estado as (typeof ORDER)[number]),
  );

  if (habitaciones.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <div className="p-4 rounded-full bg-zinc-100 dark:bg-zinc-800 mb-4">
          <BedDouble className="w-8 h-8 text-muted-foreground" />
        </div>
        <p className="text-sm font-medium text-zinc-900 dark:text-zinc-100">
          No hay habitaciones
        </p>
        <p className="text-xs text-muted-foreground mt-1">
          No se encontraron habitaciones para esta fecha.
        </p>
      </div>
    );
  }

  return (
    <div>
      <motion.div
        initial={false}
        animate="visible"
        variants={{ hidden: {}, visible: { transition: { staggerChildren: 0.04 } } }}
        className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {sorted.map((h) => (
          <motion.div
            key={h.id}
            initial={false}
            variants={{ hidden: { opacity: 0, y: 12 }, visible: { opacity: 1, y: 0 } }}
          >
            <RoomCard
              habitacion={h}
              selectedDate={selectedDate}
              selectionMode={selectionMode}
              selected={selectedRoomIds?.has(h.id!)}
              onToggleSelect={() => onToggleSelect?.(h.id!)}
              onAsignar={onAsignar}
              onCheckIn={onCheckIn}
              onCheckOut={onCheckOut}
              onLimpiar={onLimpiar}
              onExtender={onExtender}
              onVerDetalle={selectionMode ? undefined : onVerDetalle}
              onVender={onVender}
            />
          </motion.div>
        ))}
      </motion.div>
    </div>
  );
}

export default RoomGrid;
