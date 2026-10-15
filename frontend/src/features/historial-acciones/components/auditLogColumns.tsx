import type { Column } from "@/components/ui/table/data-table/types";
import type { AuditLogResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDateTimeCompact } from "@/utils/date";
import {
  PlusCircle,
  Pencil,
  Trash2,
  XCircle,
  LogIn,
  LogOut,
  UserPlus,
  CreditCard,
  History,
  ClockArrowDown,
  UserX,
  SprayCan,
} from "lucide-react";

const ACTION_ICONS: Record<string, React.ReactNode> = {
  CREATE: <PlusCircle className="w-4 h-4 text-emerald-600" />,
  UPDATE: <Pencil className="w-4 h-4 text-amber-600" />,
  DELETE: <Trash2 className="w-4 h-4 text-red-600" />,
  CANCELLED: <XCircle className="w-4 h-4 text-rose-600" />,
  CHECK_IN: <LogIn className="w-4 h-4 text-sky-600" />,
  CHECK_OUT: <LogOut className="w-4 h-4 text-violet-600" />,
  CHECK_OUT_AUTO: <ClockArrowDown className="w-4 h-4 text-zinc-500" />,
  NO_SHOW_AUTO: <UserX className="w-4 h-4 text-amber-600" />,
  CLEANED: <SprayCan className="w-4 h-4 text-sky-600" />,
  CLIENT_REGISTERED: <UserPlus className="w-4 h-4 text-emerald-600" />,
  PAYMENT_REGISTERED: <CreditCard className="w-4 h-4 text-blue-600" />,
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

function ActionCell({ action }: { action?: string }) {
  const icon = action ? (
    ACTION_ICONS[action]
  ) : (
    <History className="w-4 h-4 text-zinc-400" />
  );
  const label = action ? (ACTION_LABELS[action] ?? action) : "Desconocido";

  return (
    <span className="inline-flex items-center gap-2">
      {icon}
      <span className="text-sm font-medium">{label}</span>
    </span>
  );
}

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

function normalizeEntity(entity?: string) {
  if (!entity) return "-";
  return ENTITY_LABELS[entity] ?? entity;
}

export const auditLogColumns: Column<AuditLogResponse>[] = [
  {
    key: "action",
    header: "Acción",
    className: "w-[140px]",
    render: (log) => <ActionCell action={log.action} />,
  },
  {
    key: "entity",
    header: "Entidad",
    className: "w-[130px] text-center",
    render: (log) => (
      <span className="inline-flex items-center rounded-full bg-zinc-100 text-zinc-700 border border-zinc-200 dark:bg-zinc-800 dark:text-zinc-300 dark:border-zinc-700 px-2.5 py-0.5 text-xs font-medium">
        {normalizeEntity(log.entity)}
      </span>
    ),
  },
  {
    key: "entityId",
    header: "ID",
    className: "w-[60px] text-center font-mono text-xs max-md:hidden",
    render: (log) => log.entityId ?? "-",
  },
  {
    key: "usuarioUsername",
    header: "Usuario",
    className: "w-[140px] text-center max-lg:hidden",
    render: (log) => log.usuarioUsername ?? log.usuarioEmail ?? "-",
  },
  {
    key: "fecha",
    header: "Fecha",
    className: "w-[180px] text-center text-xs max-lg:hidden",
    render: (log) => formatDateTimeCompact(log.fecha),
  },
];
