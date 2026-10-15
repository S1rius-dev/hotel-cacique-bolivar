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

function CapacidadField() {
  const { control } = useFormContext<TipoHabitacionFormValues>();

  return (
    <FormField
      control={control}
      name="capacidad"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Capacidad máxima *</FormLabel>
          <FormControl>
            <Input
              type="number"
              min={1}
              placeholder="Ej: 4"
              {...field}
              onChange={(e) =>
                field.onChange(parseInt(e.target.value) || 0)
              }
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default CapacidadField;