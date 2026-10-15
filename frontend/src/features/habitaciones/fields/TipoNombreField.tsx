import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useFormContext } from "react-hook-form";
import type { TipoHabitacionFormValues } from "../schemas/tipoHabitacion.schema";

function TipoNombreField() {
  const { control } = useFormContext<TipoHabitacionFormValues>();

  return (
    <FormField
      control={control}
      name="nombre"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Nombre *</FormLabel>
          <FormControl>
            <Input placeholder="Ej: Suite Deluxe" {...field} />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default TipoNombreField;