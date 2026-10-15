import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import FormGlobalError from "@/components/form/FormGlobalError";
import { clienteSchema, type ClienteFormValues } from "@/features/home/schema/client.form";
import NombreField from "@/features/clientes/fields/NombreField";
import ApellidoField from "@/features/clientes/fields/ApellidoField";
import TelefonoField from "@/features/clientes/fields/TelefonoField";
import TipoIdentificacionField from "@/features/clientes/fields/TipoIdentificacionField";
import CedulaField from "@/features/clientes/fields/CedulaField";
import MunicipioField from "@/features/clientes/fields/MunicipioField";
import { Loader2 } from "lucide-react";
import { useProfile } from "@/features/auth-client/hooks/useProfile";

interface Props {
  defaultValues?: Partial<ClienteFormValues>;
  onCancel: () => void;
  onSuccess: () => void;
}

export default function ProfileEditForm({ defaultValues, onCancel, onSuccess }: Props) {
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
      ...defaultValues,
    },
  });

  const completeProfile = useProfile(form, { onSuccess });

  return (
    <FormProvider {...form}>
      <Form {...form}>
        <form onSubmit={form.handleSubmit((data) => completeProfile.mutate(data))} className="space-y-5">
          <FormGlobalError message={form.formState.errors.root?.message} />

          <div className="rounded-xl border border-stone-200 bg-white p-5">
            <div className="grid gap-5 sm:grid-cols-2">
              <NombreField />
              <ApellidoField />
              <TelefonoField />
              <TipoIdentificacionField />
              <CedulaField />
              <MunicipioField />
            </div>
          </div>

          <div className="flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onCancel}
              className="rounded-xl border border-stone-200 px-5 py-2.5 text-sm font-medium text-stone-600 transition hover:bg-stone-50 cursor-pointer"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={completeProfile.isPending}
              className="inline-flex items-center gap-2 rounded-xl bg-rose-800 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-rose-900 disabled:opacity-50 cursor-pointer"
            >
              {completeProfile.isPending && <Loader2 className="size-4 animate-spin" />}
              {completeProfile.isPending ? "Guardando..." : "Guardar cambios"}
            </button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}
