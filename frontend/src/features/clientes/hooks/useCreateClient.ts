import { getClienteController } from "@/api/generated/cliente-controller/cliente-controller";

import type { ClienteRequest, ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

import { useMutation, useQueryClient } from "@tanstack/react-query";

import type { UseFormReturn } from "react-hook-form";

import type { ClienteFormValues } from "../schemas/client.schema";

import { toast } from "sonner";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";

export const useCreateClient = (
  form: UseFormReturn<ClienteFormValues>,
  options?: { onSuccess?: (cliente: ClienteResponse) => void },
) => {
  const api = getClienteController();

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

        observaciones: data.observaciones || undefined,
      };

      return await api.crearCliente(dto);
    },

    onSuccess: (cliente) => {
      queryClient.invalidateQueries({
        queryKey: ["clientes"],
      });

      toast.success("Cliente creado correctamente");

      form.reset({
        cedula: "",
        tipoIdentificacion: 1,
        municipioExpedicion: 0,
        nombreCompleto: "",
        apellidoCompleto: "",
        telefono: "",
        observaciones: "",
      });

      options?.onSuccess?.(cliente);
    },

    onError: (error: unknown) => {
      // FIELD ERRORS
      if (applyServerFieldErrors(error, form.setError)) {
        toast.error("Verifique los campos del formulario");
        return;
      }

      // GLOBAL ERROR
      const message = extractErrorMessage(error, "Error inesperado al crear cliente");

      form.setError("root", {
        type: "server",
        message,
      });

      toast.error(message);
    },
  });
};
