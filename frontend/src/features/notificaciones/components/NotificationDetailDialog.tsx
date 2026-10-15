import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import {
  CheckCheck,
} from "lucide-react";
import type { Notificacion, NotificacionTipo } from "../types";
import {
  NOTIFICACION_TIPO_TITULO,
  NOTIFICACION_TIPO_COLOR,
  NOTIFICACION_ORIGEN,
  NOTIFICACION_RUTA,
} from "../constants";
import {
  TYPE_ICON_LARGE,
  ORIGEN_ICON,
} from "./notifications/notificationTypeIcons";
import { useNavigate } from "react-router-dom";

function getOrigenBadge(origen: string | null) {
  if (!origen) return null;
  const config = NOTIFICACION_ORIGEN[origen];
  if (!config) return null;
  return { icon: ORIGEN_ICON[origen], label: config.label };
}

function formatDate(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleDateString("es-CO", {
    day: "numeric",
    month: "long",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getNavigationUrl(n: Notificacion): string | null {
  if (!n.referenciaTipo || n.referenciaId == null) return null;
  // Para PAGO: si detalles trae reservaId/reservaId real, preferirlo; alias /pagos resuelve pagoId también
  if (n.referenciaTipo === "PAGO") {
    const detalles = n.detalles as Record<string, unknown> | null;
    const reservaIdFromDetalle =
      (detalles?.["reservaId"] as number | undefined) ??
      (detalles?.["reserva_id"] as number | undefined) ??
      (detalles?.["idReserva"] as number | undefined);
    const id = reservaIdFromDetalle ?? n.referenciaId;
    // El alias /reservas/:id/pagos resuelve tanto pagoId como reservaId
    return `/dashboard/reservas/${id}/pagos`;
  }
  const rutaFn = NOTIFICACION_RUTA[n.referenciaTipo];
  if (!rutaFn) return null;
  return rutaFn(n.referenciaId);
}

type SeccionDetalle = { label: string; value: string };

const SECCIONES_POR_TIPO: Partial<
  Record<
    NotificacionTipo,
    { label: string; key: string; transform?: (v: unknown) => string }[]
  >
> = {
  NUEVA_RESERVA: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Código de reserva", key: "codigoReserva" },
    { label: "Check-in", key: "fechaCheckIn" },
    { label: "Check-out", key: "fechaCheckOut" },
    { label: "Total", key: "total", transform: (v) => `$${Number(v).toLocaleString("es-CO")}` },
  ],
  RESERVA_CANCELADA: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Código de reserva", key: "codigoReserva" },
  ],
  CHECK_IN: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Código de reserva", key: "codigoReserva" },
    { label: "Habitaciones", key: "habitaciones" },
  ],
  CHECK_OUT: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Código de reserva", key: "codigoReserva" },
    { label: "Habitaciones", key: "habitaciones" },
  ],
  PAGO_REGISTRADO: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Código de pago", key: "codigoPago" },
    { label: "Reserva", key: "codigoReserva" },
    { label: "Monto", key: "monto", transform: (v) => `$${Number(v).toLocaleString("es-CO")}` },
    { label: "Método de pago", key: "metodoPago" },
  ],
  PAGO_REEMBOLSADO: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Código de pago", key: "codigoPago" },
    { label: "Reserva", key: "codigoReserva" },
    { label: "Monto", key: "monto", transform: (v) => `$${Number(v).toLocaleString("es-CO")}` },
  ],
  PQRS_CREADA: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Radicado", key: "radicado" },
    { label: "Tipo PQRS", key: "tipoPQRS" },
  ],
  PQRS_RESPONDIDA: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Radicado", key: "radicado" },
  ],
  ESTADIA_EXTENDIDA: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Código de reserva", key: "codigoReserva" },
    { label: "Nueva fecha de salida", key: "nuevaFechaSalida" },
  ],
  NO_SHOW: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Código de reserva", key: "codigoReserva" },
    { label: "Habitaciones", key: "habitaciones" },
  ],
  ALERTA_CLIENTE: [
    { label: "Cliente", key: "clienteNombre" },
    { label: "Asunto", key: "asunto" },
  ],
};

