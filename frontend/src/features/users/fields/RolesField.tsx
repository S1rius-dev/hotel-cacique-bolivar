import {
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import { useFormContext } from "react-hook-form";
import type { UserFormValues } from "../schemas/user.schema";
import { useRoles } from "../hooks/useRoles";
import { CheckIcon, ChevronDownIcon } from "lucide-react";
import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

function RolesField() {
  const { control } = useFormContext<UserFormValues>();
  const { data: roles = [], isLoading } = useRoles();
  const [open, setOpen] = useState(false);

  return (
    <FormField
      control={control}
      name="roles"
      render={({ field }) => {
        const selectedValues = (field.value as string[]) ?? [];

        const handleSelect = (roleName: string) => {
          const newValue = selectedValues.includes(roleName)
            ? selectedValues.filter((r) => r !== roleName)
            : [...selectedValues, roleName];
          field.onChange(newValue);
        };

        const handleRemove = (roleName: string, e: React.MouseEvent) => {
          e.stopPropagation();
          field.onChange(selectedValues.filter((r) => r !== roleName));
        };

        return (
          <FormItem className="relative">
            <FormLabel className="text-sm font-medium">
              Seleccionar roles
            </FormLabel>
            <Popover open={open} onOpenChange={setOpen}>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  role="combobox"
                  aria-expanded={open}
                  className={cn(
                    "w-full justify-between min-h-10 px-3 py-2 border rounded-lg",
                    "bg-background hover:bg-muted/50 transition-colors",
                    !selectedValues.length && "text-muted-foreground",
                  )}>
                  <div className="flex flex-wrap gap-1 flex-1 min-h-6">
                    {selectedValues.length === 0 ? (
                      <span className="text-sm">Seleccionar roles...</span>
                    ) : (
                      selectedValues.map((role) => (
                        <Badge
                          key={role}
                          variant="secondary"
                          className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-xs font-medium bg-rose-100 text-rose-800 dark:bg-rose-900/30 dark:text-rose-400">
                          {role}
                          <span
                            className="size-3 cursor-pointer hover:text-rose-600"
                            onClick={(e) => handleRemove(role, e)}>
                            ×
                          </span>
                        </Badge>
                      ))
                    )}
                  </div>
                  <ChevronDownIcon className="size-4 shrink-0 opacity-50 ml-2" />
                </Button>
              </PopoverTrigger>
              <PopoverContent
                className="w-70 p-0"
                align="start"
                sideOffset={4}
                onOpenAutoFocus={(e) => e.preventDefault()}>
                <div className="max-h-48 overflow-y-auto">
                  {isLoading ? (
                    <div className="p-3 text-sm text-muted-foreground text-center">
                      Cargando...
                    </div>
                  ) : roles.length === 0 ? (
                    <div className="p-3 text-sm text-muted-foreground text-center">
                      Sin roles disponibles
                    </div>
                  ) : (
                    roles.map((rol) => {
                      const roleName = rol.nombre!;
                      const isSelected = selectedValues.includes(roleName);
                      return (
                        <button
                          key={roleName}
                          type="button"
                          onClick={() => handleSelect(roleName)}
                          className="flex items-center gap-3 w-full px-3 py-2.5 text-sm text-left hover:bg-muted transition-colors">
                          <div
                            className={cn(
                              "size-4 rounded border shrink-0 flex items-center justify-center",
                              isSelected
                                ? "bg-rose-600 border-rose-600"
                                : "border-muted-foreground/50",
                            )}>
                            {isSelected && (
                              <CheckIcon className="size-2.5 text-white" />
                            )}
                          </div>
                          <span className="font-medium">{roleName}</span>
                        </button>
                      );
                    })
                  )}
                </div>
              </PopoverContent>
            </Popover>
            <p className="text-xs text-muted-foreground mt-1.5">
              {selectedValues.length > 0
                ? `${selectedValues.length} rol(es) seleccionado(s)`
                : "Selecciona al menos un rol"}
            </p>
            <FormMessage />
          </FormItem>
        );
      }}
    />
  );
}

export default RolesField;
