import { User, IdCard, AlertTriangle, CalendarDays, ExternalLink } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { TIPO_ALERTA_LABELS } from "../constants/disponibilidad.constants";
import type { HuespedResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDate } from "@/utils/date";

interface Props {
  huespedes: HuespedResponse[];
}

function AlertBadge({ tipo, mensaje }: { tipo?: string; mensaje?: string }) {
  const label =
    (tipo ? TIPO_ALERTA_LABELS[tipo] : undefined) ?? mensaje ?? tipo;
  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-medium bg-amber-50 text-amber-700 border border-amber-200 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-800">
      <AlertTriangle className="w-3 h-3" />
      {label}
    </span>
  );
}

function RoomHuespedesCard({ huespedes }: Props) {
  const navigate = useNavigate();
  if (huespedes.length === 0) {
    return (
      <div className="rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 p-3">
        <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground flex items-center gap-1.5 mb-2">
          <User className="w-3 h-3" />
          Huéspedes
        </p>
        <p className="text-sm text-muted-foreground">Sin huéspedes registrados</p>
      </div>
    );
  }

  return (
    <div className="rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 p-3 space-y-3">
      <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground flex items-center gap-1.5">
        <User className="w-3 h-3" />
        Huéspedes
        <span className="font-normal normal-case text-muted-foreground/60">
          ({huespedes.length})
        </span>
      </p>

      <div className="grid grid-cols-2 gap-3">
        {huespedes.map((h, i) => (
          <div
            key={h.huespedId ?? i}
            className="rounded-lg border bg-white dark:bg-zinc-950/40 p-3 space-y-2">
            <div className="flex items-center gap-2">
              <div className="p-1 rounded-full bg-zinc-100 dark:bg-zinc-800 shrink-0">
                <User className="w-4 h-4 text-zinc-500" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate">
                  {h.nombreCompleto}
                  {h.apellidoCompleto ? ` ${h.apellidoCompleto}` : ""}
                </p>
              </div>
              {h.huespedId != null && (
                <button
                  onClick={() => navigate(`/dashboard/clientes/${h.huespedId}`)}
                  className="p-1 rounded-md hover:bg-zinc-200 dark:hover:bg-zinc-700 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-300 transition-colors shrink-0"
                  title="Ver detalle del cliente"
                >
                  <ExternalLink className="w-3.5 h-3.5" />
                </button>
              )}
            </div>

            <div className="flex flex-wrap gap-x-3 gap-y-0.5 text-xs text-muted-foreground ml-9">
              <span className="flex items-center gap-1">
                <IdCard className="w-3 h-3" />
                {h.tipoIdentificacion ? h.tipoIdentificacion : "CC"} : {h.cedula}
              </span>
            </div>

            {h.alertas && h.alertas.length > 0 && (
              <div className="ml-9 space-y-1">
                <div className="flex items-center gap-1">
                  <AlertTriangle className="w-3 h-3 text-amber-500" />
                  <span className="text-[11px] font-medium text-amber-700 dark:text-amber-400">
                    Alertas
                  </span>
                </div>
                <div className="flex flex-wrap gap-1">
                  {h.alertas.map((a) => (
                    <AlertBadge key={a.id} tipo={a.tipo} mensaje={a.mensaje} />
                  ))}
                </div>
                {h.alertas.length === 1 && h.alertas[0].fechaCreacion && (
                  <p className="text-[10px] text-muted-foreground flex items-center gap-1">
                    <CalendarDays className="w-2.5 h-2.5" />
                    {formatDate(h.alertas[0].fechaCreacion)}
                  </p>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

export default RoomHuespedesCard;
