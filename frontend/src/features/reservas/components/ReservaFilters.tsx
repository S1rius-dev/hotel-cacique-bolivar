import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { DatePicker } from "@/components/ui/date-picker";
import { ESTADO_OPTIONS, ORIGEN_OPTIONS } from "@/features/reservas/constants/reserva.constants";
import { RotateCcw, Search } from "lucide-react";

interface ReservaFiltersProps {
  codigo: string;
  onCodigoChange: (v: string) => void;
  cliente: string;
  onClienteChange: (v: string) => void;
  estado: string;
  onEstadoChange: (v: string) => void;
  origen: string;
  onOrigenChange: (v: string) => void;
  checkInDesde: Date | undefined;
  onCheckInDesdeChange: (d: Date | undefined) => void;
  checkInHasta: Date | undefined;
  onCheckInHastaChange: (d: Date | undefined) => void;
  checkOutDesde: Date | undefined;
  onCheckOutDesdeChange: (d: Date | undefined) => void;
  checkOutHasta: Date | undefined;
  onCheckOutHastaChange: (d: Date | undefined) => void;
  onClear: () => void;
  hasActiveFilters: boolean;
}

function SearchBox({
  placeholder,
  value,
  onChange,
  className,
}: {
  placeholder: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  className?: string;
}) {
  return (
    <div
      className={
        `flex items-center gap-1.5 h-8 px-2.5 rounded-xl border border-zinc-200 bg-white/70 backdrop-blur-sm ` +
        `transition-all duration-200 ` +
        `focus-within:border-rose-800/90 focus-within:ring-4 focus-within:ring-rose-800/20 ` +
        `hover:border-rose-300 ` +
        `dark:bg-zinc-900/60 dark:border-zinc-700 dark:focus-within:border-rose-800/90 ` +
        (className ?? "")
      }
    >
      <Search className="w-4 h-4 text-zinc-400 dark:text-zinc-500 shrink-0" />
      <input
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        className="w-full min-w-0 bg-transparent text-xs outline-none
          placeholder:text-zinc-400 dark:placeholder:text-zinc-500
          text-zinc-900 dark:text-white"
      />
    </div>
  );
}

export function ReservaFilters({
  codigo,
  onCodigoChange,
  cliente,
  onClienteChange,
  estado,
  onEstadoChange,
  origen,
  onOrigenChange,
  checkInDesde,
  onCheckInDesdeChange,
  checkInHasta,
  onCheckInHastaChange,
  checkOutDesde,
  onCheckOutDesdeChange,
  checkOutHasta,
  onCheckOutHastaChange,
  onClear,
  hasActiveFilters,
}: ReservaFiltersProps) {
  return (
    <div className="grid grid-cols-2 sm:flex sm:flex-wrap items-center gap-2">
      <SearchBox
        placeholder="Código"
        value={codigo}
        onChange={(e) => onCodigoChange(e.target.value)}
        className="w-full sm:w-[140px]"
      />

      <SearchBox
        placeholder="Cliente"
        value={cliente}
        onChange={(e) => onClienteChange(e.target.value)}
        className="w-full sm:w-[170px] max-sm:hidden"
      />

      <div className="w-px h-6 bg-border mx-1 max-sm:hidden" />

      <Select value={estado} onValueChange={(v) => onEstadoChange(v === "all" ? "" : v)}>
        <SelectTrigger className="w-full sm:w-[135px] h-8 text-xs">
          <SelectValue placeholder="Estado" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">Todos</SelectItem>
          {ESTADO_OPTIONS.map((opt) => (
            <SelectItem key={opt.value} value={opt.value}>
              {opt.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select value={origen} onValueChange={(v) => onOrigenChange(v === "all" ? "" : v)}>
        <SelectTrigger className="w-full sm:w-[140px] h-8 text-xs max-sm:hidden">
          <SelectValue placeholder="Origen" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">Todos</SelectItem>
          {ORIGEN_OPTIONS.map((opt) => (
            <SelectItem key={opt.value} value={opt.value}>
              {opt.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <div className="w-px h-6 bg-border mx-1 max-sm:hidden" />

      <DatePicker
        value={checkInDesde}
        onChange={onCheckInDesdeChange}
        placeholder="Check-in desde"
        className="w-full sm:w-[145px]"
      />

      <DatePicker
        value={checkInHasta}
        onChange={onCheckInHastaChange}
        placeholder="Check-in hasta"
        className="w-full sm:w-[145px]"
      />

      <DatePicker
        value={checkOutDesde}
        onChange={onCheckOutDesdeChange}
        placeholder="Check-out desde"
        className="w-full sm:w-[145px] max-sm:hidden"
      />

      <DatePicker
        value={checkOutHasta}
        onChange={onCheckOutHastaChange}
        placeholder="Check-out hasta"
        className="w-full sm:w-[145px] max-sm:hidden"
      />

      {hasActiveFilters && (
        <Button
          variant="ghost"
          size="sm"
          onClick={onClear}
          className="col-span-full sm:col-auto h-8 px-2 text-xs text-muted-foreground hover:text-destructive gap-1"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          Limpiar
        </Button>
      )}
    </div>
  );
}
