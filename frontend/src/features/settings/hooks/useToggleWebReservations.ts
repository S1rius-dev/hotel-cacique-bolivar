import { useCallback } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { getBloqueoReservaController } from "@/api/generated/bloqueo-reserva-controller/bloqueo-reserva-controller";
import { extractErrorMessage } from "@/lib/extract-error";

export function useToggleWebReservations() {
  const queryClient = useQueryClient();
  const api = getBloqueoReservaController();

  const statusQuery = useQuery({
    queryKey: ["web-reservations-status"],
    queryFn: async () => {
      const res = await api.obtenerEstadoReservasWeb();
      return res.enabled;
    },
  });

  const toggleMutation = useMutation({
    mutationFn: async (enabled: boolean) => {
      return api.toggleReservasWeb({ enabled });
    },
    onSuccess: (_data, enabled) => {
      queryClient.setQueryData(["web-reservations-status"], enabled);
      queryClient.invalidateQueries({ queryKey: ["bloqueos"] });
      toast.success(
        enabled
          ? "Reservas web habilitadas correctamente"
          : "Reservas web deshabilitadas correctamente",
      );
    },
    onError: (error: unknown) => {
      toast.error(
        extractErrorMessage(error, "Error al cambiar estado de reservas"),
      );
    },
  });

  const toggle = useCallback(
    (enabled: boolean) => {
      toggleMutation.mutate(enabled);
    },
    [toggleMutation],
  );

  return {
    webEnabled: statusQuery.data ?? true,
    isLoading: statusQuery.isLoading,
    isPending: toggleMutation.isPending,
    toggle,
  };
}
