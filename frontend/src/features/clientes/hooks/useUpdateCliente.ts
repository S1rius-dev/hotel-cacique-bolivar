import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getClienteController } from "@/api/generated/cliente-controller/cliente-controller";
import type { ClienteUpdateRequest } from "@/api/generated/openAPIDefinition.schemas";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getClienteController();

export const useUpdateCliente = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      data,
    }: {
      id: number;
      data: ClienteUpdateRequest;
    }) => {
      return await api.actualizarCliente(id, data);
    },
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ["clientes"] });
      queryClient.invalidateQueries({ queryKey: ["cliente", variables.id] });
      toast.success("Cliente actualizado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al actualizar cliente"));
    },
  });
};
