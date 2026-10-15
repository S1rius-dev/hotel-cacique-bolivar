import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useFormContext } from "react-hook-form";
import type { UserFormValues } from "../schemas/user.schema";

function EmailField() {
  const { control } = useFormContext<UserFormValues>();

  return (
    <FormField
      control={control}
      name="email"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Email *</FormLabel>
          <FormControl>
            <Input
              type="email"
              autoComplete="email"
              placeholder="Ej: usuario@correo.com"
              {...field}
              className="h-9 rounded-md"
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default EmailField;
