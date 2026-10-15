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

function TelefonoField() {
  const { control } = useFormContext<ClienteFormValues>();

  return (
    <FormField
      control={control}
      name="telefono"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Teléfono</FormLabel>
          <FormControl>
            <Input
              type="tel"
              placeholder="Ej: 3001234567"
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

export default TelefonoField;
