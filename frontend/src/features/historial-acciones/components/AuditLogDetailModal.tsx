import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { formatDateTimeCompact } from "@/utils/date";
import type { AuditLogResponse } from "@/api/generated/openAPIDefinition.schemas";
import {
  PlusCircle, Pencil, Trash2, XCircle, LogIn, LogOut, UserPlus, CreditCard, History, ScrollText, ClockArrowDown, UserX, SprayCan,
} from "lucide-react";

const ACTION_ICONS: Record<string, React.ReactNode> = {
  CREATE: <PlusCircle className="w-5 h-5 text-emerald-600" />,
  UPDATE: <Pencil className="w-5 h-5 text-amber-600" />,
  DELETE: <Trash2 className="w-5 h-5 text-red-600" />,
  CANCELLED: <XCircle className="w-5 h-5 text-rose-600" />,
  CHECK_IN: <LogIn className="w-5 h-5 text-sky-600" />,
  CHECK_OUT: <LogOut className="w-5 h-5 text-violet-600" />,
  CHECK_OUT_AUTO: <ClockArrowDown className="w-5 h-5 text-zinc-500" />,
  NO_SHOW_AUTO: <UserX className="w-5 h-5 text-amber-600" />,
  CLEANED: <SprayCan className="w-5 h-5 text-sky-600" />,
  CLIENT_REGISTERED: <UserPlus className="w-5 h-5 text-emerald-600" />,
  PAYMENT_REGISTERED: <CreditCard className="w-5 h-5 text-blue-600" />,
};

const ACTION_LABELS: Record<string, string> = {
  CREATE: "Creación",
  UPDATE: "Actualización",
  DELETE: "Eliminación",
  CANCELLED: "Cancelación",
  CHECK_IN: "Check-In",
  CHECK_OUT: "Check-Out",
  CHECK_OUT_AUTO: "Check-Out Automático",
  NO_SHOW_AUTO: "No Llegó",
  CLEANED: "Limpieza",
  CLIENT_REGISTERED: "Registro Cliente",
  PAYMENT_REGISTERED: "Pago Registrado",
};

const ENTITY_LABELS: Record<string, string> = {
  Reserva: "Reserva",
  Habitacion: "Habitación",
  Cliente: "Cliente",
  User: "Usuario",
  Empleado: "Empleado",
  Pago: "Pago",
  Alerta: "Alerta",
  TipoHabitacion: "Tipo Habitación",
};

interface Props {
  log: AuditLogResponse | null;
  open: boolean;
  onClose: () => void;
}

function AuditLogDetailModal({ log, open, onClose }: Props) {
  if (!log) return null;

  const icon = log.action ? ACTION_ICONS[log.action] : <History className="w-5 h-5 text-zinc-400" />;
  const actionLabel = log.action ? (ACTION_LABELS[log.action] ?? log.action) : "Desconocido";
  const entityLabel = log.entity ? (ENTITY_LABELS[log.entity] ?? log.entity) : "-";

  const detalleLines = log.detalle ? log.detalle.split("\n").filter(Boolean) : [];

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-xl max-h-[85vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <ScrollText className="w-5 h-5 text-zinc-600" />
            Detalle de la acción
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-5">
          {/* Header badge */}
          <div className="flex items-center justify-between p-4 rounded-xl bg-zinc-50 dark:bg-zinc-900/50 border">
            <div className="flex items-center gap-3">
              <div className="size-10 rounded-full bg-white dark:bg-zinc-800 border flex items-center justify-center">
                {icon}
              </div>
              <div>
                <p className="font-semibold text-zinc-900 dark:text-white">{actionLabel}</p>
                <span className="inline-flex items-center rounded-full bg-zinc-100 text-zinc-700 border border-zinc-200 dark:bg-zinc-800 dark:text-zinc-300 dark:border-zinc-700 px-2.5 py-0.5 text-xs font-medium mt-0.5">
                  {entityLabel}
                </span>
              </div>
            </div>
            {log.entityId != null && (
              <span className="text-xs font-mono text-muted-foreground">ID #{log.entityId}</span>
            )}
          </div>

          {/* Meta info */}
          <div className="grid grid-cols-2 gap-4">
            <div className="p-3 rounded-lg border bg-card">
              <p className="text-[10px] uppercase tracking-widest text-muted-foreground mb-1">Usuario</p>
              <p className="text-sm font-medium">{log.usuarioUsername ?? log.usuarioEmail ?? "-"}</p>
            </div>
            <div className="p-3 rounded-lg border bg-card">
              <p className="text-[10px] uppercase tracking-widest text-muted-foreground mb-1">Fecha</p>
              <p className="text-sm font-medium">{formatDateTimeCompact(log.fecha)}</p>
            </div>
          </div>

          {/* Full detail */}
          <div>
            <p className="text-xs font-medium text-muted-foreground mb-2 uppercase tracking-wider">
              Detalle completo
            </p>
            <div className="rounded-xl border bg-zinc-50 dark:bg-zinc-900/40 p-4">
              {detalleLines.length > 0 ? (
                <div className="space-y-2">
                  {detalleLines.map((line, i) => {
                    const [label, ...rest] = line.split(": ");
                    const value = rest.join(": ");
                    const isHeader = !line.includes(": ") && i === 0;
                    return isHeader ? (
                      <p key={i} className="text-sm font-semibold text-zinc-900 dark:text-white">
                        {line}
                      </p>
                    ) : (
                      <div key={i} className="flex items-start gap-2 text-sm">
                        <span className="text-muted-foreground shrink-0 min-w-[100px] font-medium">
                          {label}:
                        </span>
                        <span className="text-zinc-800 dark:text-zinc-200">{value}</span>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground italic">-</p>
              )}
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export default AuditLogDetailModal;