function getSecciones(
  tipo: NotificacionTipo,
  detalles: Record<string, unknown> | null,
): SeccionDetalle[] {
  if (!detalles) return [];
  const configs = SECCIONES_POR_TIPO[tipo] ?? [];
  return configs
    .map(({ label, key, transform }) => ({
      label,
      value:
        detalles[key] != null
          ? transform
            ? transform(detalles[key])
            : String(detalles[key])
          : "",
    }))
    .filter((s) => s.value !== "");
}

interface NotificationDetailDialogProps {
  notification: Notificacion | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onMarkAsRead: (id: number) => void;
}

export default function NotificationDetailDialog({
  notification,
  open,
  onOpenChange,
  onMarkAsRead,
}: NotificationDetailDialogProps) {
  const navigate = useNavigate();

  if (!notification) return null;

  const titulo = NOTIFICACION_TIPO_TITULO[notification.tipo];
  const color = NOTIFICACION_TIPO_COLOR[notification.tipo];
  const secciones = getSecciones(notification.tipo, notification.detalles);
  const navigateUrl = getNavigationUrl(notification);
  const origenBadge = getOrigenBadge(notification.origen);

  const handleNavigate = () => {
    if (navigateUrl) {
      if (!notification.leida) {
        onMarkAsRead(notification.id);
      }
      onOpenChange(false);
      navigate(navigateUrl);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg p-0 gap-0 overflow-hidden">
        <DialogHeader className="sr-only">
          <DialogTitle>{titulo}</DialogTitle>
          <DialogDescription>{notification.mensaje}</DialogDescription>
        </DialogHeader>
        <div className="flex items-start gap-4 p-6 pb-4">
          <span
            className={`flex size-12 shrink-0 items-center justify-center rounded-xl ${
              color.iconCircle
            }`}
          >
            {TYPE_ICON_LARGE[notification.tipo]}
          </span>
          <div className="flex-1 min-w-0">
            <h2 className="font-heading text-base text-foreground font-semibold leading-tight">
              {titulo}
            </h2>
            <p className="text-xs text-muted-foreground mt-1">
              {formatDate(notification.fechaCreacion)}
            </p>
            {origenBadge && (
              <span className="inline-flex items-center gap-1 mt-2 px-2 py-0.5 rounded-md bg-muted/60 text-[11px] font-medium text-muted-foreground/70 leading-none">
                {origenBadge.icon}
                {origenBadge.label}
                {notification.origenNombre && (
                  <> · {notification.origenNombre}</>
                )}
              </span>
            )}
          </div>
        </div>

        <div className="px-6 pb-4">
          <div className="rounded-lg bg-muted/30 p-4">
            <p className="text-sm text-foreground leading-relaxed whitespace-pre-line">
              {notification.mensaje}
            </p>
          </div>
        </div>

        {secciones.length > 0 && (
          <div className="px-6 pb-4">
            <div className="grid grid-cols-2 gap-x-6 gap-y-3 rounded-lg border border-border p-4">
              {secciones.map((s) => (
                <div key={s.label} className="space-y-0.5">
                  <p className="text-[11px] font-medium text-muted-foreground/60 uppercase tracking-wider">
                    {s.label}
                  </p>
                  <p className="text-sm text-foreground font-medium truncate">
                    {s.value}
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="flex items-center justify-between gap-3 px-6 py-4 border-t border-border bg-muted/20">
          <div className="flex items-center gap-2">
            {!notification.leida && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  onMarkAsRead(notification.id);
                }}
                className="gap-2"
              >
                <CheckCheck className="size-4" />
                Marcar como leída
              </Button>
            )}
            {navigateUrl && (
              <Button
                variant="hotel"
                size="sm"
                onClick={handleNavigate}
              >
                Ir al recurso
              </Button>
            )}
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onOpenChange(false)}
          >
            Cerrar
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
