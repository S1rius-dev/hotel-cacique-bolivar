import { customInstance } from "@/api/custom-instance";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";
import { useAuth } from "@/app/auth/AuthContext";
import type { PQRSCreateRequest, PQRSResponse } from "../types";
import type { UseFormReturn } from "react-hook-form";
import type { PQRSFormValues } from "../schemas/pqrs.schema";

export const useCreatePqrs = (
  form: UseFormReturn<PQRSFormValues>,
) => {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: PQRSFormValues) => {
      const dto: PQRSCreateRequest = {
        tipo: data.tipo,
        asunto: data.asunto,
        descripcion: data.descripcion,
        reservaId: data.reservaId ? Number(data.reservaId) : undefined,
        nombreContacto: data.nombreContacto || user?.username || undefined,
        emailContacto: data.emailContacto || user?.email || undefined,
        telefonoContacto: data.telefonoContacto || undefined,
      };
      return await customInstance<PQRSResponse>({
        url: "/api/pqrs",
        method: "POST",
        data: dto,
      });
    },
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: ["pqrs"] });
      return response;
    },
    onError: (error: unknown) => {
      if (applyServerFieldErrors(error, form.setError)) {
        toast.error("Verifique los campos del formulario");
        return;
      }
      toast.error(extractErrorMessage(error, "Error al enviar la solicitud"));
    },
  });
};
