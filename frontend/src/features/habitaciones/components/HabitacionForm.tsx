import { FormProvider, useForm } from "react-hook-form";
import {
  habitacionSchema,
  type HabitacionFormValues,
} from "../schemas/habitacion.schema";

import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import NumeroField from "../fields/NumeroField";
import TipoHabitacionField from "../fields/TipoHabitacionField";
import DescripcionField from "../fields/DescripcionField";
import { Button } from "@/components/ui/button";
import { useCreateHabitacion } from "../hooks/useCreateHabitacion";
import FormGlobalError from "@/components/form/FormGlobalError";

interface Props {
  isLoading?: boolean;
}

function HabitacionForm({ isLoading }: Props) {
  const form = useForm<HabitacionFormValues>({
    resolver: zodResolver(habitacionSchema),
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      numero: "",
      tipoId: 0,
      descripcion: "",
    },
  });

  const onSubmit = (formData: HabitacionFormValues) => {
    createHabitacion.mutate(formData);
  };

  const createHabitacion = useCreateHabitacion(form);
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

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <NumeroField />
              <TipoHabitacionField />
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
              disabled={createHabitacion.isPending || isLoading}
              className="bg-rose-800 text-white">
              {createHabitacion.isPending || isLoading
                ? "Guardando..."
                : "Guardar habitación"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default HabitacionForm;