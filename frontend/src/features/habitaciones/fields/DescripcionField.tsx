import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useFormContext } from "react-hook-form";
import type { HabitacionFormValues } from "../schemas/habitacion.schema";

function DescripcionField() {
  const { control } = useFormContext<HabitacionFormValues>();

  return (
    <FormField
      control={control}
      name="descripcion"
      render={({ field }) => (
        <FormItem className="md:col-span-2">
          <FormLabel>Descripción</FormLabel>
          <FormControl>
            <Input
              placeholder="Descripción adicional de la habitación"
              {...field}
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default DescripcionField;