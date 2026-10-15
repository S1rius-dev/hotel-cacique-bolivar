import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getHabitacionController } from "@/api/generated/habitacion-controller/habitacion-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export const useDeleteHabitacion = () => {
  const api = getHabitacionController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      return await api.eliminarHabitacion(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["habitaciones"] });
      toast.success("Habitación eliminada correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al eliminar habitación"));
    },
  });
};

