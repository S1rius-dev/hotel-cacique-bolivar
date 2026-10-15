import { useState } from "react";
import { useHistorialTurnos } from "../hooks/useHistorialTurnos";
import {
  formatHoraAMPM,
  turnoDesdeDTO,
  formatUsuario,
} from "../constants/turno.constants";
import type { CajaNombre } from "../constants/turno.constants";
import { FORMAT_CURRENCY } from "@/features/pagos";
import type { TurnoResponse } from "@/api/generated/openAPIDefinition.schemas";
import { Clock, ChevronLeft, ChevronRight } from "lucide-react";
import { formatDate } from "@/utils/date";
import { extractErrorMessage } from "@/lib/extract-error";

function FechaCell({ t }: { t: TurnoResponse }) {
  const fecha = t.fechaApertura ? formatDate(t.fechaApertura) : "-";
  const horaApertura = t.fechaApertura
    ? formatHoraAMPM(
        new Date(t.fechaApertura).toLocaleTimeString("es-ES", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }),
      )
    : "";
  const horaCierre = t.fechaCierre
    ? formatHoraAMPM(
        new Date(t.fechaCierre).toLocaleTimeString("es-ES", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        }),
      )
    : "";
  return (
    <div>
      <div className="text-xs font-medium text-zinc-700 dark:text-zinc-300">{fecha}</div>
      <div className="text-[10px] text-muted-foreground">
        {horaApertura}
        {horaCierre && ` → ${horaCierre}`}
      </div>
    </div>
  );
}

function TurnoCell({ t }: { t: TurnoResponse }) {
  const info = turnoDesdeDTO(t);
  return (
    <span className="inline-flex items-center gap-1 text-xs text-zinc-700 dark:text-zinc-300">
      <Clock className="size-3 text-muted-foreground" />
      {info?.info.label ?? t.tipoTurnoDisplay ?? "-"}
    </span>
  );
}

function EstatusCell({ t }: { t: TurnoResponse }) {
  return (
    <span
      className={`inline-flex items-center gap-1 text-[10px] px-2 py-0.5 rounded-full font-medium ${
        t.estado === "ABIERTO"
          ? "bg-emerald-50 dark:bg-emerald-950/30 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-900"
          : "bg-zinc-100 dark:bg-zinc-800 text-zinc-500 dark:text-zinc-400 border border-zinc-200 dark:border-zinc-700"
      }`}>
      {t.estado === "ABIERTO" ? "Abierto" : "Cerrado"}
    </span>
  );
}

function DiferenciaCell({ diff, contadoFinal }: { diff: number; contadoFinal?: number | null }) {
  if (contadoFinal == null) return <span className="text-xs font-mono text-zinc-500">-</span>;
  return (
    <span
      className={`text-xs font-mono ${
        diff > 0
          ? "text-emerald-600 dark:text-emerald-400"
          : diff < 0
            ? "text-red-600 dark:text-red-400"
            : "text-zinc-500 dark:text-zinc-400"
      }`}>
      {diff > 0 ? "+" : ""}{FORMAT_CURRENCY.format(diff)}
    </span>
  );
}

interface HistorialTurnosTableProps {
  caja?: CajaNombre;
  onTurnoSelect?: (turno: TurnoResponse) => void;
}

