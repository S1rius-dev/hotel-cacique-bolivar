import {
  Search,
  RotateCcw,
  Download,
  FileText,
  FileImage,
  Filter,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { DatePicker } from "@/components/ui/date-picker";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { ESTADO_DETALLE_OPTIONS } from "./constants";

interface Props {
  fechaInicio: Date | undefined;
  fechaFin: Date | undefined;
  habitacion: string;
  busqueda: string;
  estado: string;
  onFechaInicioChange: (d: Date | undefined) => void;
  onFechaFinChange: (d: Date | undefined) => void;
  onHabitacionChange: (v: string) => void;
  onBusquedaChange: (v: string) => void;
  onEstadoChange: (v: string) => void;
  onFilter: () => void;
  onClear: () => void;
  onExport: (format: "pdf" | "word") => void;
  hasActiveFilters: boolean;
}

export function GuestFilters({
  fechaInicio,
  fechaFin,
  habitacion,
  busqueda,
  estado,
  onFechaInicioChange,
  onFechaFinChange,
  onHabitacionChange,
  onBusquedaChange,
  onEstadoChange,
  onFilter,
  onClear,
  onExport,
  hasActiveFilters,
}: Props) {
  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center gap-2">
        <DatePicker
          value={fechaInicio}
          onChange={onFechaInicioChange}
          placeholder="Fecha inicio"
          className="w-full sm:w-[145px]"
        />
        <DatePicker
          value={fechaFin}
          onChange={onFechaFinChange}
          placeholder="Fecha fin"
          className="w-full sm:w-[145px]"
        />

        <div className="hidden sm:block w-px h-6 bg-border mx-1" />

        <Input
          placeholder="Habitación"
          value={habitacion}
          onChange={(e) => onHabitacionChange(e.target.value)}
          className="w-full sm:w-[120px] h-8 text-xs"
        />

        <Select value={estado} onValueChange={onEstadoChange}>
          <SelectTrigger className="w-full sm:w-[145px] h-8 text-xs">
            <SelectValue placeholder="Estado" />
          </SelectTrigger>
          <SelectContent>
            {ESTADO_DETALLE_OPTIONS.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <div className="hidden sm:block w-px h-6 bg-border mx-1" />

        <Button
          variant="hotel"
          size="sm"
          onClick={onFilter}
          className="h-8 cursor-pointer"
        >
          <Filter className="size-3.5" />
          Filtrar
        </Button>

        {hasActiveFilters && (
          <Button
            variant="ghost"
            size="sm"
            onClick={onClear}
            className="h-8 px-2 text-xs text-muted-foreground gap-1 hover:text-destructive cursor-pointer"
          >
            <RotateCcw className="size-3.5" />
            Limpiar
          </Button>
        )}
      </div>

      <div className="flex items-center gap-2">
        <div
          className="flex h-8 flex-1 items-center gap-1.5 rounded-xl border border-zinc-200 bg-white/70 px-2.5 backdrop-blur-sm transition-all duration-200 focus-within:ring-4 focus-within:ring-rose-800/20 focus-within:border-rose-800/90 hover:border-rose-300 dark:border-zinc-700 dark:bg-zinc-900/60 dark:focus-within:border-rose-800/90"
        >
          <Search className="size-4 shrink-0 text-zinc-400 dark:text-zinc-500" />
          <input
            placeholder="Buscar huésped, documento o código de reserva..."
            value={busqueda}
            onChange={(e) => onBusquedaChange(e.target.value)}
            className="w-full min-w-0 bg-transparent text-xs outline-none placeholder:text-zinc-400 dark:placeholder:text-zinc-500"
          />
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="outline"
              size="sm"
              className="h-8 shrink-0 gap-2 cursor-pointer"
            >
              <Download className="size-3.5" />
              <span className="hidden sm:inline">Exportar</span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => onExport("pdf")}>
              <FileImage className="size-4 mr-2" />
              Exportar PDF
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => onExport("word")}>
              <FileText className="size-4 mr-2" />
              Exportar Word
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  );
}
