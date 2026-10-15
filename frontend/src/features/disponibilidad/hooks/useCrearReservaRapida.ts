import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";
import { toast } from "sonner";
import type { ReservaRapidaRequest } from "@/api/generated/openAPIDefinition.schemas";
import { extractErrorMessage } from "@/lib/extract-error";

export const useCrearReservaRapida = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: ReservaRapidaRequest) => {
      return await getReservaController().crearReservaRapida(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["habitaciones-disponibles"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al crear reserva"));
    },
  });
};
