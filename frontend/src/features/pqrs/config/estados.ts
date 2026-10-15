import { statusConfig, type StatusConfig } from "@/lib/status-badge/types";

export const PQRS_STATUS_CONFIG: Record<string, StatusConfig> = {
  ABIERTA: statusConfig("Abierta", "AMBER"),
  EN_REVISION: statusConfig("En Revisión", "SKY"),
  RESPONDIDA: statusConfig("Respondida", "EMERALD"),
  CERRADA: statusConfig("Cerrada", "SLATE"),
  RECHAZADA: statusConfig("Rechazada", "RED"),
};
