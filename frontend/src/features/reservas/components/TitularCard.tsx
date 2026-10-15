import { useState } from "react";
import { SearchIcon, ChevronsUpDown, UserIcon, XIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { useBuscarCliente } from "@/features/reservas/hooks/useBuscarCliente";
import ClienteSelectPopover from "./ClienteSelectPopover";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface TitularCardProps {
  selectedCliente: ClienteResponse | null;
  onSelectCliente: (cliente: ClienteResponse | null) => void;
}

function getInitials(nombre?: string) {
  if (!nombre) return "?";
  const parts = nombre.split(" ");
  if (parts.length >= 2) {
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }
  return nombre.substring(0, 2).toUpperCase();
}

export const TitularCard: React.FC<TitularCardProps> = ({
  selectedCliente,
  onSelectCliente,
}) => {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const debouncedSearch = useDebounce(search, 300);
  const { data: clientesData, isLoading } = useBuscarCliente(debouncedSearch);
  const clientes = clientesData?.content ?? [];

  const handleSelectCliente = (cliente: ClienteResponse) => {
    onSelectCliente(cliente);
    setOpen(false);
  };

  const handleClearCliente = () => {
    onSelectCliente(null);
  };

  return (
    <div className="bg-card border rounded-2xl p-5 space-y-4">
      <div className="flex items-center gap-2 pb-3 border-b border-zinc-200 dark:border-zinc-700">
        <UserIcon className="size-5 text-emerald-600" />
        <div>
          <h3 className="font-semibold text-zinc-900 dark:text-white">
            Titular de la reserva
          </h3>
          <p className="text-sm text-muted-foreground">
            Cliente responsable de la reserva
          </p>
        </div>
      </div>

      {!selectedCliente ? (
        <ClienteSelectPopover
          open={open}
          onOpenChange={setOpen}
          search={search}
          onSearchChange={setSearch}
          clientes={clientes}
          isLoading={isLoading}
          onSelect={handleSelectCliente}
          trigger={
            <Button
              variant="outline"
              role="combobox"
              aria-expanded={open}
              className="w-full justify-between h-10 font-normal"
            >
              <span className="flex items-center gap-2 text-muted-foreground truncate">
                <SearchIcon className="size-4 shrink-0" />
                {isLoading
                  ? "Cargando clientes..."
                  : "Buscar cliente por nombre o cédula..."}
              </span>
              <ChevronsUpDown className="size-4 shrink-0 opacity-50" />
            </Button>
          }
        />
      ) : (
        <div className="p-4 bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 rounded-xl">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="size-10 rounded-full bg-emerald-200 dark:bg-emerald-800 flex items-center justify-center">
                <span className="text-emerald-700 dark:text-emerald-300 font-semibold text-sm">
                  {getInitials(selectedCliente.nombreCompleto)}
                </span>
              </div>
              <div>
                <p className="font-medium text-zinc-900 dark:text-white">
                  {selectedCliente.nombreCompleto}
                </p>
                <p className="text-sm text-muted-foreground">
                  CC {selectedCliente.cedula}
                </p>
              </div>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={handleClearCliente}
              className="text-muted-foreground hover:text-zinc-900 dark:hover:text-white"
            >
              <XIcon className="size-4" />
            </Button>
          </div>
          {selectedCliente.telefono && (
            <div className="mt-3 pt-3 border-t border-emerald-200 dark:border-emerald-800 grid grid-cols-2 gap-2 text-sm">
              <div>
                <span className="text-muted-foreground">Tel:</span>
                <span className="ml-1 text-zinc-700 dark:text-zinc-300">
                  {selectedCliente.telefono}
                </span>
              </div>
              {selectedCliente.usuarioEmail && (
                <div>
                  <span className="text-muted-foreground">Email:</span>
                  <span className="ml-1 text-zinc-700 dark:text-zinc-300 truncate">
                    {selectedCliente.usuarioEmail}
                  </span>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
