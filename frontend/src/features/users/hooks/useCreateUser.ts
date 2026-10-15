import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getUserController } from "@/api/generated/user-controller/user-controller";
import type { UserRequest } from "@/api/generated/openAPIDefinition.schemas";
import type { UseFormReturn } from "react-hook-form";
import { toast } from "sonner";
import type { UserFormValues } from "../schemas/user.schema";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";

export const useCreateUser = (form: UseFormReturn<UserFormValues>) => {
  const api = getUserController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: UserRequest) => {
      return await api.crearUsuario({
        ...data,
        roles: data.roles ?? [],
      });
    },

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["users"],
      });
      toast.success("Usuario creado correctamente");
    },

    onError: (error: unknown) => {
      if (applyServerFieldErrors(error, form.setError)) return;

      form.setError("root", {
        type: "server",
        message: extractErrorMessage(error, "Error inesperado al crear usuario"),
      });
    },
  });
};
