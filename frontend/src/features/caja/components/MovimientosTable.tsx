import { useState } from "react";
import {
  FORMAT_CURRENCY,
  DetallePagoModal,
  usePagoPorId,
} from "@/features/pagos";
import { Button } from "@/components/ui/button";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { formatUsuario } from "@/features/turnos";
import type { MovimientoResponse } from "@/api/generated/openAPIDefinition.schemas";
import { ArrowUpRight, ArrowDownRight, Eye, AlertTriangle } from "lucide-react";
import { formatDate } from "@/utils/date";
import { extractErrorMessage } from "@/lib/extract-error";

const TIPO_COLORS = {
  ingreso:
    "text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/30 border-emerald-200 dark:border-emerald-900",
  egreso:
    "text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/30 border-red-200 dark:border-red-900",
} as const;

function TipoBadge({ row }: { row: MovimientoResponse }) {
  const isIngreso = row.tipo === "INGRESO" || row.tipo === "PAGO";
  const colors = isIngreso ? TIPO_COLORS.ingreso : TIPO_COLORS.egreso;
  const Icon = isIngreso ? ArrowUpRight : ArrowDownRight;
  return (
    <span
      className={`inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full font-medium border ${colors}`}>
      <Icon className="size-3" />
      {isIngreso ? "Ingreso" : "Gasto"}
    </span>
  );
}

function FechaCell({ fecha }: { fecha?: string }) {
  if (!fecha) return <span className="text-muted-foreground">-</span>;
  const d = new Date(fecha);
  return (
    <div>
      <p className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
        {formatDate(fecha)}
      </p>
      <p className="text-[10px] text-muted-foreground">
        {d.toLocaleTimeString("es-ES", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        })}
      </p>
    </div>
  );
}

function MontoCell({ monto, tipo }: { monto?: number; tipo?: string }) {
  const isIngreso = tipo === "INGRESO" || tipo === "PAGO";
  return (
    <span
      className={`text-xs font-bold tabular-nums ${isIngreso ? "text-emerald-600 dark:text-emerald-400" : "text-red-600 dark:text-red-400"}`}>
      {isIngreso ? "+" : "-"}
      {FORMAT_CURRENCY.format(monto ?? 0)}
    </span>
  );
}

function TurnoBadge({ turnoId }: { turnoId?: number | null }) {
  if (turnoId == null) {
    return (
      <span className="inline-flex items-center gap-1 text-[10px] px-1.5 py-0.5 rounded font-medium bg-amber-50 dark:bg-amber-950/30 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-900">
        <AlertTriangle className="size-2.5" />
        Sin turno
      </span>
    );
  }
  return (
    <span className="text-[11px] text-zinc-500 dark:text-zinc-400">
      #{turnoId}
    </span>
  );
}

interface MovimientosTableProps {
  movimientos: MovimientoResponse[];
  isLoading?: boolean;
  isError?: boolean;
  /** Error crudo de la query, para poder mostrar el mensaje del backend. */
  error?: unknown;
  titulo?: string;
  showTurnoColumn?: boolean;
  maxHeight?: string;
}

