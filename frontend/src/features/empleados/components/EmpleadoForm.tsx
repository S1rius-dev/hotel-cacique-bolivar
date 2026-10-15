import { useForm, FormProvider } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { FormField, FormItem, FormLabel, FormControl, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { FormCard } from "@/components/form/FormCard";
import FormGlobalError from "@/components/form/FormGlobalError";
import { empleadoSchema, type EmpleadoFormValues } from "../schemas/empleado.schema";
import { Loader2 } from "lucide-react";

interface EmpleadoFormProps {
  defaultValues?: Partial<EmpleadoFormValues>;
  onSubmit: (data: EmpleadoFormValues) => void;
  isPending?: boolean;
  submitLabel?: string;
}

export default function EmpleadoForm({ defaultValues, onSubmit, isPending, submitLabel = "Guardar" }: EmpleadoFormProps) {
  const form = useForm<EmpleadoFormValues>({
    resolver: zodResolver(empleadoSchema),
    defaultValues: {
      nombres: "",
      apellidos: "",
      telefono: "",
      cargo: "RECEPCIONISTA",
      fechaIngreso: new Date().toISOString().split("T")[0],
      activo: true,
      ...defaultValues,
    },
  });

  return (
    <FormCard title="Datos del empleado" description="Completa la información del empleado">
      <FormProvider {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <FormGlobalError message={form.formState.errors.root?.message} />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormField name="nombres" render={({ field }) => (
              <FormItem>
                <FormLabel>Nombres <span className="text-red-500">*</span></FormLabel>
                <FormControl><Input placeholder="Nombres" {...field} /></FormControl>
                <FormMessage />
              </FormItem>
            )} />
            <FormField name="apellidos" render={({ field }) => (
              <FormItem>
                <FormLabel>Apellidos <span className="text-red-500">*</span></FormLabel>
                <FormControl><Input placeholder="Apellidos" {...field} /></FormControl>
                <FormMessage />
              </FormItem>
            )} />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <FormField name="telefono" render={({ field }) => (
              <FormItem>
                <FormLabel>Teléfono</FormLabel>
                <FormControl><Input placeholder="3001234567" maxLength={10} {...field} /></FormControl>
                <FormMessage />
              </FormItem>
            )} />
            <FormField name="cargo" render={({ field }) => (
              <FormItem>
                <FormLabel>Cargo <span className="text-red-500">*</span></FormLabel>
                <Select value={field.value} onValueChange={field.onChange}>
                  <FormControl>
                    <SelectTrigger><SelectValue placeholder="Seleccionar cargo" /></SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value="GERENTE">Gerente</SelectItem>
                    <SelectItem value="RECEPCIONISTA">Recepcionista</SelectItem>
                    <SelectItem value="LIMPIEZA">Limpieza</SelectItem>
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )} />
            <FormField name="fechaIngreso" render={({ field }) => (
              <FormItem>
                <FormLabel>Fecha de ingreso <span className="text-red-500">*</span></FormLabel>
                <FormControl><Input type="date" {...field} /></FormControl>
                <FormMessage />
              </FormItem>
            )} />
          </div>

          <FormField name="activo" render={({ field }) => (
            <FormItem className="flex flex-row items-center gap-2">
              <FormControl>
                <Checkbox checked={field.value} onCheckedChange={field.onChange} />
              </FormControl>
              <FormLabel className="!mt-0">Empleado activo</FormLabel>
            </FormItem>
          )} />

          <div className="flex justify-end gap-3 pt-2">
            <Button type="submit" variant="hotel" disabled={isPending}>
              {isPending && <Loader2 className="size-4 animate-spin" />}
              {submitLabel}
            </Button>
          </div>
        </form>
      </FormProvider>
    </FormCard>
  );
}
