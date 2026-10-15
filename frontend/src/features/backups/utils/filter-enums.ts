import type {
  BackupResponseEstado,
  BackupResponseTipo,
} from "../types";

/** Traduce el valor del filtro ("all" o un enum) al parámetro de la API. */
export const asTipo = (v: string): BackupResponseTipo | undefined =>
  v === "all" ? undefined : (v as BackupResponseTipo);

export const asEstado = (v: string): BackupResponseEstado | undefined =>
  v === "all" ? undefined : (v as BackupResponseEstado);
