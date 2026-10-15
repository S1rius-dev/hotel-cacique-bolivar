import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { getConsumoController } from "@/api/generated/consumo-controller/consumo-controller";
import type { ConsumoRequest } from "@/api/generated/openAPIDefinition.schemas";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getConsumoController();

export const useConsumos = (reservaId?: number) =>
  useQuery({
    queryKey: ["consumos", reservaId],
    queryFn: () => api.listarConsumos(reservaId!),
    enabled: !!reservaId,
  });

export const useAgregarConsumo = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ reservaId, data }: { reservaId: number; data: ConsumoRequest }) =>
      api.agregarConsumo(reservaId, data),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ["consumos", vars.reservaId] });
      qc.invalidateQueries({ queryKey: ["reserva", vars.reservaId] });
      qc.invalidateQueries({ queryKey: ["reservas"] });
      qc.invalidateQueries({ queryKey: ["estado-diario"] });
      qc.invalidateQueries({ queryKey: ["productos"] });
      qc.invalidateQueries({ queryKey: ["tienda-metricas"] });
      // saldo y pagos se derivan de reserva, pero invalidamos por si hay caches separados
      qc.invalidateQueries({ queryKey: ["pagos"] });
      qc.invalidateQueries({ queryKey: ["turno-activo"] });
    },
    onError: (e: unknown) => toast.error(extractErrorMessage(e, "Error al cargar consumo")),
  });
};

export const useAnularConsumo = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ reservaId, consumoId }: { reservaId: number; consumoId: number }) =>
      api.anularConsumo(reservaId, consumoId),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ["consumos", vars.reservaId] });
      qc.invalidateQueries({ queryKey: ["reserva", vars.reservaId] });
      qc.invalidateQueries({ queryKey: ["reservas"] });
      qc.invalidateQueries({ queryKey: ["estado-diario"] });
      qc.invalidateQueries({ queryKey: ["productos"] });
      qc.invalidateQueries({ queryKey: ["tienda-metricas"] });
      toast.success("Consumo anulado");
    },
    onError: (e: unknown) => toast.error(extractErrorMessage(e, "Error al anular consumo")),
  });
};
