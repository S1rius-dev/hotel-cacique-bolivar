import { useFormContext } from "react-hook-form";
import type { ClienteFormValues } from "../schemas/client.schema";
import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";

interface Props {
  disabled?: boolean;
  onBlur?: () => void;
}

function CedulaField({ disabled, onBlur }: Props) {
  const { control } = useFormContext<ClienteFormValues>();

  return (
    <FormField
      control={control}
      name="cedula"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Identificación *</FormLabel>
          <FormControl>
            <Input
              placeholder="Ej: 1032456789"
              {...field}
              onBlur={() => {
                field.onBlur();
                onBlur?.();
              }}
              disabled={disabled}
              className="h-8.5 rounded-md"
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default CedulaField;
