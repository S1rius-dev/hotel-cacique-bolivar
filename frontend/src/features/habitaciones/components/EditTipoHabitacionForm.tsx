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
import PrecioConIvaField from "../fields/PrecioConIvaField";
import TipoDescripcionField from "../fields/TipoDescripcionField";
import { Button } from "@/components/ui/button";
import { useUpdateTipoHabitacion } from "../hooks/useUpdateTipoHabitacion";
import FormGlobalError from "@/components/form/FormGlobalError";
import type { TipoHabitacionResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  tipoHabitacion: TipoHabitacionResponse;
  isLoading?: boolean;
}

function EditTipoHabitacionForm({ tipoHabitacion, isLoading }: Props) {
  const form = useForm<TipoHabitacionFormValues>({
    resolver: zodResolver(tipoHabitacionSchema),
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      nombre: tipoHabitacion.nombre ?? "",
      descripcion: tipoHabitacion.descripcion ?? "",
      capacidad: tipoHabitacion.capacidadMaxima ?? 1,
      precioBase: tipoHabitacion.precioBase ?? 0,
      precioPersonaExtra: tipoHabitacion.precioPersonaExtra ?? 0,
      precioConIva: tipoHabitacion.precioConIva,
    },
  });

  const onSubmit = (formData: TipoHabitacionFormValues) => {
    if (tipoHabitacion.id) {
      updateTipoHabitacion.mutate({ id: tipoHabitacion.id, data: formData });
    }
  };

  const updateTipoHabitacion = useUpdateTipoHabitacion(form);
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
              disabled={updateTipoHabitacion.isPending || isLoading}
              className="bg-rose-800 text-white">
              {updateTipoHabitacion.isPending || isLoading
                ? "Guardando..."
                : "Guardar cambios"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default EditTipoHabitacionForm;