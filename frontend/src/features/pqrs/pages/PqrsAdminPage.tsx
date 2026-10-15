import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import ManagementTablePage from "@/layouts/dashboard/ManagementTablePage";
import { usePqrsList } from "../hooks/usePqrsList";
import { pqrsColumns } from "../components/pqrsColumns";
import PqrsDetailModal from "../components/PqrsDetailModal";
import type { PQRSResponse, EstadoPqrs, TipoPqrs } from "../types";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Filter } from "lucide-react";

const DEFAULT_SIZE = 15;

const ESTADO_OPTIONS: { value: string; label: string }[] = [
  { value: "all", label: "Todos los estados" },
  { value: "ABIERTA", label: "Abierta" },
  { value: "EN_REVISION", label: "En Revisión" },
  { value: "RESPONDIDA", label: "Respondida" },
  { value: "CERRADA", label: "Cerrada" },
  { value: "RECHAZADA", label: "Rechazada" },
];

const TIPO_OPTIONS: { value: string; label: string }[] = [
  { value: "all", label: "Todos los tipos" },
  { value: "QUEJA", label: "Queja" },
  { value: "PETICION", label: "Petición" },
  { value: "RECLAMO", label: "Reclamo" },
  { value: "SUGERENCIA", label: "Sugerencia" },
  { value: "FELICITACION", label: "Felicitación" },
  { value: "REPORTE_TECNICO", label: "Reporte Técnico" },
];

export default function PqrsAdminPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get("page") ?? "0", 10);
  const [estadoFiltro, setEstadoFiltro] = useState<string>("all");
  const [tipoFiltro, setTipoFiltro] = useState<string>("all");

  const filters: {
    page: number;
    size: number;
    estado?: EstadoPqrs;
    tipo?: TipoPqrs;
  } = {
    page,
    size: DEFAULT_SIZE,
  };
  if (estadoFiltro !== "all") filters.estado = estadoFiltro as EstadoPqrs;
  if (tipoFiltro !== "all") filters.tipo = tipoFiltro as TipoPqrs;

  const { data, isLoading } = usePqrsList(filters);

  const [selectedPqrs, setSelectedPqrs] = useState<PQRSResponse | null>(null);

  const pqrsList = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;
  const pageSize = data?.size ?? 0;

  return (
    <>
      <div className="flex items-center gap-3 px-6 pt-4 pb-2">
        <Filter className="size-4 text-muted-foreground" />
        <Select
          value={estadoFiltro}
          onValueChange={(v) => {
            setEstadoFiltro(v);
            setSearchParams({ page: "0" });
          }}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Filtrar por estado" />
          </SelectTrigger>
          <SelectContent>
            {ESTADO_OPTIONS.map((o) => (
              <SelectItem key={o.value} value={o.value}>
                {o.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select
          value={tipoFiltro}
          onValueChange={(v) => {
            setTipoFiltro(v);
            setSearchParams({ page: "0" });
          }}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Filtrar por tipo" />
          </SelectTrigger>
          <SelectContent>
            {TIPO_OPTIONS.map((o) => (
              <SelectItem key={o.value} value={o.value}>
                {o.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <ManagementTablePage
        title="Gestión de PQRS"
        data={pqrsList}
        columns={pqrsColumns}
        loading={isLoading}
        currentPage={page + 1}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={pageSize}
        onPageChange={(p) =>
          setSearchParams({
            page: String(p - 1),
            size: String(DEFAULT_SIZE),
            ...(estadoFiltro !== "all" ? { estado: estadoFiltro } : {}),
            ...(tipoFiltro !== "all" ? { tipo: tipoFiltro } : {}),
          })
        }
        onRowClick={(pqrs) => setSelectedPqrs(pqrs)}
      />
      <PqrsDetailModal
        pqrs={selectedPqrs}
        open={selectedPqrs != null}
        onClose={() => setSelectedPqrs(null)}
      />
    </>
  );
}
