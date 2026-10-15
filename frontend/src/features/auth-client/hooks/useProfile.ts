import { getAuthController } from "@/api/generated/auth-controller/auth-controller";
import type { ClienteRequest } from "@/api/generated/openAPIDefinition.schemas";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { UseFormReturn } from "react-hook-form";
import type { ClienteFormValues } from "@/features/home/schema/client.form";
import { toast } from "sonner";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";
import { useAuth } from "@/app/auth/AuthContext";
import { useNavigate } from "react-router-dom";

const authApi = getAuthController();

export const useProfile = (
  form: UseFormReturn<ClienteFormValues>,
  options?: { onSuccess?: () => void },
) => {
  const { refreshUser } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: ClienteFormValues) => {
      const dto: ClienteRequest = {
        cedula: data.cedula,
        tipoIdentificacionId: data.tipoIdentificacion,
        municipioExpedicionId: data.municipioExpedicion,
        nombreCompleto: data.nombreCompleto,
        apellidoCompleto: data.apellidoCompleto,
        telefono: data.telefono || undefined,
      };
      return await authApi.completarPerfil(dto);
    },

    onSuccess: async () => {
      await refreshUser();
      queryClient.invalidateQueries({ queryKey: ["clientes"] });
      toast.success("Perfil completado correctamente");
      form.reset({
        cedula: "",
        tipoIdentificacion: 1,
        municipioExpedicion: 0,
        nombreCompleto: "",
        apellidoCompleto: "",
        telefono: "",
      });
      if (options?.onSuccess) {
        options.onSuccess();
      } else {
        navigate("/", { replace: true });
      }
    },

    onError: (error: unknown) => {
      if (applyServerFieldErrors(error, form.setError)) {
        toast.error("Verifique los campos del formulario");
        return;
      }

      const message = extractErrorMessage(error, "Error inesperado al completar perfil");

      form.setError("root", { type: "server", message });
      toast.error(message);
    },
  });
};
