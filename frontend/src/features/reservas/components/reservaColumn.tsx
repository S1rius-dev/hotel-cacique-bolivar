import type { ReservaResponse } from "@/api/generated/openAPIDefinition.schemas";
import type { Column } from "@/components/ui/table/data-table/types";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { RESERVAS_STATUS_CONFIG } from "../config/estados";
import { formatDateTimeCompact, formatDate } from "@/utils/date";
import { formatCurrency } from "@/utils/format";

export const reservaColumn: Column<ReservaResponse>[] = [
  {
    key: "codigoReserva",
    header: "Código",
    className: "font-medium",
    cellClassName: "truncate",
  },
  {
    key: "titularReservaNombre",
    header: "Cliente",
    className: "font-medium",
    cellClassName: "truncate",
    render: (reserva) =>
      `${reserva.titular?.nombreCompleto ?? ""} ${reserva.titular?.apellidoCompleto ?? ""}`,
  },
  {
    key: "fechaCheckIn",
    header: "Check-In",
    className: "max-md:hidden",
    render: (reserva) => formatDate(reserva.fechaCheckIn),
  },
  {
    key: "fechaCheckOut",
    header: "Check-Out",
    className: "max-md:hidden",
    render: (reserva) => formatDate(reserva.fechaCheckOut),
  },
  {
    key: "fechaCreacion",
    header: "Creada",
    className: "max-lg:hidden",
    render: (reserva) => formatDateTimeCompact(reserva.fechaCreacion),
  },
  {
    key: "noches",
    header: "Noches",
    className: "text-center max-lg:hidden",
  },
  {
    key: "total",
    header: "Total",
    className: "text-right font-semibold tabular-nums max-md:hidden",
    render: (reserva) =>
      formatCurrency(reserva.total),
  },
  {
    key: "estado",
    header: "Estado",
    className: "text-center",
    render: (reserva) => (
      <StatusBadge status={reserva.estado as string} configMap={RESERVAS_STATUS_CONFIG} />
    ),
  },
];
