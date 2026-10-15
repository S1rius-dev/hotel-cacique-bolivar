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

function UsernameField() {
  const { control } = useFormContext<UserFormValues>();

  return (
    <FormField
      control={control}
      name="username"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Nombre de usuario *</FormLabel>
          <FormControl>
            <Input
              autoComplete="username"
              placeholder="Ej: juan123"
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

export default UsernameField;
