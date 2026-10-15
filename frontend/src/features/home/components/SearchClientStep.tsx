import { useState } from "react";
import { Search, Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import { useClientCatalogos } from "@/features/clientes/hooks/useCatalogo";
import { Button } from "@/components/ui/button";

interface Props {
  onSearch: (tipoIdentificacionId: number, cedula: string) => void;
  isSearching: boolean;
}

export default function SearchClientStep({ onSearch, isSearching }: Props) {
  const { tiposIdentificacion, isLoading: catLoading } = useClientCatalogos();
  const [tipoId, setTipoId] = useState<string>("");
  const [cedula, setCedula] = useState("");

  const canSearch = tipoId && cedula.trim().length >= 3 && !isSearching;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (canSearch) onSearch(Number(tipoId), cedula.trim());
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div className="text-center">
        <div
          className="
            mx-auto mb-3 size-12 rounded-full
            bg-rose-100 dark:bg-rose-900/30
            flex items-center justify-center
          "
        >
          <Search className="size-5 text-rose-600" />
        </div>
        <h3 className="text-lg font-semibold text-stone-800 dark:text-stone-200">
          Buscar cliente existente
        </h3>
        <p className="text-sm text-stone-500 dark:text-stone-400 mt-1">
          Ingresa tu tipo y número de documento para verificar si ya estás
          registrado en nuestro sistema.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-stone-600 dark:text-stone-400">
            Tipo de identificación
          </label>
          <Select
            value={tipoId}
            onValueChange={setTipoId}
            disabled={catLoading}
          >
            <SelectTrigger
              className="
                h-11 w-full border-border/60 bg-background shadow-sm
                focus:ring-2 focus:ring-rose-200
              "
            >
              <SelectValue placeholder="Seleccione" />
            </SelectTrigger>
            <SelectContent>
              {tiposIdentificacion
                .filter((t): t is typeof t & { id: number } => t.id != null)
                .map((tipo) => (
                  <SelectItem
                    key={tipo.id}
                    value={tipo.id.toString()}
                    className="cursor-pointer"
                  >
                    {tipo.nombre}
                  </SelectItem>
                ))}
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-1.5">
          <label className="text-xs font-medium text-stone-600 dark:text-stone-400">
            Número de documento
          </label>
          <Input
            placeholder="12345678"
            value={cedula}
            onChange={(e) =>
              setCedula(e.target.value.replace(/\D/g, ""))
            }
            className="h-11 border-border/60 bg-background shadow-sm focus-visible:ring-2 focus-visible:ring-rose-200"
            maxLength={20}
          />
        </div>
      </div>

      <Button
        type="submit"
        disabled={!canSearch}
        className="
          w-full h-11
          bg-rose-700 hover:bg-rose-800
          text-white font-semibold
          cursor-pointer
          disabled:opacity-50 disabled:cursor-not-allowed
          transition-colors
        "
      >
        {isSearching ? (
          <>
            <Loader2 className="size-4 mr-2 animate-spin" />
            Buscando...
          </>
        ) : (
          <>
            <Search className="size-4 mr-2" />
            Buscar
          </>
        )}
      </Button>
    </form>
  );
}
