import {
  FormField,
  FormItem,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { useFormContext } from "react-hook-form";
import type { ClienteFormValues } from "../schemas/client.schema";
import { Textarea } from "@/components/ui/textarea";

function ObservacionesField() {
  const { control } = useFormContext<ClienteFormValues>();

  return (
    <FormField
      control={control}
      name="observaciones"
      render={({ field }) => (
        <FormItem>
          <FormControl>
            <Textarea
              placeholder="Ej: El cliente prefiere habitaciones en el tercer piso, Con ventana,etc.."
              className="min-h-28 resize-none"
              {...field}></Textarea>
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default ObservacionesField;
