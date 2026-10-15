import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { CANAL_EFECTIVO, CANAL_SIN_FILTRO } from "@/features/payments/constants/gateway";

interface Props {
  search: string;
  onSearchChange: (v: string) => void;
  canal: string;
  onCanalChange: (v: string) => void;
  estado: string;
  onEstadoChange: (v: string) => void;
  canalOnline: string;
  canalOnlineLabel: string;
}

export default function PagosFilters({
  search,
  onSearchChange,
  canal,
  onCanalChange,
  estado,
  onEstadoChange,
  canalOnline,
  canalOnlineLabel,
}: Props) {
  return (
    <div className="rounded-xl border bg-card border-border p-3 flex flex-col lg:flex-row gap-3">
      {/* Search */}
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
        <Input
          placeholder="Buscar por cliente o referencia…"
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          className="pl-9 h-9 bg-background border-border focus-visible:ring-[#c81e3a]/40"
        />
      </div>

      <div className="flex gap-3">
        <Select value={canal} onValueChange={onCanalChange}>
          <SelectTrigger className="w-[160px] h-9 bg-background border-border">
            <SelectValue placeholder="Canal" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={CANAL_SIN_FILTRO}>Todos los canales</SelectItem>
            <SelectItem value={canalOnline}>{canalOnlineLabel}</SelectItem>
            <SelectItem value={CANAL_EFECTIVO}>Efectivo</SelectItem>
          </SelectContent>
        </Select>

        <Select value={estado} onValueChange={onEstadoChange}>
          <SelectTrigger className="w-[170px] h-9 bg-background border-border">
            <SelectValue placeholder="Estado" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={CANAL_SIN_FILTRO}>Todos los estados</SelectItem>
            <SelectItem value="aprobada">Aprobada</SelectItem>
            <SelectItem value="pendiente">Pendiente</SelectItem>
            <SelectItem value="rechazada">Rechazada</SelectItem>
          </SelectContent>
        </Select>
      </div>
    </div>
  );
}
