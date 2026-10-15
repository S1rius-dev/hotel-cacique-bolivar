import { useEffect, useCallback } from "react";
import { useForm, useWatch, FormProvider } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { Dialog } from "@/components/ui/dialog";
import {
  PublicDialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/public-dialog";
import { Form } from "@/components/ui/form";
import { Button } from "@/components/ui/button";
import FormGlobalError from "@/components/form/FormGlobalError";
import { clienteSchema, type ClienteFormValues } from "../schema/client.form";
import CedulaField from "@/features/clientes/fields/CedulaField";
import TipoIdentificacionField from "@/features/clientes/fields/TipoIdentificacionField";
import MunicipioField from "@/features/clientes/fields/MunicipioField";
import NombreField from "@/features/clientes/fields/NombreField";
import ApellidoField from "@/features/clientes/fields/ApellidoField";
import TelefonoField from "@/features/clientes/fields/TelefonoField";
import { useClientSearch } from "../hooks/useClientSearch";
import { useLinkClient } from "../hooks/useLinkClient";
import { useProfile } from "@/features/auth-client/hooks/useProfile";
import { CheckCircle2, Loader2, UserCheck } from "lucide-react";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export default function CompleteProfileDialog({ open, onOpenChange }: Props) {
  const { status, client, search, reset } = useClientSearch();
  const linkMutation = useLinkClient(afterSuccess);

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

  const watchedTipo = useWatch({ control: form.control, name: "tipoIdentificacion" });
  const watchedCedula = useWatch({ control: form.control, name: "cedula" });

  const completeProfile = useProfile(form, { onSuccess: afterSuccess });

  function afterSuccess() {
    onOpenChange(false);
    setTimeout(reset, 300);
  }

  useEffect(() => {
    if (client) {
      form.setValue("nombreCompleto", client.nombreCompleto ?? "");
      form.setValue("apellidoCompleto", client.apellidoCompleto ?? "");
    }
  }, [client, form]);

  const handleCedulaBlur = useCallback(() => {
    if (!watchedCedula || watchedCedula.length < 3) return;
    if (status === "found" && client) return;
    search(watchedTipo, watchedCedula);
  }, [watchedCedula, watchedTipo, search, status, client]);

  const handleLink = () => {
    if (client?.id) linkMutation.mutate(client.id);
  };

  const handleSubmit = (formData: ClienteFormValues) => {
    completeProfile.mutate(formData);
  };

  const handleClose = (v: boolean) => {
    if (!v) {
      form.reset();
      setTimeout(reset, 300);
    }
    onOpenChange(v);
  };

  const isFound = status === "found" && client;
  const isSearching = status === "searching";
  const isLinking = linkMutation.isPending;
  const isCreating = completeProfile.isPending;
  const actionLoading = isSearching || isLinking || isCreating;

  const actionLabel = () => {
    if (isSearching) return "Buscando...";
    if (isLinking) return "Vinculando...";
    if (isCreating) return "Guardando...";
    if (isFound) return "Vincular";
    return "Completar perfil";
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <PublicDialogContent
        className="
          w-[95vw] sm:max-w-lg
          max-h-[90vh] overflow-y-auto
          rounded-2xl
        "
      >
        <DialogHeader>
          <div>
            <DialogTitle>Completa tu perfil</DialogTitle>
            <DialogDescription>
              {isFound
                ? "Te encontramos en el sistema — solo vincula tus datos"
                : "Ingresa tus datos para gestionar tus reservas"}
            </DialogDescription>
          </div>
        </DialogHeader>

        <FormProvider {...form}>
          <Form {...form}>
            <form
              onSubmit={form.handleSubmit(handleSubmit)}
              className="space-y-5"
            >
              <FormGlobalError
                message={form.formState.errors.root?.message}
              />

              {/* Client found banner */}
              {isFound && (
                <div className="flex items-start gap-3 rounded-xl border border-emerald-200 bg-emerald-50 p-4">
                  <CheckCircle2 className="size-5 text-emerald-600 shrink-0 mt-0.5" />
                  <div>
                    <p className="text-sm font-semibold text-emerald-800">
                      Cliente encontrado
                    </p>
                    <p className="text-sm text-emerald-700 mt-0.5">
                      <UserCheck className="size-3.5 inline mr-1 -mt-0.5" />
                      {client?.nombreCompleto} {client?.apellidoCompleto}
                    </p>
                    <p className="text-xs text-emerald-600 mt-1">
                      Solo necesitas vincular tu cuenta para continuar
                    </p>
                  </div>
                </div>
              )}

              {/* Not found message */}
              {status === "notFound" && (
                <p className="text-xs text-amber-600 text-center">
                  No encontramos un cliente con esos datos. Completa el
                  formulario para registrarte.
                </p>
              )}

              {/* Error message */}
              {status === "error" && (
                <p className="text-xs text-red-500 text-center">
                  Ocurrió un error al buscar. Intenta de nuevo.
                </p>
              )}

              <div className="rounded-2xl border border-zinc-200 bg-white p-6">
                <div className="grid gap-6 lg:grid-cols-2">
                  <CedulaField onBlur={handleCedulaBlur} />
                  <TipoIdentificacionField />

                  <MunicipioField />
                  <NombreField />
                  <ApellidoField />
                  <TelefonoField />
                </div>

                <Button
                  variant="hotel"
                  type={isFound ? "button" : "submit"}
                  onClick={isFound ? handleLink : undefined}
                  className="w-full mt-5"
                  disabled={actionLoading}
                >
                  {actionLoading && (
                    <Loader2 className="size-4 animate-spin mr-2" />
                  )}
                  {actionLabel()}
                </Button>
              </div>
            </form>
          </Form>
        </FormProvider>
      </PublicDialogContent>
    </Dialog>
  );
}

