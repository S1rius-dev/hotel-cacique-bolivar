import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getReservaController();

interface ToggleVariables {
  reservaId: number;
  detalleId: number;
  incluyeIva: boolean;
}

/**
 * Alterna el modo de precio (con/sin IVA) de una habitacion de la reserva.
 * El backend devuelve la ReservaResponse completa ya recalculada, asi que se
 * escribe directo en la cache: los totales del modal cambian al instante sin
 * un GET extra. El saldo se sirve de otro endpoint, asi que se invalida.
 */
export const useTogglePrecioModo = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ reservaId, detalleId, incluyeIva }: ToggleVariables) =>
      await api.togglePrecioModo(reservaId, detalleId, { incluyeIva }),
    onSuccess: (data, variables) => {
      queryClient.setQueryData(["reserva", variables.reservaId], data);
      queryClient.invalidateQueries({ queryKey: ["saldo", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
    },
    onError: (error: unknown, variables) => {
      toast.error(
        extractErrorMessage(
          error,
          "Error al cambiar el modo de precio de la habitación",
        ),
      );
      // La reserva local puede quedar desincronizada tras un fallo (404, 400
      // por estado terminal, etc.): se recarga para volver al estado real.
      queryClient.invalidateQueries({ queryKey: ["reserva", variables.reservaId] });
    },
  });
};
