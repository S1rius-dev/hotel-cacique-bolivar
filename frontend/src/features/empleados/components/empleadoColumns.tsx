import type { Column } from "@/components/ui/table/data-table/types";
import type { EmpleadoResponse } from "@/api/generated/openAPIDefinition.schemas";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { format } from "date-fns";

const CARGO_LABELS: Record<string, string> = {
  GERENTE: "Gerente",
  RECEPCIONISTA: "Recepcionista",
  LIMPIEZA: "Limpieza",
};

export const empleadoColumns: Column<EmpleadoResponse>[] = [
  {
    key: "nombres",
    header: "Nombres",
    className: "min-w-[120px]",
    render: (emp) => (
      <span className="font-medium">{emp.nombres} {emp.apellidos}</span>
    ),
  },
  {
    key: "cargo",
    header: "Cargo",
    className: "w-[130px]",
    render: (emp) => (
      <span className="inline-flex items-center rounded-full bg-violet-50 text-violet-700 border border-violet-200 dark:bg-violet-950 dark:text-violet-400 dark:border-violet-800 px-2.5 py-0.5 text-xs font-medium">
        {CARGO_LABELS[emp.cargo ?? ""] ?? emp.cargo}
      </span>
    ),
  },
  {
    key: "telefono",
    header: "Teléfono",
    className: "w-[120px] max-lg:hidden",
    render: (emp) => emp.telefono || "—",
  },
  {
    key: "activo",
    header: "Estado",
    className: "w-[90px]",
    render: (emp) => <StatusBadge active={emp.activo} />,
  },
  {
    key: "usuarioUsername",
    header: "Usuario",
    className: "w-[130px] max-lg:hidden",
    render: (emp) => emp.usuarioUsername || "—",
  },
  {
    key: "fechaIngreso",
    header: "Ingreso",
    className: "w-[110px] max-lg:hidden",
    render: (emp) => emp.fechaIngreso ? format(new Date(emp.fechaIngreso), "dd/MM/yyyy") : "—",
  },
];
