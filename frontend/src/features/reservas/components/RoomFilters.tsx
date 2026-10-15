import { SearchIcon } from "lucide-react";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

interface RoomFiltersProps {
  searchQuery: string;
  onSearchChange: (value: string) => void;
  tipoFilter: string;
  onTipoChange: (value: string) => void;
  tiposUnicos: string[];
  filteredCount: number;
  isLoading?: boolean;
}

export const RoomFilters: React.FC<RoomFiltersProps> = ({
  searchQuery,
  onSearchChange,
  tipoFilter,
  onTipoChange,
  tiposUnicos,
  filteredCount,
  isLoading,
}) => {
  return (
    <>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 shrink-0">
        <h3 className="font-semibold text-zinc-900 dark:text-white">
          Habitaciones Disponibles
        </h3>
        <span className="text-xs text-muted-foreground">
          {isLoading ? "Cargando…" : `${filteredCount} mostradas`}
        </span>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 shrink-0">
        <div className="relative flex-1 max-w-xs">
          <SearchIcon className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por número..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className="pl-9 h-9"
          />
        </div>
      </div>

      {tiposUnicos.length > 0 && (
        <div className="flex flex-wrap gap-1.5 shrink-0">
          <span className="text-xs text-muted-foreground self-center mr-1">
            Tipo:
          </span>
          <button
            onClick={() => onTipoChange("all")}
            className={cn(
              "px-3 py-1 rounded-full text-xs font-medium transition-all border",
              tipoFilter === "all"
                ? "bg-rose-800 text-white border-rose-800"
                : "bg-transparent text-muted-foreground border-zinc-300 dark:border-zinc-600 hover:bg-zinc-100 dark:hover:bg-zinc-800",
            )}>
            Todos
          </button>
          {tiposUnicos.map((tipo) => (
            <button
              key={tipo}
              onClick={() => onTipoChange(tipo)}
              className={cn(
                "px-3 py-1 rounded-full text-xs font-medium transition-all border",
                tipoFilter === tipo
                  ? "bg-rose-800 text-white border-rose-800"
                  : "bg-transparent text-muted-foreground border-zinc-300 dark:border-zinc-600 hover:bg-zinc-100 dark:hover:bg-zinc-800",
              )}>
              {tipo}
            </button>
          ))}
        </div>
      )}
    </>
  );
};
