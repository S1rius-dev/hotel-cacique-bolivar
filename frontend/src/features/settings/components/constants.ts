import { statusConfig, type StatusConfig } from "@/lib/status-badge/types";
import type { GeneralSetting, BlockState, OnlineReservationRules } from "../types";

export const BLOCK_STATUS_CONFIG: Record<string, StatusConfig> = {
  ACTIVO: statusConfig("Activo", "ORANGE"),
  FINALIZADO: statusConfig("Finalizado", "ZINC"),
  PROGRAMADO: statusConfig("Programado", "BLUE"),
};

export const BLOCK_STATE_CONFIG: Record<BlockState, StatusConfig> = {
  ACTIVO: statusConfig("Activo", "ROSE"),
  PROXIMO: statusConfig("Próximo", "ORANGE"),
  FINALIZADO: statusConfig("Finalizado", "VIOLET"),
  DESACTIVADO: statusConfig("Desactivado", "ZINC"),
};

export const BLOCK_MOTIVOS = [
  "Mantenimiento",
  "Evento privado",
  "Temporada cerrada",
  "Problemas operativos",
  "Otro",
] as const;

export const ONLINE_RESERVATION_DEFAULTS: OnlineReservationRules = {
  minAnticipationDays: 1,
  maxAnticipationDays: 90,
  hourLimit: "22:00",
  allowSameDay: true,
  pendingExpirationMinutes: 15,
};

export const GENERAL_SETTINGS: GeneralSetting[] = [
  {
    id: "hotel-name",
    label: "Nombre del hotel",
    value: "Hotel Cacique",
    description: "Nombre comercial que se muestra en recibos y comunicaciones",
    icon: "Building2",
  },
  {
    id: "timezone",
    label: "Zona horaria",
    value: "America/Bogotá (UTC -5)",
    description: "Huso horario para todas las operaciones del sistema",
    icon: "Globe",
  },
  {
    id: "currency",
    label: "Moneda",
    value: "COP (Peso colombiano)",
    description: "Moneda predeterminada para tarifas y transacciones",
    icon: "CircleDollarSign",
  },
  {
    id: "language",
    label: "Idioma",
    value: "Español (Colombia)",
    description: "Idioma de la interfaz del sistema",
    icon: "Languages",
  },
  {
    id: "checkin",
    label: "Hora de check-in",
    value: "15:00",
    description: "Hora a partir de la cual los huéspedes pueden ingresar",
    icon: "Clock",
  },
  {
    id: "checkout",
    label: "Hora de check-out",
    value: "12:00",
    description: "Hora límite para la salida de los huéspedes",
    icon: "Clock",
  },
];