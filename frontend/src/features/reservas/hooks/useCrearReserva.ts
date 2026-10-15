import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";
import { toast } from "sonner";
import type { ReservaRequest } from "@/api/generated/openAPIDefinition.schemas";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getReservaController();

export const useCrearReserva = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: ReservaRequest) => {
      return await api.crearReserva(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["habitaciones-disponibles"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Reserva creada exitosamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al crear la reserva"));
    },
  });
};