import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getUserController } from "@/api/generated/user-controller/user-controller";
import type { UserUpdateRequest } from "@/api/generated/openAPIDefinition.schemas";
import type { UseFormReturn } from "react-hook-form";
import type { UserUpdateFormValues } from "../schemas/user.schema";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";

export const useDetailsUser = (id?: string) => {
  const api = getUserController();

  return useQuery({
    queryKey: ["users", id],
    queryFn: async () => {
      const userId = Number(id);
      if (isNaN(userId)) return null;
      return await api.obtenerUsuario(userId);
    },
    enabled: !!id && !isNaN(Number(id)),
  });
};

export const useDeleteUser = () => {
  const api = getUserController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      return await api.eliminarUsuario(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["users"],
      });
    },
  });
};

export const useUpdateUser = (
  form: UseFormReturn<UserUpdateFormValues>,
  userId?: number
) => {
  const api = getUserController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: UserUpdateRequest) => {
      return await api.actualizarUsuario(userId!, {
        ...data,
        roles: data.roles ?? [],
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["users"],
      });
    },
    onError: (error: unknown) => {
      if (applyServerFieldErrors(error, form.setError)) return;

      form.setError("root", {
        type: "server",
        message: extractErrorMessage(error, "Error inesperado al actualizar usuario"),
      });
    },
  });
};
