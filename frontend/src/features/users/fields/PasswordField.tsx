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

function PasswordField() {
  const { control } = useFormContext<UserFormValues>();

  return (
    <FormField
      control={control}
      name="password"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Contraseña *</FormLabel>
          <FormControl>
            <Input
              type="password"
              autoComplete="new-password"
              placeholder="Mínimo 6 caracteres"
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

export default PasswordField;
