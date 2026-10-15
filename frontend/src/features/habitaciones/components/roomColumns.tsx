import type { Column } from "@/components/ui/table/data-table/types";
import type {
  HabitacionResponse,
  TipoHabitacionResponse,
} from "@/api/generated/openAPIDefinition.schemas";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { HABITACIONES_STATUS_CONFIG } from "../config/estados";
import { formatCurrency } from "@/utils/format";

export const roomColumns: Column<HabitacionResponse>[] = [
  {
    key: "numero",
    header: "Número",
    className: "text-center font-medium",
  },
  {
    key: "tipo",
    header: "Tipo",
    className: "text-center",
    cellClassName: "truncate",
    render: (row) => row.tipo?.nombre ?? "-",
  },
  {
    key: "estado",
    header: "Estado",
    className: "text-center",
    render: (row) => <StatusBadge status={row.estado as string} configMap={HABITACIONES_STATUS_CONFIG} />,
  },
  {
    key: "precio",
    header: "Precio",
    className: "text-right font-semibold max-md:hidden",
    render: (row) =>
      `${formatCurrency(row.tipo?.precioBase ?? 0)} COP`,
  },
  {
    key: "capacidad",
    header: "Capacidad",
    className: "text-center max-md:hidden",
    render: (row) => `${row.tipo?.capacidadMaxima ?? 0} Personas`,
  },
];

export const typeRoomColumns: Column<TipoHabitacionResponse>[] = [
  {
    key: "nombre",
    header: "Nombre",
    className: "font-medium",
    cellClassName: "truncate",
  },
  {
    key: "descripcion",
    header: "Descripción",
    className: "max-lg:hidden",
    cellClassName: "truncate",
  },
  {
    key: "capacidadMaxima",
    header: "Capacidad Máxima",
    className: "text-center",
    render: (row) => `${row.capacidadMaxima ?? 0} Personas`,
  },
  {
    key: "precioBase",
    header: "Precio Base",
    className: "text-right max-md:hidden",
    render: (row) =>
      `${formatCurrency(row.precioBase ?? 0)} COP`,
  },
  {
    key: "precioPersonaExtra",
    header: "Persona Extra",
    className: "text-right max-lg:hidden",
    render: (row) =>
      `${formatCurrency(row.precioPersonaExtra ?? 0)} COP`,
  },
];

