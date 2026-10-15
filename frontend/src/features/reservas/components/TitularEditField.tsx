import { useState } from "react";
import { User, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { useBuscarCliente } from "../hooks/useBuscarCliente";
import ClienteSelectPopover from "./ClienteSelectPopover";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  value?: number;
  currentName?: string;
  onChange: (id?: number) => void;
}

function TitularEditField({ value, currentName, onChange }: Props) {
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const debouncedSearch = useDebounce(search, 300);
  const { data: clientesData, isLoading } = useBuscarCliente(debouncedSearch);
  const clientes = clientesData?.content ?? [];

  const selectedCliente = value != null
    ? clientes.find((c) => c.id === value)
    : null;

  const handleSelect = (cliente: ClienteResponse) => {
    onChange(cliente.id!);
    setOpen(false);
  };

  return (
    <div>
      {value != null && selectedCliente ? (
        <div className="flex items-center justify-between p-4 rounded-lg border bg-rose-50/80 dark:bg-rose-950/20 border-rose-200 dark:border-rose-800">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full bg-rose-200 dark:bg-rose-800 flex items-center justify-center">
              <span className="text-rose-800 dark:text-rose-300 font-semibold text-xs">
                {(selectedCliente.nombreCompleto ?? "?")[0] ?? "?"}
              </span>
            </div>
            <div>
              <p className="text-sm font-medium text-zinc-900 dark:text-white">
                {selectedCliente.nombreCompleto} {selectedCliente.apellidoCompleto}
              </p>
              <p className="text-xs text-muted-foreground">
                CC {selectedCliente.cedula}
                {selectedCliente.telefono && ` · ${selectedCliente.telefono}`}
              </p>
            </div>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onChange(undefined)}
            className="text-muted-foreground hover:text-foreground"
          >
            <X className="w-4 h-4" />
          </Button>
        </div>
      ) : (
        <div>
          {currentName && (
            <p className="text-xs text-muted-foreground mb-2 flex items-center gap-1">
              <User className="w-3 h-3" />
              Actual: {currentName}
              <span className="text-[10px]">(se reemplazará al seleccionar uno nuevo)</span>
            </p>
          )}
          <ClienteSelectPopover
            open={open}
            onOpenChange={setOpen}
            search={search}
            onSearchChange={setSearch}
            clientes={clientes}
            isLoading={isLoading}
            onSelect={handleSelect}
            trigger={
              <Button
                variant="outline"
                role="combobox"
                aria-expanded={open}
                className="w-full justify-between h-10 font-normal"
              >
                <span className="flex items-center gap-2 text-muted-foreground truncate">
                  <User className="w-4 h-4 shrink-0" />
                  {isLoading ? "Cargando clientes..." : "Buscar cliente por nombre o cédula..."}
                </span>
                <svg className="w-4 h-4 opacity-50 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 9l4-4 4 4m0 6l-4 4-4-4" />
                </svg>
              </Button>
            }
          />
        </div>
      )}
    </div>
  );
}

export default TitularEditField;
