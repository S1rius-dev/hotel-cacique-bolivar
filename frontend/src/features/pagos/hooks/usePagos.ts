import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getPagoController } from "@/api/generated/pago-controller/pago-controller";
import type { PagoRequest } from "@/api/generated/openAPIDefinition.schemas";

export const usePagosDeReserva = (reservaId: number | undefined) =>
  useQuery({
    queryKey: ["pagos", reservaId],
    queryFn: () => getPagoController().listarPagosPorReserva(reservaId!),
    enabled: !!reservaId,
  });

export const useSaldoReserva = (reservaId: number | undefined) =>
  useQuery({
    queryKey: ["saldo", reservaId],
    queryFn: () => getPagoController().consultarSaldoPago(reservaId!),
    enabled: !!reservaId,
  });

export const useRegistrarPago = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: PagoRequest) =>
      getPagoController().registrarPago(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["reserva", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["pagos", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["saldo", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    },
  });
};

export const usePagoPorId = (id: number | undefined) =>
  useQuery({
    queryKey: ["pago", id],
    queryFn: () => getPagoController().obtenerPago(id!),
    enabled: !!id,
  });

export const useReembolsarPago = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => getPagoController().reembolsarPago(id),
    onSuccess: (data) => {
      const reservaId = data.reservaId;
      if (reservaId) {
        queryClient.invalidateQueries({ queryKey: ["reserva", reservaId] });
        queryClient.invalidateQueries({ queryKey: ["pagos", reservaId] });
        queryClient.invalidateQueries({ queryKey: ["saldo", reservaId] });
        queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
        queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      }
    },
  });
};
