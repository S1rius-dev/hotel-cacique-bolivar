import { useState } from "react";
import { Search, Loader2, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import PQRSTimeline from "./PQRSTimeline";
import { usePqrsTracking } from "../hooks/usePqrsTracking";
import { ESTADOS_PQRS } from "../constants";
import { extractErrorMessage } from "@/lib/extract-error";

export default function PQRSTracker() {
  const [numero, setNumero] = useState("");
  const [searchCodigo, setSearchCodigo] = useState<string | null>(null);

  const { data: pqrs, isLoading, isError, error } = usePqrsTracking(searchCodigo);

  const handleSearch = () => {
    if (!numero.trim()) return;
    setSearchCodigo(numero.trim());
  };

  const timelineSteps = pqrs
    ? Object.entries(ESTADOS_PQRS).map(([estado, config]) => {
        const estados = Object.keys(ESTADOS_PQRS);
        const currentIdx = estados.indexOf(pqrs.estado);
        const stepIdx = estados.indexOf(estado);
        return {
          estado,
          label: config.label,
          descripcion: config.descripcion,
          completado: stepIdx < currentIdx,
          activo: stepIdx === currentIdx,
        };
      })
    : [];

  return (
    <section className="rounded-2xl border border-stone-200 bg-white p-6">
      <h3 className="font-heading text-lg font-semibold text-stone-800">
        Consulta el estado de tu PQRS
      </h3>
      <p className="mt-1 text-sm text-stone-500">
        Ingresa el número de radicado que recibiste por correo
      </p>

      <div className="mt-4 flex gap-3">
        <Input
          placeholder="Ej: PQRS-20260726-12345"
          value={numero}
          onChange={(e) => {
            setNumero(e.target.value);
            if (searchCodigo) setSearchCodigo(null);
          }}
          onKeyDown={(e) => { if (e.key === "Enter") handleSearch(); }}
          className="flex-1"
        />
        <Button
          variant="hotel"
          onClick={handleSearch}
          disabled={!numero.trim() || isLoading}
        >
          {isLoading ? (
            <Loader2 className="size-4 animate-spin" />
          ) : (
            <Search className="size-4" />
          )}
          Consultar
        </Button>
      </div>

      {isError && (
        <div className="mt-4 flex items-center gap-2 rounded-lg bg-red-50 p-3 text-sm text-red-700">
          <AlertCircle className="size-4 shrink-0" />
          <span>
            {extractErrorMessage(
              error,
              "No se encontró ninguna PQRS con ese número de radicado",
            )}
          </span>
        </div>
      )}

      {pqrs && (
        <div className="mt-6 border-t border-stone-100 pt-6">
          <div className="mb-4 rounded-lg bg-stone-50 p-3">
            <p className="text-xs text-stone-500">N° de radicado</p>
            <p className="font-mono text-sm font-semibold text-stone-800">
              {pqrs.codigo}
            </p>
          </div>
          <div className="mb-4 grid grid-cols-2 gap-3 text-sm">
            <div>
              <p className="text-xs text-stone-500">Tipo</p>
              <p className="font-medium text-stone-700">
                {ESTADOS_PQRS[pqrs.estado]?.label ?? pqrs.estado}
              </p>
            </div>
            <div>
              <p className="text-xs text-stone-500">Asunto</p>
              <p className="font-medium text-stone-700">{pqrs.asunto}</p>
            </div>
          </div>
          {pqrs.respuesta && (
            <div className="mb-4 rounded-lg border border-stone-200 bg-stone-50 p-3">
              <p className="text-xs text-stone-500 mb-1">Respuesta</p>
              <p className="text-sm text-stone-700">{pqrs.respuesta}</p>
            </div>
          )}
          <PQRSTimeline steps={timelineSteps} />
        </div>
      )}

      {!pqrs && !isLoading && !isError && searchCodigo && (
        <div className="mt-6 border-t border-stone-100 pt-6">
          <p className="text-sm text-stone-400 text-center">
            Ingresa un número de radicado para consultar el estado
          </p>
        </div>
      )}
    </section>
  );
}
