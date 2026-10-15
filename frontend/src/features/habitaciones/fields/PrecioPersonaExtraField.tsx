import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useFormContext } from "react-hook-form";
import { formatNumber } from "@/utils/format";
import type { TipoHabitacionFormValues } from "../schemas/tipoHabitacion.schema";

function PrecioPersonaExtraField() {
  const { control } = useFormContext<TipoHabitacionFormValues>();

  return (
    <FormField
      control={control}
      name="precioPersonaExtra"
      render={({ field }) => {
        const rawValue =
          typeof field.value === "number" ? field.value : parseInt(String(field.value) || "0", 10);

        return (
          <FormItem>
            <FormLabel>Precio persona extra *</FormLabel>
            <FormControl>
              <Input
                type="text"
                placeholder="Ej: 50.000"
                value={rawValue > 0 ? formatNumber(rawValue) : ""}
                onChange={(e) => {
                  const onlyNumbers = e.target.value.replace(/[^\d]/g, "");
                  const num = onlyNumbers ? parseInt(onlyNumbers, 10) : 0;
                  field.onChange(num);
                }}
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        );
      }}
    />
  );
}

export default PrecioPersonaExtraField;