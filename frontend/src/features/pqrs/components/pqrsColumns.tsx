import type { Column } from "@/components/ui/table/data-table/types";
import type { PQRSResponse } from "../types";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { PQRS_STATUS_CONFIG } from "../config/estados";
import { formatDateTimeCompact } from "@/utils/date";

const TIPO_LABELS: Record<string, string> = {
  QUEJA: "Queja",
  PETICION: "Petición",
  RECLAMO: "Reclamo",
  SUGERENCIA: "Sugerencia",
  FELICITACION: "Felicitación",
  REPORTE_TECNICO: "Reporte Técnico",
};

const PRIORIDAD_COLORS: Record<string, string> = {
  BAJA: "bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950 dark:text-emerald-400 dark:border-emerald-800",
  MEDIA: "bg-sky-50 text-sky-700 border-sky-200 dark:bg-sky-950 dark:text-sky-400 dark:border-sky-800",
  ALTA: "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950 dark:text-amber-400 dark:border-amber-800",
  URGENTE: "bg-red-50 text-red-700 border-red-200 dark:bg-red-950 dark:text-red-400 dark:border-red-800",
};

export const pqrsColumns: Column<PQRSResponse>[] = [
  {
    key: "codigo",
    header: "Radicado",
    className: "w-[140px]",
    render: (pqrs) => (
      <span className="font-mono text-xs font-semibold text-rose-700">
        {pqrs.codigo}
      </span>
    ),
  },
  {
    key: "tipo",
    header: "Tipo",
    className: "w-[110px]",
    render: (pqrs) => (
      <span className="text-sm">{TIPO_LABELS[pqrs.tipo] ?? pqrs.tipo}</span>
    ),
  },
  {
    key: "prioridad",
    header: "Prioridad",
    className: "w-[90px] text-center max-lg:hidden",
    render: (pqrs) => (
      <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium border ${PRIORIDAD_COLORS[pqrs.prioridad] ?? ""}`}>
        {pqrs.prioridad}
      </span>
    ),
  },
  {
    key: "estado",
    header: "Estado",
    className: "w-[120px]",
    render: (pqrs) => <StatusBadge status={pqrs.estado} configMap={PQRS_STATUS_CONFIG} />,
  },
  {
    key: "asunto",
    header: "Asunto",
    className: "min-w-[150px] max-w-[250px]",
    render: (pqrs) => (
      <span className="text-sm truncate block">{pqrs.asunto || "—"}</span>
    ),
  },
  {
    key: "nombreContacto",
    header: "Contacto",
    className: "w-[140px] max-lg:hidden",
    render: (pqrs) => pqrs.nombreContacto || pqrs.usuarioNombre || "—",
  },
  {
    key: "fechaCreacion",
    header: "Fecha",
    className: "w-[150px] max-lg:hidden",
    render: (pqrs) => formatDateTimeCompact(pqrs.fechaCreacion),
  },
];