export default function MovimientosTable({
  movimientos,
  isLoading = false,
  isError = false,
  error,
  titulo = "Movimientos",
  showTurnoColumn = false,
  maxHeight = "max-h-75",
}: MovimientosTableProps) {
  const [pagoIdSeleccionado, setPagoIdSeleccionado] = useState<number | null>(
    null,
  );
  const sorted = [...movimientos].sort((a, b) => {
    const fa = a.fecha ? new Date(a.fecha).getTime() : 0;
    const fb = b.fecha ? new Date(b.fecha).getTime() : 0;
    return fb - fa;
  });

  if (isError) {
    return (
      <div className="rounded-xl border border-red-200 dark:border-red-900 bg-red-50/50 dark:bg-red-950/20 p-6 text-center text-sm text-red-600 dark:text-red-400">
        {extractErrorMessage(error, "Error al cargar los movimientos")}
      </div>
    );
  }

  return (
    <>
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
  .card-table .td-tipo { grid-column: 2; text-align: right !important; }
  .card-table .td-concepto { grid-column: 1 / -1; }
  .card-table .td-monto { grid-column: 1; }
  .card-table .td-usuario { grid-column: 2; }
  .card-table .td-obs { grid-column: 1; }
  .card-table .td-turno { grid-column: 2; text-align: right !important; }
  .card-table .td-detalle {
    grid-column: 2;
    text-align: right !important;
    justify-content: flex-end;
  }
}
      `}</style>
      <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 border-t-2 border-t-rose-800/70 bg-white dark:bg-zinc-900/40 overflow-clip">
        <div className="px-5 py-3 border-b border-zinc-100 dark:border-zinc-800">
          <div className="flex items-center gap-2">
            <span className="h-1.5 w-1.5 rounded-full bg-rose-800 animate-pulse shrink-0" />
            <h3 className="text-sm font-semibold text-zinc-800 dark:text-zinc-200">
              {titulo}
            </h3>
            {movimientos.length > 0 && (
              <span className="text-[11px] text-muted-foreground">
                ({movimientos.length} registro{movimientos.length !== 1 ? "s" : ""})
              </span>
            )}
          </div>
        </div>

        {isLoading ? (
          <div className="p-6 text-center text-sm text-muted-foreground">
            Cargando movimientos...
          </div>
        ) : movimientos.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-12 text-sm text-muted-foreground">
            <p className="font-medium text-zinc-500 dark:text-zinc-400">
              No hay movimientos registrados
            </p>
            <p className="text-xs mt-1">
              Los pagos y gastos aparecerán aquí
            </p>
          </div>
        ) : (
          <div className={`overflow-x-auto ${maxHeight} overflow-y-auto`}>
            <table className="w-full text-xs card-table">
              <thead className="sticky top-0 z-10 bg-white dark:bg-zinc-900">
                <tr className="border-b border-rose-200/60 dark:border-rose-900/40 bg-rose-50/40 dark:bg-rose-950/15">
                  <Th data-label="Fecha">Fecha</Th>
                  <Th data-label="Tipo">Tipo</Th>
                  <Th data-label="Concepto">Concepto</Th>
                  <Th data-label="Monto" className="text-right">Monto</Th>
                  <Th data-label="Usuario">Usuario</Th>
                  {showTurnoColumn && <Th data-label="Turno">Turno</Th>}
                  <Th data-label="Obs.">Obs.</Th>
                  <Th data-label="Detalle" className="text-right">Detalle</Th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100 dark:divide-zinc-800">
                {sorted.map((row) => {
                  const esPago = !!row.pagoId;
                  const sinTurno = row.turnoId == null;
                  return (
                    <tr
                      key={row.id}
                      onClick={() => esPago && setPagoIdSeleccionado(row.pagoId!)}
                      className={`transition-colors ${
                        sinTurno
                          ? "bg-amber-50/40 dark:bg-amber-950/15 hover:bg-amber-50/70 dark:hover:bg-amber-950/25"
                          : esPago
                            ? "cursor-pointer hover:bg-blue-50/60 dark:hover:bg-blue-950/20"
                            : "hover:bg-zinc-50/80 dark:hover:bg-zinc-900/40"
                      }`}>
                      <Td data-label="Fecha" className="td-fecha">
                        <FechaCell fecha={row.fecha} />
                      </Td>
                      <Td data-label="Tipo" className="td-tipo">
                        <TipoBadge row={row} />
                      </Td>
                      <Td data-label="Concepto" className="td-concepto">
                        <span className="text-xs font-medium text-zinc-700 dark:text-zinc-300">
                          {row.concepto ?? "-"}
                        </span>
                      </Td>
                      <Td data-label="Monto" className="td-monto">
                        <MontoCell monto={row.monto} tipo={row.tipo} />
                      </Td>
                      <Td data-label="Usuario" className="td-usuario">
                        <span className="text-xs text-zinc-500 dark:text-zinc-400">
                          {formatUsuario(row.usuario)}
                        </span>
                      </Td>
                      {showTurnoColumn && (
                        <Td data-label="Turno" className="td-turno">
                          <TurnoBadge turnoId={row.turnoId} />
                        </Td>
                      )}
                      <Td data-label="Obs." className="td-obs !whitespace-normal">
                        <span className="text-[11px] text-muted-foreground break-words">
                          {row.observaciones ?? "-"}
                        </span>
                      </Td>
                      <Td data-label="Detalle" className="td-detalle">
                        {esPago ? (
                          <TooltipProvider>
                            <Tooltip>
                              <TooltipTrigger asChild>
                                <Button
                                  type="button"
                                  variant="ghost"
                                  size="icon-sm"
                                  aria-label="Ver detalle del pago"
                                  className="text-zinc-500 hover:bg-blue-50 hover:text-blue-700 dark:text-zinc-400 dark:hover:bg-blue-950/30 dark:hover:text-blue-300 pointer-events-none">
                                  <Eye className="size-3.5" />
                                </Button>
                              </TooltipTrigger>
                              <TooltipContent>
                                Ver detalle del pago
                              </TooltipContent>
                            </Tooltip>
                          </TooltipProvider>
                        ) : (
                          <span className="text-muted-foreground">-</span>
                        )}
                      </Td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {pagoIdSeleccionado && (
        <PagoDetalleModalContent
          pagoId={pagoIdSeleccionado}
          onClose={() => setPagoIdSeleccionado(null)}
        />
      )}
    </>
  );
}

function PagoDetalleModalContent({
  pagoId,
  onClose,
}: {
  pagoId: number;
  onClose: () => void;
}) {
  const { data: pago } = usePagoPorId(pagoId);
  if (!pago) return null;
  return <DetallePagoModal open pago={pago} onClose={onClose} />;
}

function Th({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <th
      className={`text-left font-semibold text-zinc-500 dark:text-zinc-400 px-4 py-2.5 whitespace-nowrap ${className ?? ""}`}>
      {children}
    </th>
  );
}

function Td({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <td className={`px-4 py-2.5 whitespace-nowrap ${className ?? ""}`}>
      {children}
    </td>
  );
}
