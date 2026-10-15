import { Search, User, X, Check, Loader2, AlertTriangle, IdCard, Phone, CalendarDays, Plus, ExternalLink } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import { useClientAlerts } from "../hooks/useClientAlerts";
import { TIPO_ALERTA_LABELS } from "../constants/disponibilidad.constants";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDate } from "@/utils/date";

interface Props {
  search: string;
  onSearchChange: (v: string) => void;
  selected: ClienteResponse[];
  onToggle: (c: ClienteResponse) => void;
  clientes: ClienteResponse[] | undefined;
  loadingClientes: boolean;
  excludeIds?: number[];
  onCreateNewClient?: () => void;
}

function QuickReservaClientCard({
  cliente,
  onToggle,
}: {
  cliente: ClienteResponse;
  onToggle: (c: ClienteResponse) => void;
}) {
  const navigate = useNavigate();
  const { data: alertas, isLoading } = useClientAlerts(cliente.id!);
  const hasAlerts = alertas != null && alertas.length > 0;

  const lastFecha = hasAlerts
    ? alertas!
        .map((a) => a.fechaCreacion)
        .filter(Boolean)
        .sort()
        .reverse()[0]
    : null;

  return (
    <div
      className={`rounded-lg border p-3 space-y-2 transition-colors ${
        hasAlerts
          ? "border-l-4 border-l-amber-400 dark:border-l-amber-500 bg-amber-50/40 dark:bg-amber-950/20"
          : "bg-white dark:bg-zinc-950/40 hover:bg-zinc-50 dark:hover:bg-zinc-900/60"
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3 min-w-0">
          <div className="p-1.5 rounded-full bg-zinc-100 dark:bg-zinc-800 shrink-0">
            <User className="w-4 h-4 text-zinc-500" />
          </div>
          <div className="min-w-0">
            <p className="text-sm font-semibold text-zinc-900 dark:text-zinc-100 truncate">
              {cliente.nombreCompleto} {cliente.apellidoCompleto}
            </p>
            <div className="flex flex-wrap gap-x-3 gap-y-0.5 text-[11px] text-muted-foreground mt-0.5">
              <span className="flex items-center gap-1">
                <IdCard className="w-3 h-3" />
                CC {cliente.cedula}
              </span>
              {cliente.telefono && (
                <span className="flex items-center gap-1">
                  <Phone className="w-3 h-3" />
                  {cliente.telefono}
                </span>
              )}
            </div>
            {cliente.createdAt && (
              <p className="text-[10px] text-muted-foreground flex items-center gap-1 mt-0.5">
                <CalendarDays className="w-2.5 h-2.5" />
                Cliente desde {formatDate(cliente.createdAt)}
              </p>
            )}
          </div>
        </div>
        <div className="flex items-center gap-1">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(`/dashboard/clientes/${cliente.id}`)}
            title="Ver ficha del cliente"
            className="shrink-0 text-muted-foreground hover:text-foreground h-7 w-7 p-0"
          >
            <ExternalLink className="w-3.5 h-3.5" />
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onToggle(cliente)}
            className="shrink-0 text-emerald-700 border-emerald-200 hover:bg-emerald-50 dark:text-emerald-400 dark:border-emerald-800 dark:hover:bg-emerald-900/20 text-[11px] h-7 px-2.5"
          >
            <Plus className="w-3 h-3 mr-1" />
            Seleccionar
          </Button>
        </div>
      </div>

      {/* Alertas section */}
      {cliente.id != null && (
        <div className="ml-10">
          {isLoading && (
            <div className="flex items-center gap-1 text-[11px] text-muted-foreground">
              <Loader2 className="w-2.5 h-2.5 animate-spin" />
              Cargando alertas...
            </div>
          )}
          {hasAlerts && (
            <div className="space-y-1">
              <div className="flex items-center gap-1">
                <AlertTriangle className="w-3 h-3 text-amber-500" />
                <span className="text-[11px] font-medium text-amber-700 dark:text-amber-400">
                  Alertas
                </span>
              </div>
              <div className="flex flex-wrap gap-1">
                {alertas!.map((a) => (
                  <span
                    key={a.id}
                    className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-full text-[10px] font-medium bg-amber-50 text-amber-700 border border-amber-200 dark:bg-amber-900/20 dark:text-amber-400 dark:border-amber-800"
                  >
                    {TIPO_ALERTA_LABELS[a.tipo ?? ""] ?? a.mensaje ?? a.tipo}
                  </span>
                ))}
              </div>
              {lastFecha && (
                <p className="text-[10px] text-muted-foreground flex items-center gap-1">
                  <CalendarDays className="w-2.5 h-2.5" />
                  Última alerta:{" "}
                  {formatDate(lastFecha)}
                </p>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function QuickReservaClientSearch({ search, onSearchChange, selected, onToggle, clientes, loadingClientes, excludeIds, onCreateNewClient }: Props) {
  const disponibles = (clientes ?? []).filter(
    (c) =>
      !selected.some((s) => s.id === c.id) &&
      !(excludeIds && c.id != null && excludeIds.includes(c.id)),
  );

  return (
    <div className="flex flex-col min-h-0 flex-1">
      {/* Search input */}
      <div className="space-y-1.5 mb-3">
        <label className="text-xs font-medium text-muted-foreground">
          Buscar huéspedes
        </label>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            type="text"
            placeholder="Nombre o cédula..."
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            className="w-full h-10 pl-9 pr-3 rounded-lg border bg-background text-sm outline-none focus:border-ring focus:ring-[3px] focus:ring-ring/50"
          />
        </div>
      </div>

      {/* Selected chips */}
      {selected.length > 0 && (
        <div className="space-y-1.5 mb-3">
          <p className="text-xs font-medium text-muted-foreground">
            Seleccionados ({selected.length})
          </p>
          <div className="flex flex-wrap gap-1.5">
            {selected.map((c) => (
              <span
                key={c.id}
                className="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400"
              >
                <Check className="w-3 h-3" />
                {c.nombreCompleto}
                <button
                  onClick={() => onToggle(c)}
                  className="hover:text-emerald-900 dark:hover:text-emerald-200"
                >
                  <X className="w-3 h-3" />
                </button>
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Search results */}
      <div className="flex-1 overflow-y-auto min-h-0 space-y-2">
        {loadingClientes && search.length >= 2 && (
          <div className="flex items-center justify-center py-4">
            <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
          </div>
        )}
        {disponibles.slice(0, 8).map((cliente) => (
          <QuickReservaClientCard
            key={cliente.id}
            cliente={cliente}
            onToggle={onToggle}
          />
        ))}
        {clientes && search.length >= 2 && disponibles.length === 0 && (
          <div className="text-center py-4 space-y-3">
            <p className="text-xs text-muted-foreground">
              No se encontraron clientes
            </p>
            {onCreateNewClient && (
              <Button
                variant="outline"
                size="sm"
                onClick={onCreateNewClient}
                className="border-emerald-200 text-emerald-700 hover:bg-emerald-50 dark:border-emerald-800 dark:text-emerald-400 dark:hover:bg-emerald-900/20"
              >
                <Plus className="w-3.5 h-3.5 mr-1.5" />
                Registrar nuevo huésped
              </Button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default QuickReservaClientSearch;
