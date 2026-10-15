import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import { useFormContext } from "react-hook-form";
import type { ClienteFormValues } from "../schemas/client.schema";
import { useClientCatalogos } from "@/features/clientes/hooks/useCatalogo";

function TipoIdentificacionField() {
  const { control } = useFormContext<ClienteFormValues>();
  const { tiposIdentificacion, isLoading } = useClientCatalogos();

  return (
    <FormField
      control={control}
      name="tipoIdentificacion"
      render={({ field }) => (
        <FormItem className="w-full">
          <FormLabel className="text-sm font-medium">
            Tipo de identificación *
          </FormLabel>

          <Select
            value={field.value?.toString() ?? ""}
            onValueChange={(value) => field.onChange(Number(value))}
            disabled={isLoading}>
            <FormControl>
              <SelectTrigger className="h-11 w-full border-border/60 bg-background shadow-sm transition focus:ring-2 focus:ring-rose-200">
                <SelectValue placeholder="Seleccione tipo" />
              </SelectTrigger>
            </FormControl>

            <SelectContent className=" border-border/60">
              {tiposIdentificacion
                .filter(
                  (
                    tipo,
                  ): tipo is typeof tipo & {
                    id: number;
                  } => tipo.id != null,
                )
                .map((tipo) => (
                  <SelectItem
                    key={tipo.id}
                    value={tipo.id.toString()}
                    className="cursor-pointer">
                    {tipo.nombre}
                  </SelectItem>
                ))}
            </SelectContent>
          </Select>

          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default TipoIdentificacionField;
