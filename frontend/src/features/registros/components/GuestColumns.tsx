import { Eye } from "lucide-react";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Badge } from "@/components/ui/badge";
import type { Column } from "@/components/ui/table/data-table/types";
import type { ActionItem } from "@/components/ui/table/data-table/DataTableActions";
import { formatDateShort } from "@/utils/date";
import type { HuespedRegistroDTO } from "@/api/generated/openAPIDefinition.schemas";
import { RECORD_STATUS_CONFIG } from "./constants";

import { GuestAvatar } from "./GuestAvatar";

export function getColumns(): Column<HuespedRegistroDTO>[] {
  return [
    {
      key: "fechaCheckIn",
      header: "Check-In",
      render: (row) => (
        <span className="text-sm font-medium">
          {row.fechaCheckIn ? formatDateShort(row.fechaCheckIn) : "—"}
        </span>
      ),
    },
    {
      key: "fechaCheckOut",
      header: "Check-Out",
      render: (row) => (
        <span className="text-sm font-medium">
          {row.fechaCheckOut ? formatDateShort(row.fechaCheckOut) : "—"}
        </span>
      ),
      className: "max-md:hidden",
      cellClassName: "max-md:hidden",
    },
    {
      key: "nombreCompleto",
      header: "Huésped",
      render: (row) => (
        <div className="flex items-center gap-3 min-w-0">
          <GuestAvatar
            name={`${row.nombreCompleto ?? ""} ${row.apellidoCompleto ?? ""}`}
          />
          <div className="min-w-0">
            <p className="truncate text-sm font-medium">
              {row.nombreCompleto} {row.apellidoCompleto}
            </p>
            <p className="truncate text-xs text-muted-foreground">
              {row.cedula}
            </p>
          </div>
        </div>
      ),
    },
    {
      key: "numeroHabitacion",
      header: "Habitación",
      render: (row) => (
        <Badge variant="outline" className="font-mono text-xs">
          {row.numeroHabitacion}
        </Badge>
      ),
    },
    {
      key: "tipoHabitacion",
      header: "Tipo",
      render: (row) => (
        <span className="text-sm text-muted-foreground">
          {row.tipoHabitacion}
        </span>
      ),
      className: "max-md:hidden",
      cellClassName: "max-md:hidden",
    },
    {
      key: "codigoReserva",
      header: "Reserva",
      render: (row) => (
        <span className="text-sm font-mono text-muted-foreground">
          {row.codigoReserva}
        </span>
      ),
      className: "max-lg:hidden",
      cellClassName: "max-lg:hidden",
    },
    {
      key: "estadoDetalle",
      header: "Estado",
      render: (row) => (
        <StatusBadge
          status={row.estadoDetalle ?? "RESERVADA"}
          configMap={RECORD_STATUS_CONFIG}
        />
      ),
    },
  ];
}

export function getActions(
  onViewDetail: (row: HuespedRegistroDTO) => void,
): ActionItem<HuespedRegistroDTO>[] {
  return [
    {
      label: "Ver detalle",
      onClick: onViewDetail,
      icon: Eye,
    },
  ];
}
