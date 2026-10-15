import { FormProvider, useForm } from "react-hook-form";
import {
  clienteSchema,
  type ClienteFormValues,
} from "../schemas/client.schema";

import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import CedulaField from "../fields/CedulaField";
import NombreField from "../fields/NombreField";
import ApellidoField from "../fields/ApellidoField";
import TelefonoField from "../fields/TelefonoField";
import TipoIdentificacionField from "../fields/TipoIdentificacionField";
import { Button } from "@/components/ui/button";
import { useCreateClient } from "../hooks/useCreateClient";
import MunicipioField from "../fields/MunicipioField";
import ObservacionesField from "../fields/ObservacionesField";
import FormGlobalError from "@/components/form/FormGlobalError";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  isLoading?: boolean;
  onSuccess?: (cliente: ClienteResponse) => void;
}

function ClientForm({ isLoading, onSuccess }: Props) {
  const form = useForm<ClienteFormValues>({
    resolver: zodResolver(clienteSchema),
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      cedula: "",
      tipoIdentificacion: 1,
      municipioExpedicion: 0,
      nombreCompleto: "",
      apellidoCompleto: "",
      telefono: "",
      observaciones: "",
    },
  });

  const onSubmit = (formData: ClienteFormValues) => {
    createClient.mutate(formData);
  };
  const createClient = useCreateClient(form, { onSuccess });
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
              <CedulaField />
              <TipoIdentificacionField />
              <MunicipioField />
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
              {/* COLUMNA IZQUIERDA */}
              <div className="space-y-4">
                <NombreField />
                <ApellidoField />
              </div>

              {/* COLUMNA DERECHA */}
              <div>
                <TelefonoField />
              </div>
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
              variant="hotel"
              type="submit"
              disabled={createClient.isPending || isLoading}>
              {createClient.isPending || isLoading
                ? "Guardando..."
                : "Guardar cliente"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default ClientForm;
