import { statusConfig, type StatusConfig } from "@/lib/status-badge/types";

export const EMPLEADOS_STATUS_CONFIG: Record<string, StatusConfig> = {
  ACTIVO: statusConfig("Activo", "EMERALD"),
  INACTIVO: statusConfig("Inactivo", "RED"),
};
