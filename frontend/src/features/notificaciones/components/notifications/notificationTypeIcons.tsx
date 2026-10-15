import type { ReactNode } from "react";
import type { NotificacionTipo } from "../../types";
import {
  CalendarCheck,
  XCircle,
  LogIn,
  LogOut,
  MessageSquarePlus,
  MessageSquareReply,
  Wallet,
  Undo2,
  CalendarRange,
  Ban,
  TriangleAlert,
  Globe,
  User,
  Shield,
  Cpu,
  DatabaseBackup,
  ShieldAlert,
} from "lucide-react";

export const TYPE_ICON: Record<NotificacionTipo, ReactNode> = {
  NUEVA_RESERVA: <CalendarCheck className="size-4 text-cyan-600 dark:text-cyan-400" />,
  RESERVA_CANCELADA: <XCircle className="size-4 text-rose-600 dark:text-rose-400" />,
  CHECK_IN: <LogIn className="size-4 text-blue-600 dark:text-blue-400" />,
  CHECK_OUT: <LogOut className="size-4 text-violet-600 dark:text-violet-400" />,
  PAGO_REGISTRADO: <Wallet className="size-4 text-emerald-600 dark:text-emerald-400" />,
  PAGO_REEMBOLSADO: <Undo2 className="size-4 text-emerald-600 dark:text-emerald-400" />,
  PQRS_CREADA: <MessageSquarePlus className="size-4 text-amber-600 dark:text-amber-400" />,
  PQRS_RESPONDIDA: <MessageSquareReply className="size-4 text-amber-600 dark:text-amber-400" />,
  ESTADIA_EXTENDIDA: <CalendarRange className="size-4 text-cyan-600 dark:text-cyan-400" />,
  NO_SHOW: <Ban className="size-4 text-rose-600 dark:text-rose-400" />,
  ALERTA_CLIENTE: <TriangleAlert className="size-4 text-amber-600 dark:text-amber-400" />,
  BACKUP_EXITOSO: <DatabaseBackup className="size-4 text-sky-600 dark:text-sky-400" />,
  BACKUP_FALLIDO: <ShieldAlert className="size-4 text-rose-600 dark:text-rose-400" />,
};

export const TYPE_ICON_LARGE: Record<NotificacionTipo, ReactNode> = {
  NUEVA_RESERVA: <CalendarCheck className="size-6 text-cyan-600 dark:text-cyan-400" />,
  RESERVA_CANCELADA: <XCircle className="size-6 text-rose-600 dark:text-rose-400" />,
  CHECK_IN: <LogIn className="size-6 text-blue-600 dark:text-blue-400" />,
  CHECK_OUT: <LogOut className="size-6 text-violet-600 dark:text-violet-400" />,
  PAGO_REGISTRADO: <Wallet className="size-6 text-emerald-600 dark:text-emerald-400" />,
  PAGO_REEMBOLSADO: <Undo2 className="size-6 text-emerald-600 dark:text-emerald-400" />,
  PQRS_CREADA: <MessageSquarePlus className="size-6 text-amber-600 dark:text-amber-400" />,
  PQRS_RESPONDIDA: <MessageSquareReply className="size-6 text-amber-600 dark:text-amber-400" />,
  ESTADIA_EXTENDIDA: <CalendarRange className="size-6 text-cyan-600 dark:text-cyan-400" />,
  NO_SHOW: <Ban className="size-6 text-rose-600 dark:text-rose-400" />,
  ALERTA_CLIENTE: <TriangleAlert className="size-6 text-amber-600 dark:text-amber-400" />,
  BACKUP_EXITOSO: <DatabaseBackup className="size-6 text-sky-600 dark:text-sky-400" />,
  BACKUP_FALLIDO: <ShieldAlert className="size-6 text-rose-600 dark:text-rose-400" />,
};

export const ORIGEN_ICON: Record<string, ReactNode> = {
  WEB: <Globe className="size-3" />,
  RECEPCION: <User className="size-3" />,
  ADMIN: <Shield className="size-3" />,
  SISTEMA: <Cpu className="size-3" />,
};
