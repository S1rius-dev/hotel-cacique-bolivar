import type { NotificacionTipo } from "./types";

export const NOTIFICACION_TIPO_TITULO: Record<NotificacionTipo, string> = {
  NUEVA_RESERVA: "Nueva reserva",
  RESERVA_CANCELADA: "Reserva cancelada",
  CHECK_IN: "Check-in",
  CHECK_OUT: "Check-out",
  PAGO_REGISTRADO: "Pago registrado",
  PAGO_REEMBOLSADO: "Pago reembolsado",
  PQRS_CREADA: "PQRS creada",
  PQRS_RESPONDIDA: "PQRS respondida",
  ESTADIA_EXTENDIDA: "Estadía extendida",
  NO_SHOW: "No-Show",
  ALERTA_CLIENTE: "Alerta de cliente",
  BACKUP_EXITOSO: "Copia de seguridad creada",
  BACKUP_FALLIDO: "Copia de seguridad fallida",
};

export const NOTIFICACION_TIPO_COLOR: Record<
  NotificacionTipo,
  {
    border: string;
    dot: string;
    tint: string;
    iconCircle: string;
  }
> = {
  NUEVA_RESERVA:     { border: "border-l-cyan-500", dot: "bg-cyan-500", tint: "bg-cyan-500/8", iconCircle: "bg-cyan-500/15" },
  RESERVA_CANCELADA: { border: "border-l-rose-500", dot: "bg-rose-500", tint: "bg-rose-500/8", iconCircle: "bg-rose-500/15" },
  CHECK_IN:          { border: "border-l-blue-500", dot: "bg-blue-500", tint: "bg-blue-500/8", iconCircle: "bg-blue-500/15" },
  CHECK_OUT:         { border: "border-l-violet-500", dot: "bg-violet-500", tint: "bg-violet-500/8", iconCircle: "bg-violet-500/15" },
  PAGO_REGISTRADO:   { border: "border-l-emerald-500", dot: "bg-emerald-500", tint: "bg-emerald-500/8", iconCircle: "bg-emerald-500/15" },
  PAGO_REEMBOLSADO:  { border: "border-l-emerald-500", dot: "bg-emerald-500", tint: "bg-emerald-500/8", iconCircle: "bg-emerald-500/15" },
  PQRS_CREADA:       { border: "border-l-amber-500", dot: "bg-amber-500", tint: "bg-amber-500/8", iconCircle: "bg-amber-500/15" },
  PQRS_RESPONDIDA:   { border: "border-l-amber-500", dot: "bg-amber-500", tint: "bg-amber-500/8", iconCircle: "bg-amber-500/15" },
  ESTADIA_EXTENDIDA: { border: "border-l-cyan-500", dot: "bg-cyan-500", tint: "bg-cyan-500/8", iconCircle: "bg-cyan-500/15" },
  NO_SHOW:           { border: "border-l-rose-500", dot: "bg-rose-500", tint: "bg-rose-500/8", iconCircle: "bg-rose-500/15" },
  ALERTA_CLIENTE:    { border: "border-l-amber-500", dot: "bg-amber-500", tint: "bg-amber-500/8", iconCircle: "bg-amber-500/15" },
  BACKUP_EXITOSO:    { border: "border-l-sky-500", dot: "bg-sky-500", tint: "bg-sky-500/8", iconCircle: "bg-sky-500/15" },
  BACKUP_FALLIDO:    { border: "border-l-rose-500", dot: "bg-rose-500", tint: "bg-rose-500/8", iconCircle: "bg-rose-500/15" },
};

export const NOTIFICACION_ORIGEN: Record<string, { label: string }> = {
  WEB: { label: "Sitio web" },
  RECEPCION: { label: "Recepción" },
  ADMIN: { label: "Administrador" },
  SISTEMA: { label: "Sistema" },
};

export const NOTIFICACION_RUTA: Record<string, (id: number) => string> = {
  RESERVA: (id) => `/dashboard/reservas/${id}`,
  PAGO: (id) => `/dashboard/reservas/${id}/pagos`,
  PQRS: (id) => `/dashboard/pqrs/${id}`,
  CLIENTE: (id) => `/dashboard/clientes/${id}`,
  // La copia no tiene pagina de detalle, asi que el id se ignora y se lleva al listado.
  BACKUP: () => `/dashboard/backups`,
};
