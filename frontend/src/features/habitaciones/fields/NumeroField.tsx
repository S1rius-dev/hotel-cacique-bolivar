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

function NumeroField() {
  const { control } = useFormContext<HabitacionFormValues>();

  return (
    <FormField
      control={control}
      name="numero"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Número de habitación *</FormLabel>
          <FormControl>
            <Input placeholder="Ej: 101" {...field} />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default NumeroField;