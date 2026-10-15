export type BlockStatus = "ACTIVO" | "FINALIZADO" | "PROGRAMADO";

export type BlockState = "ACTIVO" | "PROXIMO" | "FINALIZADO" | "DESACTIVADO";

export interface BookingBlock {
  id: number;
  fechaInicio: string;
  fechaFin: string;
  motivo: string;
  activo: boolean;
  createdAt?: string;
}

export interface GeneralSetting {
  id: string;
  label: string;
  value: string;
  description?: string;
  icon: string;
}

export interface OnlineReservationRules {
  minAnticipationDays: number;
  maxAnticipationDays: number;
  hourLimit: string;
  allowSameDay: boolean;
  pendingExpirationMinutes: number;
}

export interface OnlineReservationSettings {
  webEnabled: boolean;
  rules: OnlineReservationRules;
}

function parseDate(value: string): Date {
  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    const [y, m, d] = value.split("-").map(Number);
    return new Date(y, m - 1, d);
  }
  return new Date(value);
}

export function isBlockActive(block: BookingBlock, now: Date = new Date()): boolean {
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const inicio = parseDate(block.fechaInicio);
  const fin = parseDate(block.fechaFin);
  return inicio.getTime() <= today.getTime() && fin.getTime() >= today.getTime();
}

export function deriveBlockStatus(block: BookingBlock): BlockStatus {
  if (!block.activo) return "FINALIZADO";
  const inicio = parseDate(block.fechaInicio);
  if (inicio > new Date()) return "PROGRAMADO";
  return "ACTIVO";
}

export function deriveReservationBlockState(
  block: BookingBlock,
  now: Date = new Date(),
): BlockState {
  if (!block.activo) return "DESACTIVADO";
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const inicio = parseDate(block.fechaInicio);
  const fin = parseDate(block.fechaFin);

  if (Number.isNaN(inicio.getTime()) && Number.isNaN(fin.getTime())) {
    return "ACTIVO";
  }
  if (!Number.isNaN(fin.getTime()) && fin.getTime() < today.getTime()) {
    return "FINALIZADO";
  }
  if (
    !Number.isNaN(inicio.getTime()) &&
    !Number.isNaN(fin.getTime()) &&
    inicio.getTime() <= today.getTime() &&
    fin.getTime() >= today.getTime()
  ) {
    return "ACTIVO";
  }
  return "PROXIMO";
}

export function canReactivateBlock(
  block: BookingBlock,
  now: Date = new Date(),
): boolean {
  if (block.activo) return false;
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const fin = parseDate(block.fechaFin);
  if (Number.isNaN(fin.getTime())) return true;
  return fin.getTime() >= today.getTime();
}