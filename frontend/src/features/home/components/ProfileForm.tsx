import { FormProvider, useForm } from "react-hook-form";

import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";

import FormGlobalError from "@/components/form/FormGlobalError";
import { clienteSchema, type ClienteFormValues } from "../schema/client.form";
import ApellidoField from "@/features/clientes/fields/ApellidoField";
import MunicipioField from "@/features/clientes/fields/MunicipioField";
import NombreField from "@/features/clientes/fields/NombreField";
import TelefonoField from "@/features/clientes/fields/TelefonoField";
import TipoIdentificacionField from "@/features/clientes/fields/TipoIdentificacionField";
import { useProfile } from "@/features/auth-client/hooks/useProfile";
import { Button } from "@/components/ui/button";
import CedulaField from "@/features/clientes/fields/CedulaField";

interface Props {
  isLoading?: boolean;
  onSuccess?: () => void;
}

function ProfileForm({ isLoading, onSuccess }: Props) {
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
    },
  });

  const completeProfile = useProfile(form, { onSuccess });
  const globalError = form.formState.errors.root?.message;

  const onSubmit = (formData: ClienteFormValues) => {
    completeProfile.mutate(formData);
  };

  return (
    <FormProvider {...form}>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
          <FormGlobalError message={globalError} />
          <div className="rounded-2xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900/40 p-6">
            <div className="grid gap-6 lg:grid-cols-2">
              <CedulaField />
              <TipoIdentificacionField />

              <MunicipioField />
              <NombreField />

              <ApellidoField />
              <TelefonoField />
            </div>
            <Button
              variant="hotel"
              type="submit"
              className="w-full mt-5"
              disabled={completeProfile.isPending || isLoading}>
              {completeProfile.isPending || isLoading
                ? "Guardando..."
                : "Guardar cliente"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default ProfileForm;
