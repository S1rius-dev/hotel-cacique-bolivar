import type {
  BackupResponseEstado,
  BackupResponseTipo,
} from "@/api/generated/openAPIDefinition.schemas";

export type { BackupResponseEstado, BackupResponseTipo };

export const BACKUP_PAGE_SIZE = 15;

export interface BackupFilters {
  page: number;
  size: number;
  tipo?: BackupResponseTipo;
  estado?: BackupResponseEstado;
}
