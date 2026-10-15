import { useMutation, useQueryClient } from "@tanstack/react-query";
import { customInstance } from "@/api/custom-instance";
import type { ClientePublicResponse } from "@/api/generated/openAPIDefinition.schemas";
import { useAuth } from "@/app/auth/AuthContext";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export function useLinkClient(onSuccess?: () => void) {
  const { refreshUser } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (clienteId: number) => {
      return customInstance<ClientePublicResponse>({
        url: `/api/auth/vincular-cliente/${clienteId}`,
        method: "POST",
      });
    },

    onSuccess: async () => {
      await refreshUser();
      queryClient.invalidateQueries({ queryKey: ["clientes"] });
      toast.success("Cuenta vinculada correctamente");
      onSuccess?.();
    },

    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al vincular cuenta"));
    },
  });
}
