import { Filter } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

const TIPO_OPTIONS: { value: string; label: string }[] = [
  { value: "all", label: "Todos los orígenes" },
  { value: "AUTOMATICO", label: "Automáticas" },
  { value: "MANUAL", label: "Manuales" },
];

const ESTADO_OPTIONS: { value: string; label: string }[] = [
  { value: "all", label: "Todos los estados" },
  { value: "EXITOSO", label: "Exitosas" },
  { value: "FALLIDO", label: "Fallidas" },
  { value: "EN_PROGRESO", label: "En progreso" },
];

interface Props {
  tipo: string;
  estado: string;
  onTipoChange: (v: string) => void;
  onEstadoChange: (v: string) => void;
}

export function BackupFilters({ tipo, estado, onTipoChange, onEstadoChange }: Props) {
  return (
    <div className="flex flex-wrap items-center gap-3">
      <Filter className="size-4 text-muted-foreground" />

      <Select value={tipo} onValueChange={onTipoChange}>
        <SelectTrigger className="w-[180px]">
          <SelectValue placeholder="Filtrar por origen" />
        </SelectTrigger>
        <SelectContent>
          {TIPO_OPTIONS.map((o) => (
            <SelectItem key={o.value} value={o.value}>
              {o.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select value={estado} onValueChange={onEstadoChange}>
        <SelectTrigger className="w-[180px]">
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
    </div>
  );
}
