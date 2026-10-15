import type { Column } from "@/components/ui/table/data-table/types";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { formatDateTimeCompact } from "@/utils/date";

export const clientColumns: Column<ClienteResponse>[] = [
  {
    key: "cedula",
    header: "Cédula",
    className: "text-center font-medium tabular-nums",
    cellClassName: "truncate",
  },
  {
    key: "nombre",
    header: "Cliente",
    className: "font-medium",
    cellClassName: "truncate",
    render: (cliente) =>
      `${cliente.nombreCompleto} ${cliente.apellidoCompleto}`,
  },
  {
    key: "telefono",
    header: "Teléfono",
    className: "max-md:hidden",
    cellClassName: "truncate",
    render: (cliente) => cliente.telefono || "-",
  },
  {
    key: "reservas",
    header: "Reservas",
    className: "text-center max-md:hidden",
    render: (cliente) => (
      <span className="inline-flex items-center gap-1 rounded-full bg-sky-50 text-sky-700 border border-sky-200 dark:bg-sky-950 dark:text-sky-400 dark:border-sky-800 px-2.5 py-0.5 text-xs font-medium tabular-nums">
        {cliente.reservasIds?.length ?? 0}
      </span>
    ),
  },
  {
    key: "createdAt",
    header: "Creado",
    className: "text-center max-lg:hidden",
    render: (cliente) => formatDateTimeCompact(cliente.createdAt),
  },
  {
    key: "activo",
    header: "Estado",
    className: "text-center max-md:hidden",
    render: (cliente) => <StatusBadge active={cliente.activo} />,
  },
];
