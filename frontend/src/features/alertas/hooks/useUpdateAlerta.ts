import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getAlertaController } from "@/api/generated/alerta-controller/alerta-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getAlertaController();

export const useUpdateAlerta = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, estado }: { id: number; estado: boolean }) => {
      return await api.actualizarAlerta(id, { estado });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["alertas", "cliente"],
      });
      toast.success("Estado de alerta actualizado");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al actualizar alerta"));
    },
  });
};