import type { TurnoResponse } from "@/api/generated/openAPIDefinition.schemas";

export type TipoTurno = "MANANA" | "TARDE" | "NOCHE";

export interface TurnoInfo {
  label: string;
  inicio: string;
  fin: string;
}

function toAMPM(hora: number): string {
  if (hora === 0) return "12:00 AM";
  if (hora < 12) return `${hora}:00 AM`;
  if (hora === 12) return "12:00 PM";
  return `${hora - 12}:00 PM`;
}

export function formatHoraAMPM(hora24: string): string {
  const [h] = hora24.split(":").map(Number);
  if (isNaN(h)) return hora24;
  return toAMPM(h);
}

export const TIPOS_TURNO: Record<TipoTurno, TurnoInfo> = {
  MANANA: { label: "Mañana", inicio: "7:00 AM", fin: "3:00 PM" },
  TARDE: { label: "Tarde", inicio: "3:00 PM", fin: "10:00 PM" },
  NOCHE: { label: "Noche", inicio: "10:00 PM", fin: "7:00 AM" },
} as const;

export function obtenerTurnoActual(): {
  key: TipoTurno;
  info: TurnoInfo;
} {
  const hora = new Date().getHours();
  if (hora >= 7 && hora < 15)
    return { key: "MANANA", info: TIPOS_TURNO.MANANA };
  if (hora >= 15 && hora < 22)
    return { key: "TARDE", info: TIPOS_TURNO.TARDE };
  return { key: "NOCHE", info: TIPOS_TURNO.NOCHE };
}

export function turnoDesdeDTO(
  dto?: TurnoResponse | null,
): { key: TipoTurno; info: TurnoInfo } | null {
  if (!dto?.tipoTurno) return null;
  const key = dto.tipoTurno as TipoTurno;
  const info = TIPOS_TURNO[key];
  if (!info) return null;
  return { key, info };
}

export function formatUsuario(value?: string | null): string {
  if (!value) return "-";
  if (value.includes("@")) return value.split("@")[0];
  return value;
}

export type CajaNombre = "RECEPCION" | "TIENDA";

export const CAJAS: { value: CajaNombre; label: string }[] = [
  { value: "RECEPCION", label: "Recepción" },
  { value: "TIENDA", label: "Tienda" },
] as const;

export type { TurnoResponse };
