import { FormProvider, useForm } from "react-hook-form";
import {
  clienteUpdateSchema,
  type ClienteUpdateFormValues,
} from "../schemas/client.schema";

import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import NombreField from "../fields/NombreField";
import ApellidoField from "../fields/ApellidoField";
import TelefonoField from "../fields/TelefonoField";
import { Button } from "@/components/ui/button";
import { useUpdateCliente } from "../hooks/useUpdateCliente";
import ObservacionesField from "../fields/ObservacionesField";
import FormGlobalError from "@/components/form/FormGlobalError";
import EstadoField from "../fields/EstadoField";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  cliente: ClienteResponse;
  isLoading?: boolean;
}

function EditClientForm({ cliente, isLoading }: Props) {
  const form = useForm<ClienteUpdateFormValues>({
    resolver: zodResolver(clienteUpdateSchema),
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      cedula: cliente.cedula || "",
      tipoIdentificacion: cliente.tipoIdentificacion?.id || 1,
      municipioExpedicion: cliente.municipioExpedicion?.id || 0,
      nombreCompleto: cliente.nombreCompleto || "",
      apellidoCompleto: cliente.apellidoCompleto || "",
      telefono: cliente.telefono || "",
      observaciones: cliente.observaciones || "",
      activo: cliente.activo ?? true,
    },
  });

  const onSubmit = (formData: ClienteUpdateFormValues) => {
    const {
      nombreCompleto,
      apellidoCompleto,
      telefono,
      observaciones,
      activo,
    } = formData;
    updateCliente.mutate({
      id: cliente.id!,
      data: {
        nombreCompleto,
        apellidoCompleto,
        telefono,
        observaciones,
        activo,
      },
    });
    window.history.back();
  };

  const updateCliente = useUpdateCliente();
  const globalError = form.formState.errors.root?.message;

  return (
    <FormProvider {...form}>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
          <FormGlobalError message={globalError} />
          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-lg font-semibold tracking-tight">
                Información de identificación
              </h2>

              <p className="text-sm text-muted-foreground">
                Datos oficiales del documento del cliente.
              </p>
            </div>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
              <div>
                <label className="text-sm font-medium text-muted-foreground mb-1.5 block">
                  Cédula *
                </label>
                <div className="h-10 px-3 rounded-md border border-zinc-200 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800 flex items-center text-sm text-zinc-700 dark:text-zinc-300">
                  {cliente.cedula}
                </div>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground mb-1.5 block">
                  Tipo de identificación *
                </label>
                <div className="h-10 px-3 rounded-md border border-zinc-200 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800 flex items-center text-sm text-zinc-700 dark:text-zinc-300">
                  {cliente.tipoIdentificacion?.nombre || "-"}
                </div>
              </div>
              <div>
                <label className="text-sm font-medium text-muted-foreground mb-1.5 block">
                  Municipio de expedición *
                </label>
                <div className="h-10 px-3 rounded-md border border-zinc-200 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800 flex items-center text-sm text-zinc-700 dark:text-zinc-300">
                  {cliente.municipioExpedicion
                    ? `${cliente.municipioExpedicion.nombre}`
                    : "-"}
                </div>
              </div>
            </div>
          </section>

          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-lg font-semibold tracking-tight">
                Información personal
              </h2>

              <p className="text-sm text-muted-foreground">
                Información básica del cliente.
              </p>
            </div>

            <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
              <div className="space-y-4">
                <NombreField />

                <TelefonoField />
              </div>

              <div>
                <ApellidoField />
              </div>
            </div>
          </section>

          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-2">
              <h2 className="text-lg font-semibold tracking-tight">
                Estado del cliente
              </h2>

              <p className="text-sm text-muted-foreground">
                Controla el estado de actividad del cliente.
              </p>
            </div>

            <div className="max-w-xs">
              <EstadoField />
            </div>
          </section>

          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-2">
              <h2 className="text-lg font-semibold tracking-tight">
                Observaciones
              </h2>

              <p className="text-sm text-muted-foreground">
                Información adicional relevante sobre el cliente.
              </p>
            </div>

            <ObservacionesField />
          </section>
          <div className="flex items-center justify-end gap-3">
            <Button
              type="submit"
              disabled={updateCliente.isPending || isLoading}
              className="bg-rose-800 text-white">
              {updateCliente.isPending || isLoading
                ? "Guardando..."
                : "Guardar cambios"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default EditClientForm;
