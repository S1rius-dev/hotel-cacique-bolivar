import { Loader2 } from "lucide-react";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Command,
  CommandInput,
  CommandList,
  CommandEmpty,
  CommandGroup,
  CommandItem,
} from "@/components/ui/command";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  search: string;
  onSearchChange: (value: string) => void;
  clientes: ClienteResponse[];
  isLoading: boolean;
  onSelect: (cliente: ClienteResponse) => void;
  trigger: React.ReactNode;
  placeholder?: string;
}

function getInitials(nombre?: string) {
  if (!nombre) return "?";
  const parts = nombre.split(" ");
  if (parts.length >= 2) {
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }
  return nombre.substring(0, 2).toUpperCase();
}

function ClienteSelectPopover({
  open,
  onOpenChange,
  search,
  onSearchChange,
  clientes,
  isLoading,
  onSelect,
  trigger,
  placeholder = "Buscar por nombre o cédula...",
}: Props) {
  return (
    <Popover open={open} onOpenChange={(v) => { onOpenChange(v); if (!v) onSearchChange(""); }}>
      <PopoverTrigger asChild>
        {trigger}
      </PopoverTrigger>
      <PopoverContent className="p-0 w-[var(--radix-popover-trigger-width)]">
        <Command shouldFilter={false}>
          <CommandInput
            placeholder={placeholder}
            value={search}
            onValueChange={onSearchChange}
          />
          <CommandList>
            <CommandEmpty>
              {isLoading ? (
                <div className="flex items-center justify-center py-4">
                  <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
                </div>
              ) : (
                "No se encontraron clientes"
              )}
            </CommandEmpty>
            <CommandGroup>
              {clientes.map((cliente) => (
                <CommandItem
                  key={cliente.id}
                  value={`${cliente.nombreCompleto} ${cliente.cedula}`}
                  onSelect={() => onSelect(cliente)}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center shrink-0">
                      <span className="text-emerald-700 dark:text-emerald-300 font-semibold text-xs">
                        {getInitials(cliente.nombreCompleto)}
                      </span>
                    </div>
                    <div>
                      <p className="font-medium text-sm">
                        {cliente.nombreCompleto} {cliente.apellidoCompleto}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        CC {cliente.cedula}
                        {cliente.telefono && ` - ${cliente.telefono}`}
                      </p>
                    </div>
                  </div>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}

export default ClienteSelectPopover;
