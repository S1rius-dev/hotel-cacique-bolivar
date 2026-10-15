import { statusConfig, type StatusConfig } from "@/lib/status-badge/types";

export const RESERVAS_STATUS_CONFIG: Record<string, StatusConfig> = {
  PENDIENTE: statusConfig("Pendiente", "AMBER"),
  CONFIRMADA: statusConfig("Confirmada", "SKY"),
  EN_CURSO: statusConfig("En curso", "BLUE"),
  FINALIZADA: statusConfig("Finalizada", "SLATE"),
  CANCELADA: statusConfig("Cancelada", "RED"),
  NO_SHOW: statusConfig("No Llegó", "ROSE"),
  LIBERADA: statusConfig("Liberada", "SLATE"),
};
