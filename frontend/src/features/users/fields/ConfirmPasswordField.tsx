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

function ConfirmPasswordField() {
  const { control } = useFormContext<UserFormValues>();

  return (
    <FormField
      control={control}
      name="confirmPassword"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Confirmar contraseña *</FormLabel>
          <FormControl>
            <Input
              type="password"
              autoComplete="new-password"
              placeholder="Repita la contraseña"
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

export default ConfirmPasswordField;
