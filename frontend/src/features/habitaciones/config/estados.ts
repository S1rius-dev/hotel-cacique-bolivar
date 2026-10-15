import { statusConfig, type StatusConfig } from "@/lib/status-badge/types";

export const HABITACIONES_STATUS_CONFIG: Record<string, StatusConfig> = {
  DISPONIBLE: statusConfig("Disponible", "EMERALD"),
  RESERVADA: statusConfig("Reservada", "VIOLET"),
  OCUPADA: statusConfig("Ocupada", "ORANGE"),
  EN_LIMPIEZA: statusConfig("En limpieza", "SKY"),
  MANTENIMIENTO: statusConfig("Mantenimiento", "ZINC"),
  INHABILITADA: statusConfig("Inhabilitada", "NEUTRAL"),
};
