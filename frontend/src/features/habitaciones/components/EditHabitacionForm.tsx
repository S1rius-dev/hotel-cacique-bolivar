import { FormProvider, useForm } from "react-hook-form";
import {
  habitacionSchema,
  type HabitacionFormValues,
} from "../schemas/habitacion.schema";

import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import NumeroField from "../fields/NumeroField";
import TipoHabitacionField from "../fields/TipoHabitacionField";
import EstadoHabitacionField from "../fields/EstadoHabitacionField";
import DescripcionField from "../fields/DescripcionField";
import { Button } from "@/components/ui/button";
import { useUpdateHabitacion } from "../hooks/useUpdateHabitacion";
import FormGlobalError from "@/components/form/FormGlobalError";
import type { HabitacionResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  habitacion: HabitacionResponse;
  isLoading?: boolean;
}

function EditHabitacionForm({ habitacion, isLoading }: Props) {
  const form = useForm<HabitacionFormValues>({
    resolver: zodResolver(habitacionSchema),
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      numero: habitacion.numero ?? "",
      tipoId: habitacion.tipo?.id ?? 0,
      descripcion: habitacion.descripcion ?? "",
      estado: (habitacion.estado as HabitacionFormValues["estado"]) ?? "DISPONIBLE",
    },
  });

  const onSubmit = (formData: HabitacionFormValues) => {
    if (habitacion.id) {
      updateHabitacion.mutate({ id: habitacion.id, data: formData });
    }
  };

  const updateHabitacion = useUpdateHabitacion(form);
  const globalError = form.formState.errors.root?.message;

  return (
    <FormProvider {...form}>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
          <FormGlobalError message={globalError} />
          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-lg font-semibold tracking-tight">
                Información de la habitación
              </h2>
              <p className="text-sm text-muted-foreground">
                Datos de identificación de la habitación.
              </p>
            </div>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <NumeroField />
              <TipoHabitacionField />
              <EstadoHabitacionField />
            </div>
          </section>

          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-2">
              <h2 className="text-lg font-semibold tracking-tight">
                Descripción
              </h2>
              <p className="text-sm text-muted-foreground">
                Información adicional sobre la habitación.
              </p>
            </div>
            <DescripcionField />
          </section>

          <div className="flex items-center justify-end gap-3">
            <Button
              type="submit"
              disabled={updateHabitacion.isPending || isLoading}
              className="bg-rose-800 text-white">
              {updateHabitacion.isPending || isLoading
                ? "Guardando..."
                : "Guardar cambios"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default EditHabitacionForm;