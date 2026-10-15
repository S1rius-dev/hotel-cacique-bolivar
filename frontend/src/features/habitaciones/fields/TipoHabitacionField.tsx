import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useFormContext } from "react-hook-form";
import type { HabitacionFormValues } from "../schemas/habitacion.schema";
import { useTypeRoom } from "../hooks/useRoom";

function TipoHabitacionField() {
  const { control } = useFormContext<HabitacionFormValues>();
  const { data: tiposData } = useTypeRoom();
  const tipos = tiposData?.content ?? [];

  return (
    <FormField
      control={control}
      name="tipoId"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Tipo de habitación *</FormLabel>
          <Select
            onValueChange={(value) => field.onChange(Number(value))}
            value={field.value ? String(field.value) : ""}>
            <FormControl>
              <SelectTrigger>
                <SelectValue placeholder="Selecciona un tipo" />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {tipos.map((tipo) => (
                <SelectItem key={tipo.id} value={String(tipo.id)}>
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

export default TipoHabitacionField;