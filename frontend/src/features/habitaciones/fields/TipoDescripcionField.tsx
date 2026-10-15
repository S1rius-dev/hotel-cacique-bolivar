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

function TipoDescripcionField() {
  const { control } = useFormContext<TipoHabitacionFormValues>();

  return (
    <FormField
      control={control}
      name="descripcion"
      render={({ field }) => (
        <FormItem className="md:col-span-2">
          <FormLabel>Descripción</FormLabel>
          <FormControl>
            <Input
              placeholder="Descripción adicional del tipo de habitación"
              {...field}
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default TipoDescripcionField;