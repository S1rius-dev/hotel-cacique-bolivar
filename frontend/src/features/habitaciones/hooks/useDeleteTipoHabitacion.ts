import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getTipoHabitacionController } from "@/api/generated/tipo-habitacion-controller/tipo-habitacion-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export const useDeleteTipoHabitacion = () => {
  const api = getTipoHabitacionController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      return await api.eliminarTipoHabitacion(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tiposHabitacion"] });
      toast.success("Tipo de habitación eliminado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(
        extractErrorMessage(error, "Error al eliminar tipo de habitación"),
      );
    },
  });
};