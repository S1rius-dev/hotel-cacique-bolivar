import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useFormContext } from "react-hook-form";
import type { HabitacionFormValues } from "../schemas/habitacion.schema";

function EstadoHabitacionField() {
  const { control } = useFormContext<HabitacionFormValues>();

  const estados = [
    { value: "DISPONIBLE", label: "Disponible" },
    { value: "RESERVADA", label: "Reservada" },
    { value: "OCUPADA", label: "Ocupada" },
    { value: "EN_LIMPIEZA", label: "En limpieza" },
    { value: "MANTENIMIENTO", label: "Mantenimiento" },
    { value: "INHABILITADA", label: "Inhabilitada" },
  ];

  return (
    <FormField
      control={control}
      name="estado"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Estado *</FormLabel>
          <Select
            onValueChange={field.onChange}
            value={field.value as string}>
            <FormControl>
              <SelectTrigger>
                <SelectValue placeholder="Selecciona un estado" />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {estados.map((estado) => (
                <SelectItem key={estado.value} value={estado.value}>
                  {estado.label}
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

export default EstadoHabitacionField;