import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";

import { Textarea } from "@/components/ui/textarea";
import { useFormContext } from "react-hook-form";
import type { AlertaFormValues } from "../schema/alert.schema";

function MensajeAlertaField() {
  const { control } = useFormContext<AlertaFormValues>();

  return (
    <FormField
      control={control}
      name="mensaje"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Mensaje *</FormLabel>

          <FormControl>
            <Textarea
              {...field}
              placeholder="Describe el motivo de la alerta..."
              className="min-h-[100px] resize-none"
            />
          </FormControl>

          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default MensajeAlertaField;
