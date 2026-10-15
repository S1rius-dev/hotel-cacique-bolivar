import { getAlertaController } from "@/api/generated/alerta-controller/alerta-controller";

import type { UseFormReturn } from "react-hook-form";

import type { AlertaFormValues } from "../schema/alert.schema";

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { toast } from "sonner";
import { normalizeApiError } from "@/lib/extract-error";

export const useCreateAlert = (
  form: UseFormReturn<AlertaFormValues>,
  clienteId?: number
) => {
  const api = getAlertaController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: AlertaFormValues) => {
      return await api.crearAlerta({
        clienteId: data.clienteId,
        tipo: data.tipo,
        mensaje: data.mensaje,
      });
    },
    onSuccess: () => {
      toast.success("Alerta creada correctamente");
      form.reset();
      queryClient.invalidateQueries({
        queryKey: ["alertas", "cliente", clienteId],
      });
    },
    onError: (error: unknown) => {
      const backend = normalizeApiError(error);

      if (backend.fieldErrors) {
        Object.entries(backend.fieldErrors).forEach(([field, message]) => {
          form.setError(field as keyof AlertaFormValues, {
            type: "server",
            message: String(message),
          });
        });

        toast.error("Verifique los campos del formulario");

        return;
      }

      const message = backend.message || "Error inesperado al crear alerta";

      form.setError("root", {
        type: "server",
        message,
      });

      toast.error(message);
    },
  });
};
