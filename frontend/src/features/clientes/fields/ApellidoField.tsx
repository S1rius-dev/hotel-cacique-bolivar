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

function ApellidoField() {
  const { control } = useFormContext<ClienteFormValues>();

  return (
    <FormField
      control={control}
      name="apellidoCompleto"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Apellidos completos*</FormLabel>
          <FormControl>
            <Input
              placeholder="Ej: Pérez Gómez"
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

export default ApellidoField;
