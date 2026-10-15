import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export const useCheckInPorDetalle = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      detalleId,
      huespedesIds,
    }: {
      reservaId: number;
      detalleId: number;
      huespedesIds: number[];
    }) =>
      getReservaController().checkInPorDetalle(reservaId, detalleId, {
        huespedesIds,
      }),
    onSuccess: (_data, { reservaId }) => {
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["reserva", reservaId] });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Check-in realizado exitosamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al realizar check-in"));
    },
  });
};

export const useExtenderEstadia = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      nuevaFechaCheckOut,
    }: {
      reservaId: number;
      nuevaFechaCheckOut: string;
    }) =>
      getReservaController().extenderEstadia(reservaId, {
        nuevaFechaCheckOut,
      }),
    onSuccess: (_data, { reservaId }) => {
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["reserva", reservaId] });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Estadía extendida exitosamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al extender la estadía"));
    },
  });
};

export const useCheckOutPorDetalle = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      detalleId,
    }: {
      reservaId: number;
      detalleId: number;
    }) => getReservaController().checkOutPorDetalle(reservaId, detalleId),
    onSuccess: (_data, { reservaId }) => {
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["reserva", reservaId] });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Check-out realizado exitosamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al realizar check-out"));
    },
  });
};
