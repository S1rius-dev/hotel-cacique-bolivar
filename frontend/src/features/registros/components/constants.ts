import { statusConfig, type StatusConfig } from "@/lib/status-badge/types";
import type { HuespedRegistroDTOEstadoDetalle } from "@/api/generated/openAPIDefinition.schemas";

export const RECORD_STATUS_CONFIG: Record<HuespedRegistroDTOEstadoDetalle, StatusConfig> = {
  RESERVADA: statusConfig("Reservada", "BLUE"),
  OCUPADA: statusConfig("Ocupada", "EMERALD"),
  LIBERADA: statusConfig("Liberada", "SLATE"),
  CANCELADA: statusConfig("Cancelada", "RED"),
};

export const ESTADO_DETALLE_OPTIONS = [
  { value: "all", label: "Todos los estados" },
  { value: "RESERVADA", label: "Reservada" },
  { value: "OCUPADA", label: "Ocupada" },
  { value: "LIBERADA", label: "Liberada" },
  { value: "CANCELADA", label: "Cancelada" },
] as const;
