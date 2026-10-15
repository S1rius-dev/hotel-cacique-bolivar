import { useMemo, useState } from "react";

import { Check, ChevronsUpDown, Loader2 } from "lucide-react";

import { useDebounce } from "use-debounce";

import { Button } from "@/components/ui/button";

import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";

import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";

import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";

import { cn } from "@/lib/utils";

import { useFormContext } from "react-hook-form";

import type { ClienteFormValues } from "../schemas/client.schema";

import { useClientCatalogos } from "@/features/clientes/hooks/useCatalogo";

function MunicipioField() {
  const { control } = useFormContext<ClienteFormValues>();

  /**
   * UI STATE
   */
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [hasOpened, setHasOpened] = useState(false);
  const [displayValue, setDisplayValue] = useState<string>("");

  /**
   * PERFORMANCE
   * evita requests por cada tecla
   */
  const [debouncedSearch] = useDebounce(search, 300);

  /**
   * QUERY
   */
  const searchValue = !hasOpened && open ? "" : debouncedSearch;
  const { municipios, isLoading } = useClientCatalogos({
    municipioSearch: searchValue,
  });

  const handleOpenChange = (isOpen: boolean) => {
    if (isOpen && !hasOpened) {
      setHasOpened(true);
    }
    setOpen(isOpen);
  };

  /**
   * evita filtros innecesarios
   */
  const validMunicipios = useMemo(
    () =>
      municipios.filter(
        (
          municipio,
        ): municipio is typeof municipio & {
          id: number;
        } => municipio.id != null,
      ),
    [municipios],
  );

  return (
    <FormField
      control={control}
      name="municipioExpedicion"
      render={({ field }) => {
        const selectedMunicipio = validMunicipios.find(
          (m) => Number(m.id) === Number(field.value),
        );

        return (
          <FormItem className="flex w-full flex-col space-y-1">
            <FormLabel className="text-sm font-medium">
              Municipio de expedición *
            </FormLabel>

            <Popover open={open} onOpenChange={handleOpenChange}>
              <PopoverTrigger asChild>
                <FormControl>
                  <Button
                    type="button"
                    variant="outline"
                    role="combobox"
                    disabled={isLoading}
                    className={cn(
                      "h-8 w-full  border-border/60 bg-background px-3 shadow-sm transition-all hover:bg-accent/40",
                      !field.value && "text-muted-foreground",
                    )}>
                    <div className="flex w-full items-center justify-between">
                      <span className="truncate text-left">
                        {selectedMunicipio?.nombre ||
                          displayValue ||
                          "Buscar municipio"}
                      </span>

                      <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                    </div>
                  </Button>
                </FormControl>
              </PopoverTrigger>

              <PopoverContent
                align="start"
                className="w-(--radix-popover-trigger-width) rounded-xl border-border/60 p-0 shadow-xl">
                <Command shouldFilter={false}>
                  <CommandInput
                    placeholder="Buscar municipio..."
                    value={search}
                    onValueChange={setSearch}
                    className="h-11"
                  />

                  <CommandList className="max-h-75 overflow-y-auto">
                    {isLoading ? (
                      <div className="flex items-center justify-center py-6">
                        <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
                      </div>
                    ) : (
                      <>
                        <CommandEmpty>
                          {debouncedSearch.length < 2
                            ? "Escriba al menos 2 caracteres"
                            : "No se encontraron municipios"}
                        </CommandEmpty>

                        <CommandGroup>
                          {validMunicipios.map((municipio) => (
                            <CommandItem
                              key={municipio.id}
                              value={municipio.nombre ?? ""}
                              onSelect={() => {
                                field.onChange(municipio.id);
                                setDisplayValue(municipio.nombre || "");
                                setOpen(false);
                                setSearch("");
                              }}
                              className="cursor-pointer">
                              <div className="flex w-full items-center justify-between gap-2">
                                <div className="flex flex-col overflow-hidden">
                                  <span className="truncate font-medium">
                                    {municipio.nombre}
                                  </span>


                                </div>

                                <Check
                                  className={cn(
                                    "h-4 w-4 shrink-0",
                                    Number(municipio.id) === Number(field.value)
                                      ? "opacity-100"
                                      : "opacity-0",
                                  )}
                                />
                              </div>
                            </CommandItem>
                          ))}
                        </CommandGroup>
                      </>
                    )}
                  </CommandList>
                </Command>
              </PopoverContent>
            </Popover>

            <FormMessage />
          </FormItem>
        );
      }}
    />
  );
}

export default MunicipioField;
