import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useFormContext } from "react-hook-form";
import type { ClienteFormValues } from "../schemas/client.schema";

function NombreField() {
  const { control } = useFormContext<ClienteFormValues>();

  return (
    <FormField
      control={control}
      name="nombreCompleto"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Nombres completos *</FormLabel>
          <FormControl>
            <Input
              placeholder="Ej: Juan David"
              {...field}
              className="h-8.5 rounded-md"
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default NombreField;
