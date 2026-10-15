import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";

import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";

import { useFormContext } from "react-hook-form";

interface Props {
  disabled?: boolean;
}

function EstadoField({ disabled }: Props) {
  const { control } = useFormContext();

  return (
    <FormField
      control={control}
      name="activo"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Estado *</FormLabel>

          <Select
            value={field.value ? "true" : "false"}
            onValueChange={(val) => field.onChange(val === "true")}
            disabled={disabled}>
            <FormControl>
              <SelectTrigger className="h-8.5 rounded-md">
                <SelectValue placeholder="Seleccione estado" />
              </SelectTrigger>
            </FormControl>

            <SelectContent>
              <SelectItem value="true">Activo</SelectItem>
              <SelectItem value="false">Inactivo</SelectItem>
            </SelectContent>
          </Select>

          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default EstadoField;