import type { ReactNode } from "react";
import type { NotificacionTipo } from "../../types";
import { formatearMoneda, formatearFecha } from "./notificationUtils";
import {
  User,
  Hash,
  CalendarDays,
  DollarSign,
  CreditCard,
  DoorOpen,
  FileText,
  TriangleAlert,
  MessageSquarePlus,
} from "lucide-react";

export function getExtraFields(
  tipo: NotificacionTipo,
  d: Record<string, unknown> | null,
): { icon: ReactNode; value: string }[] {
  if (!d) return [];

  const currency = (v: unknown) =>
    v != null ? formatearMoneda(Number(v)) : null;
  const date = (v: unknown) => (v != null ? formatearFecha(v as string) : null);
  const str = (v: unknown) => (v != null ? String(v) : null);

  const fields: {
    icon: ReactNode;
    keys: string[];
    join?: string;
    transform?: (v: unknown) => string | null;
  }[] = [];

  switch (tipo) {
    case "NUEVA_RESERVA":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <Hash className="size-3" />, keys: ["codigoReserva"], transform: str },
        { icon: <CalendarDays className="size-3" />, keys: ["fechaCheckIn", "fechaCheckOut"], join: " → ", transform: date },
        { icon: <DollarSign className="size-3" />, keys: ["total"], transform: currency },
      );
      break;
    case "RESERVA_CANCELADA":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <Hash className="size-3" />, keys: ["codigoReserva"], transform: str },
      );
      break;
    case "CHECK_IN":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <Hash className="size-3" />, keys: ["codigoReserva"], transform: str },
        { icon: <DoorOpen className="size-3" />, keys: ["habitaciones"], transform: str },
      );
      break;
    case "CHECK_OUT":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <Hash className="size-3" />, keys: ["codigoReserva"], transform: str },
        { icon: <DoorOpen className="size-3" />, keys: ["habitaciones"], transform: str },
      );
      break;
    case "PAGO_REGISTRADO":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <CreditCard className="size-3" />, keys: ["codigoPago"], transform: str },
        { icon: <DollarSign className="size-3" />, keys: ["monto"], transform: currency },
        { icon: <Hash className="size-3" />, keys: ["codigoReserva"], transform: str },
      );
      break;
    case "PAGO_REEMBOLSADO":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <CreditCard className="size-3" />, keys: ["codigoPago"], transform: str },
        { icon: <DollarSign className="size-3" />, keys: ["monto"], transform: currency },
      );
      break;
    case "PQRS_CREADA":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <FileText className="size-3" />, keys: ["radicado"], transform: str },
        { icon: <MessageSquarePlus className="size-3" />, keys: ["tipoPQRS"], transform: str },
      );
      break;
    case "PQRS_RESPONDIDA":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <FileText className="size-3" />, keys: ["radicado"], transform: str },
      );
      break;
    case "ESTADIA_EXTENDIDA":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <Hash className="size-3" />, keys: ["codigoReserva"], transform: str },
        { icon: <CalendarDays className="size-3" />, keys: ["nuevaFechaSalida"], transform: date },
      );
      break;
    case "NO_SHOW":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <Hash className="size-3" />, keys: ["codigoReserva"], transform: str },
        { icon: <DoorOpen className="size-3" />, keys: ["habitaciones"], transform: str },
      );
      break;
    case "ALERTA_CLIENTE":
      fields.push(
        { icon: <User className="size-3" />, keys: ["clienteNombre"], transform: str },
        { icon: <TriangleAlert className="size-3" />, keys: ["asunto"], transform: str },
      );
      break;
  }

  return fields.flatMap((f) => {
    const values = f.keys.map((k) => d[k]).filter((v) => v != null);
    if (values.length === 0) return [];
    const parts = values.map((v) => f.transform?.(v) ?? String(v));
    if (parts.some((p) => p == null)) return [];
    return [{ icon: f.icon, value: parts.join(f.join ?? " · ") }];
  });
}
