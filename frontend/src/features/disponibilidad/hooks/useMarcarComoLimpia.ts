import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getHabitacionController } from "@/api/generated/habitacion-controller/habitacion-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export const useMarcarComoLimpia = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ habitacionId }: { habitacionId: number }) =>
      getHabitacionController().marcarHabitacionComoLimpia(habitacionId),
    onSuccess: (_data, { habitacionId }) => {
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["habitacion", habitacionId] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Habitación marcada como limpia");
    },
    onError: (error: unknown) => {
      toast.error(
        extractErrorMessage(error, "Error al marcar habitación como limpia"),
      );
    },
  });
};
