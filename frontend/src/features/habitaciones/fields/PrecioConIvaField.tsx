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

function PrecioConIvaField() {
  const { control } = useFormContext<TipoHabitacionFormValues>();

  return (
    <FormField
      control={control}
      name="precioConIva"
      render={({ field }) => {
        const rawValue = parseInt(String(field.value ?? ""), 10);

        return (
          <FormItem>
            <FormLabel>Precio por noche con IVA</FormLabel>
            <FormControl>
              <Input
                type="text"
                placeholder="Ej: 178.500"
                value={
                  Number.isFinite(rawValue) && rawValue > 0
                    ? formatNumber(rawValue)
                    : ""
                }
                onChange={(e) => {
                  const onlyNumbers = e.target.value.replace(/[^\d]/g, "");
                  const num = onlyNumbers ? parseInt(onlyNumbers, 10) : undefined;
                  field.onChange(num);
                }}
              />
            </FormControl>
            <FormMessage />
            <p className="text-xs text-muted-foreground">
              Opcional. Precio que se usa cuando la habitación cobra con IVA.
            </p>
          </FormItem>
        );
      }}
    />
  );
}

export default PrecioConIvaField;
