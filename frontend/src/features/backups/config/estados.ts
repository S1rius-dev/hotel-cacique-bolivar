import { statusConfig, type StatusConfig } from "@/lib/status-badge/types";

export const BACKUP_ESTADO_CONFIG: Record<string, StatusConfig> = {
  EXITOSO: statusConfig("Exitoso", "EMERALD"),
  EN_PROGRESO: statusConfig("En progreso", "SKY"),
  FALLIDO: statusConfig("Fallido", "RED"),
};

export const BACKUP_TIPO_LABEL: Record<string, string> = {
  AUTOMATICO: "Automática",
  MANUAL: "Manual",
};

/**
 * Un FALLIDO puede dejar un archivo parcial en disco que no restaura. Ocultar la descarga en
 * ese estado evita que alguien intente recuperar un dump incompleto creyendolo bueno.
 */
export const esDescargable = (estado?: string): boolean => estado === "EXITOSO";