export default function HistorialTurnosTable({ caja = "RECEPCION", onTurnoSelect }: HistorialTurnosTableProps) {
  const [page, setPage] = useState(0);

  const { data, isLoading, isError, error } = useHistorialTurnos(caja, {
    page,
    size: 15,
    sort: ["fechaApertura,desc"],
  });

  const turnos = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  return (
    <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 border-t-2 border-t-rose-800/70 bg-white dark:bg-zinc-900/40 overflow-clip">
      <style>{`
@media (width < 1024px) {
  .card-table thead { display: none; }
  .card-table tbody tr {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.375rem 1rem;
    padding: 1rem;
    margin-bottom: 0.75rem;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    background: var(--color-card);
  }
  .card-table tbody td {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
    padding: 0;
    border: none;
    white-space: normal !important;
    text-align: left !important;
  }
  .card-table tbody td::before {
    content: attr(data-label);
    font-size: 0.6875rem;
    font-weight: 600;
    color: var(--color-muted-foreground);
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }
  .card-table .td-turno { grid-column: 2; text-align: right !important; }
  .card-table .td-estado { grid-column: 2; text-align: right !important; }
  .card-table .td-fondo { grid-column: 1; }
  .card-table .td-ingresos { grid-column: 2; text-align: right !important; }
  .card-table .td-gastos { grid-column: 1; }
  .card-table .td-esperado { grid-column: 2; text-align: right !important; }
  .card-table .td-contado { grid-column: 1; }
  .card-table .td-dif { grid-column: 2; text-align: right !important; }
}
      `}</style>
      <div className="flex items-center justify-between px-5 py-3 border-b border-zinc-100 dark:border-zinc-800">
        <div className="flex items-center gap-2">
          <span className="h-1.5 w-1.5 rounded-full bg-rose-800 animate-pulse shrink-0" />
          <h3 className="text-sm font-semibold text-zinc-800 dark:text-zinc-200">
            Historial de turnos
          </h3>
          {totalElements > 0 && (
            <span className="text-[11px] text-muted-foreground">
              ({totalElements} registro{totalElements !== 1 ? "s" : ""})
            </span>
          )}
        </div>
      </div>

      {isLoading ? (
        <div className="p-8 text-center text-sm text-muted-foreground">
          Cargando historial...
        </div>
      ) : isError ? (
        <div className="px-5 py-6 text-center text-sm text-red-600 dark:text-red-400">
          {extractErrorMessage(error, "Error al cargar el historial")}
        </div>
      ) : turnos.length === 0 ? (
        <div className="px-5 py-12 text-center text-sm text-muted-foreground">
          No hay turnos registrados
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-xs card-table" style={{ tableLayout: "fixed" }}>
            <colgroup>
              <col style={{ width: "13%" }} />
              <col style={{ width: "10%" }} />
              <col style={{ width: "12%" }} />
              <col style={{ width: "9%" }} />
              <col style={{ width: "10%" }} />
              <col style={{ width: "11%" }} />
              <col style={{ width: "10%" }} />
              <col style={{ width: "11%" }} />
              <col style={{ width: "11%" }} />
              <col style={{ width: "13%" }} />
            </colgroup>
            <thead>
              <tr className="border-b border-rose-200/60 dark:border-rose-900/40 bg-rose-50/40 dark:bg-rose-950/15">
                <Th data-label="Fecha">Fecha</Th>
                <Th data-label="Turno">Turno</Th>
                <Th data-label="Usuario">Usuario</Th>
                <Th data-label="Estado">Estado</Th>
                <Th data-label="Fondo" align="right">Fondo</Th>
                <Th data-label="Ingresos" align="right">Ingresos</Th>
                <Th data-label="Gastos" align="right">Gastos</Th>
                <Th data-label="Esperado" align="right">Esperado</Th>
                <Th data-label="Contado" align="right">Contado</Th>
                <Th data-label="Dif." align="right">Dif.</Th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
              {turnos.map((t) => {
                const diff = t.diferencia ?? 0;
                return (
                  <tr
                    key={t.id}
                    onClick={() => onTurnoSelect?.(t)}
                    className="hover:bg-zinc-50 dark:hover:bg-zinc-900/40 transition-colors cursor-pointer">
                    <Td data-label="Fecha" className="td-fecha"><FechaCell t={t} /></Td>
                    <Td data-label="Turno" className="td-turno"><TurnoCell t={t} /></Td>
                    <Td data-label="Usuario" className="td-usuario">
                      <span className="text-xs text-zinc-600 dark:text-zinc-400">
                        {formatUsuario(t.usuarioApertura)}
                      </span>
                    </Td>
                    <Td data-label="Estado" className="td-estado"><EstatusCell t={t} /></Td>
                    <Td data-label="Fondo" className="td-fondo" align="right">
                      <span className="text-xs font-mono text-zinc-700 dark:text-zinc-300">
                        {FORMAT_CURRENCY.format(t.fondoInicial ?? 0)}
                      </span>
                    </Td>
                    <Td data-label="Ingresos" className="td-ingresos" align="right">
                      <span className="text-xs font-mono text-emerald-600 dark:text-emerald-400">
                        {FORMAT_CURRENCY.format(t.totalIngresosEfectivo ?? 0)}
                      </span>
                    </Td>
                    <Td data-label="Gastos" className="td-gastos" align="right">
                      <span className="text-xs font-mono text-red-600 dark:text-red-400">
                        {FORMAT_CURRENCY.format(t.totalEgresos ?? 0)}
                      </span>
                    </Td>
                    <Td data-label="Esperado" className="td-esperado" align="right">
                      <span className="text-xs font-mono text-zinc-700 dark:text-zinc-300">
                        {FORMAT_CURRENCY.format(t.esperadoCierre ?? 0)}
                      </span>
                    </Td>
                    <Td data-label="Contado" className="td-contado" align="right">
                      <span className="text-xs font-mono text-zinc-700 dark:text-zinc-300">
                        {t.contadoFinal != null ? FORMAT_CURRENCY.format(t.contadoFinal) : "-"}
                      </span>
                    </Td>
                    <Td data-label="Dif." className="td-dif" align="right">
                      <DiferenciaCell diff={diff} contadoFinal={t.contadoFinal} />
                    </Td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex items-center justify-between px-5 py-2.5 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
          <span className="h-1 w-1 rounded-full bg-rose-800/50" />
          <span className="text-[11px] text-muted-foreground">
            Página {page + 1} de {totalPages}
          </span>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="p-1.5 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
              <ChevronLeft className="size-4" />
            </button>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="p-1.5 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:cursor-not-allowed transition-colors">
              <ChevronRight className="size-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function Th({ children, className, align = "left" }: { children: React.ReactNode; className?: string; align?: "left" | "right" }) {
  return (
    <th
      align={align}
      className={`font-semibold text-zinc-500 dark:text-zinc-400 px-3 py-2.5 whitespace-nowrap ${className ?? ""}`}>
      {children}
    </th>
  );
}

function Td({ children, className, align = "left" }: { children: React.ReactNode; className?: string; align?: "left" | "right" }) {
  return (
    <td
      align={align}
      className={`px-3 py-2.5 whitespace-nowrap ${className ?? ""}`}>
      {children}
    </td>
  );
}
