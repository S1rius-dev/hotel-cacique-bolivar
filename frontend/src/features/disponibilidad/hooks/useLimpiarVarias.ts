import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getHabitacionController } from "@/api/generated/habitacion-controller/habitacion-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export const useLimpiarVarias = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (habitacionesIds: number[]) =>
      getHabitacionController().limpiarHabitaciones({ habitacionesIds }),
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success(res.mensaje ?? "Habitaciones limpiadas");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al limpiar habitaciones"));
    },
  });
};
