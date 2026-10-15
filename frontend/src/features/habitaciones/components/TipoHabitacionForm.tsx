import { FormProvider, useForm } from "react-hook-form";
import {
  tipoHabitacionSchema,
  type TipoHabitacionFormValues,
} from "../schemas/tipoHabitacion.schema";

import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import TipoNombreField from "../fields/TipoNombreField";
import CapacidadField from "../fields/CapacidadField";
import PrecioBaseField from "../fields/PrecioBaseField";
import PrecioPersonaExtraField from "../fields/PrecioPersonaExtraField";
import TipoDescripcionField from "../fields/TipoDescripcionField";
import { Button } from "@/components/ui/button";
import { useCreateTipoHabitacion } from "../hooks/useCreateTipoHabitacion";
import PrecioConIvaField from "../fields/PrecioConIvaField";
import FormGlobalError from "@/components/form/FormGlobalError";

interface Props {
  isLoading?: boolean;
}

function TipoHabitacionForm({ isLoading }: Props) {
  const form = useForm<TipoHabitacionFormValues>({
    resolver: zodResolver(tipoHabitacionSchema),
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      nombre: "",
      descripcion: "",
      capacidad: 1,
      precioBase: 0,
      precioPersonaExtra: 0,
    },
  });

  const onSubmit = (formData: TipoHabitacionFormValues) => {
    createTipoHabitacion.mutate(formData);
  };

  const createTipoHabitacion = useCreateTipoHabitacion(form);
  const globalError = form.formState.errors.root?.message;

  return (
    <FormProvider {...form}>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
          <FormGlobalError message={globalError} />
          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-lg font-semibold tracking-tight">
                Información del tipo de habitación
              </h2>
              <p className="text-sm text-muted-foreground">
                Características y precios del tipo de habitación.
              </p>
            </div>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <TipoNombreField />
              <CapacidadField />
              <PrecioBaseField />
              <PrecioPersonaExtraField />
              <PrecioConIvaField />
            </div>
          </section>

          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-2">
              <h2 className="text-lg font-semibold tracking-tight">
                Descripción
              </h2>
              <p className="text-sm text-muted-foreground">
                Información adicional sobre el tipo de habitación.
              </p>
            </div>
            <TipoDescripcionField />
          </section>

          <div className="flex items-center justify-end gap-3">
            <Button
              type="submit"
              disabled={createTipoHabitacion.isPending || isLoading}
              className="bg-rose-800 text-white">
              {createTipoHabitacion.isPending || isLoading
                ? "Guardando..."
                : "Guardar tipo de habitación"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default TipoHabitacionForm;