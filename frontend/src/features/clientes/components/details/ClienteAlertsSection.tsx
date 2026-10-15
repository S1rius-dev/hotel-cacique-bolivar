import { AlertTriangle, CheckCircle, XCircle, Calendar, ShieldAlert } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import type { AlertaResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDateTimeFull } from "@/utils/date";

interface Props {
  alertas: AlertaResponse[];
  isLoading?: boolean;
}

const tipoLabels: Record<string, string> = {
  ECONOMICO: "Económico",
  HURTO: "Hurto",
  CONFLICTIVO: "Conflictivo",
  FUMADOR: "Fumador",
  DANOS: "Daños",
};

function ClienteAlertsSection({ alertas = [], isLoading }: Props) {
  if (isLoading) {
    return (
      <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-amber-500">
        <div className="flex items-center gap-2 text-amber-600">
          <AlertTriangle className="w-4 h-4" />
          <p className="text-sm">Cargando alertas...</p>
        </div>
      </section>
    );
  }

  if (alertas.length === 0) {
    return null;
  }

  return (
    <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-amber-500">
      <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
        <ShieldAlert className="h-5 w-5 text-amber-600" />
        Alertas del cliente
        <Badge
          variant="outline"
          className="text-xs ml-1 border-amber-300 text-amber-700 dark:border-amber-600 dark:text-amber-400"
        >
          {alertas.length} alerta{alertas.length !== 1 ? "s" : ""}
        </Badge>
      </h3>

      <div className="space-y-3">
        {alertas.map((alerta) => (
          <div
            key={alerta.id}
            className="flex items-start gap-3 p-3 rounded-lg bg-white dark:bg-zinc-900 border border-amber-200 dark:border-amber-800/30 shadow-sm"
          >
            <div
              className={`p-2 rounded-md ${
                alerta.estado
                  ? "bg-rose-100 text-rose-600 dark:bg-rose-900/30 dark:text-rose-400"
                  : "bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400"
              }`}>
              {alerta.estado ? (
                <XCircle className="w-4 h-4" />
              ) : (
                <CheckCircle className="w-4 h-4" />
              )}
            </div>

            <div className="flex-1 min-w-0 space-y-2">
              <div className="flex items-center justify-between gap-2">
                <Badge
                  variant={
                    alerta.tipo === "ECONOMICO"
                      ? "default"
                      : alerta.tipo === "HURTO" || alerta.tipo === "CONFLICTIVO"
                      ? "destructive"
                      : "secondary"
                  }
                  className="text-xs">
                  {tipoLabels[alerta.tipo || ""] || alerta.tipo}
                </Badge>
                <Badge
                  variant={alerta.estado ? "destructive" : "default"}
                  className="text-xs">
                  {alerta.estado ? "Activa" : "Inactiva"}
                </Badge>
              </div>

              <p className="text-sm text-zinc-700 dark:text-zinc-300 leading-relaxed break-words">
                {alerta.mensaje}
              </p>

              {alerta.fechaCreacion && (
                <div className="flex items-center gap-1 text-xs text-muted-foreground">
                  <Calendar className="w-3 h-3" />
                  {formatDateTimeFull(alerta.fechaCreacion)}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default ClienteAlertsSection;
