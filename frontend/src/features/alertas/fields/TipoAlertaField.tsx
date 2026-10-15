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
import type { AlertaFormValues } from "../schema/alert.schema";
import { AlertaRequestTipo } from "@/api/generated/openAPIDefinition.schemas";

function TipoAlertaField() {
  const { control } = useFormContext<AlertaFormValues>();

  const tipos = Object.values(AlertaRequestTipo);

  const tipoLabels: Record<string, string> = {
    ECONOMICO: "Económico",
    HURTO: "Hurto",
    CONFLICTIVO: "Conflictivo",
    FUMADOR: "Fumador",
    DANOS: "Daños",
  };

  return (
    <FormField
      control={control}
      name="tipo"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Tipo de alerta *</FormLabel>

          <Select value={field.value ?? ""} onValueChange={field.onChange}>
            <FormControl>
              <SelectTrigger className="h-11">
                <SelectValue placeholder="Seleccione tipo de alerta" />
              </SelectTrigger>
            </FormControl>

            <SelectContent>
              {tipos.map((tipo) => (
                <SelectItem key={tipo} value={tipo}>
                  {tipoLabels[tipo] ?? tipo}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export default TipoAlertaField;
