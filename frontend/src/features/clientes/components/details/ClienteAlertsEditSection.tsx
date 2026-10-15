import { AlertTriangle, CheckCircle, XCircle, Save } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useState } from "react";
import { useAlertasPorCliente } from "@/features/alertas/hooks/useAlertasPorCliente";
import { useUpdateAlerta } from "@/features/alertas/hooks/useUpdateAlerta";
import type { AlertaResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  clienteId: number;
}

const tipoLabels: Record<string, string> = {
  ECONOMICO: "Económico",
  HURTO: "Hurto",
  CONFLICTIVO: "Conflictivo",
  FUMADOR: "Fumador",
  DANOS: "Daños",
};

function AlertEditItem({
  alerta,
  onSave,
  isPending,
}: {
  alerta: AlertaResponse;
  onSave: (estado: boolean) => void;
  isPending: boolean;
}) {
  const [estado, setEstado] = useState(alerta.estado ?? false);
  const [hasChanged, setHasChanged] = useState(false);

  const handleEstadoChange = (newEstado: boolean) => {
    setEstado(newEstado);
    setHasChanged(newEstado !== (alerta.estado ?? false));
  };

  return (
    <div className="flex items-start gap-3 p-4 rounded-xl bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800 shadow-sm">
      <div
        className={`p-2 rounded-md shrink-0 ${
          estado
            ? "bg-rose-100 text-rose-600 dark:bg-rose-900/30 dark:text-rose-400"
            : "bg-emerald-100 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400"
        }`}>
        {estado ? (
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
            variant={estado ? "destructive" : "outline"}
            className="text-xs">
            {estado ? "Activa" : "Inactiva"}
          </Badge>
        </div>

        <p className="text-sm text-zinc-700 dark:text-zinc-300 leading-relaxed">
          {alerta.mensaje}
        </p>

        <div className="flex items-center gap-2 pt-1">
          <select
            value={estado ? "true" : "false"}
            onChange={(e) => handleEstadoChange(e.target.value === "true")}
            className="text-xs px-2 py-1 rounded-md border border-zinc-200 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800 text-zinc-700 dark:text-zinc-300 cursor-pointer">
            <option value="true">Activa</option>
            <option value="false">Inactiva</option>
          </select>

          {hasChanged && (
            <Button
              size="sm"
              variant="outline"
              className="gap-1 h-7 text-xs"
              onClick={() => onSave(estado)}
              disabled={isPending}>
              <Save className="w-3 h-3" />
              Guardar
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}

export default function ClienteAlertsEditSection({ clienteId }: Props) {
  const { data: alertasData, isLoading } = useAlertasPorCliente(clienteId);
  const alertas = alertasData?.content ?? [];
  const updateAlerta = useUpdateAlerta();

  if (isLoading) {
    return (
      <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 p-4">
        <div className="text-sm text-muted-foreground">Cargando alertas...</div>
      </div>
    );
  }

  if (alertas.length === 0) {
    return null;
  }

  return (
    <div className="rounded-2xl border border-amber-200 bg-amber-50/50 dark:bg-amber-900/10 dark:border-amber-800/30 overflow-hidden">
      <div className="px-5 py-4 border-b border-amber-200 dark:border-amber-800/30 bg-amber-100/50 dark:bg-amber-900/20">
        <div className="flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-amber-600 dark:text-amber-400" />
          <h3 className="text-sm font-semibold text-amber-900 dark:text-amber-200">
            Gestionar alertas del cliente
          </h3>
          <Badge
            variant="outline"
            className="text-xs border-amber-300 text-amber-700 dark:border-amber-600 dark:text-amber-400">
            {alertas.length} alerta{alertas.length !== 1 ? "s" : ""}
          </Badge>
        </div>
      </div>

      <div className="p-4 space-y-3">
        {alertas.map((alerta) => (
          <AlertEditItem
            key={alerta.id}
            alerta={alerta}
            onSave={(estado) => {
              if (alerta.id) {
                updateAlerta.mutate({ id: alerta.id, estado });
              }
            }}
            isPending={updateAlerta.isPending}
          />
        ))}
      </div>
    </div>
  );
}