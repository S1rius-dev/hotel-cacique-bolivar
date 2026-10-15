import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getBloqueoReservaController } from "@/api/generated/bloqueo-reserva-controller/bloqueo-reserva-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export const useDeleteBloqueo = () => {
  const api = getBloqueoReservaController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      return await api.desactivar(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bloqueos"] });
      toast.success("Bloqueo desactivado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al desactivar bloqueo"));
    },
  });
};
