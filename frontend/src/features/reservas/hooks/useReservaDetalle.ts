import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";
import type { ReservaUpdateRequest, DetalleReservaRequest, ActualizarDetalleRequest, AjusteRequest } from "@/api/generated/openAPIDefinition.schemas";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getReservaController();

// ── Query ─────────────────────────────────────────────

export const useReservaDetalle = (id?: number) => {
  return useQuery({
    queryKey: ["reserva", id],
    queryFn: () => api.buscarReservaPorId(id!),
    enabled: !!id,
  });
};

// ── Check-out por detalle (habitación individual) ──

export const useCheckOutDetalle = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      detalleId,
    }: {
      reservaId: number;
      detalleId: number;
    }) => api.checkOutPorDetalle(reservaId, detalleId),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });

      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Check-out realizado exitosamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al realizar el check-out")),
  });
};

// ── Check-in y Check-out global (toda la reserva) ──

export const useCheckInReserva = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (reservaId: number) => api.realizarCheckIn(reservaId),
    onSuccess: (_data, reservaId) => {
      queryClient.invalidateQueries({ queryKey: ["reserva", reservaId] });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });

      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Check-in de reserva realizado exitosamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al hacer el check-in")),
  });
};

export const useCheckOutReserva = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (reservaId: number) => api.realizarCheckOut(reservaId),
    onSuccess: (_data, reservaId) => {
      queryClient.invalidateQueries({ queryKey: ["reserva", reservaId] });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });

      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Check-out de reserva realizado exitosamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al hacer el check-out")),
  });
};

// ── Check-in con huéspedes (check-in + registro de huéspedes, un solo llamado) ──

export const useCheckInConHuespedes = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      detalleId,
      clienteIds,
    }: {
      reservaId: number;
      detalleId: number;
      clienteIds: number[];
    }) =>
      api.checkInPorDetalle(reservaId, detalleId, { huespedesIds: clienteIds }),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });

      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Check-in realizado exitosamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al hacer el check-in")),
  });
};

// ── Eliminar reserva ──

export const useDeleteReserva = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => api.eliminarReserva(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["habitaciones-disponibles"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Reserva eliminada correctamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al eliminar la reserva")),
  });
};

// ── Actualizar reserva (PUT) ──

export const useActualizarReserva = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      data,
    }: {
      id: number;
      data: ReservaUpdateRequest;
    }) => api.actualizarReserva(id, data),
    onSuccess: (_data, variables) => {
      toast.success("Reserva actualizada correctamente");
      queryClient.invalidateQueries({ queryKey: ["reserva", variables.id] });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["habitaciones-disponibles"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    },
    onError: (error) => toast.error(extractErrorMessage(error, "Error al actualizar la reserva")),
  });
};

export const useActualizarDetalle = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      detalleId,
      data,
    }: {
      reservaId: number;
      detalleId: number;
      data: ActualizarDetalleRequest;
    }) => api.actualizarDetalle(reservaId, detalleId, data),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["habitaciones-disponibles"] });

      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
    },
  });
};

// ── Agregar huéspedes a un detalle (POST) ──

export const useAgregarHuespedes = () => {
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
    }) => api.agregarHuespedes(reservaId, detalleId, { huespedesIds }),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Huéspedes agregados correctamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al agregar los huéspedes")),
  });
};

// ── Ajustes (cargos/descuentos/cortesías) ──

export const useAgregarAjuste = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      data,
    }: {
      reservaId: number;
      data: AjusteRequest;
    }) => api.agregarAjuste(reservaId, data),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["pagos", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["saldo", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Ajuste agregado correctamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al agregar el ajuste")),
  });
};

export const useEditarAjuste = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      ajusteId,
      data,
    }: {
      reservaId: number;
      ajusteId: number;
      data: AjusteRequest;
    }) => api.editarAjuste(reservaId, ajusteId, data),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["pagos", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["saldo", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Ajuste actualizado correctamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al editar el ajuste")),
  });
};

export const useEliminarAjuste = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      ajusteId,
    }: {
      reservaId: number;
      ajusteId: number;
    }) => api.eliminarAjuste(reservaId, ajusteId),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["pagos", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["saldo", variables.reservaId] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Ajuste eliminado correctamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al eliminar el ajuste")),
  });
};

// ── Remover huésped de un detalle (DELETE) ──

export const useRemoverHuesped = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      detalleId,
      detalleHuespedId,
    }: {
      reservaId: number;
      detalleId: number;
      detalleHuespedId: number;
    }) => api.removerHuesped(reservaId, detalleId, detalleHuespedId),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Huésped removido correctamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al remover el huésped")),
  });
};

// ── Agregar detalle (habitación) a la reserva (POST) ──

export const useAgregarDetalle = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      data,
    }: {
      reservaId: number;
      data: DetalleReservaRequest;
    }) => api.agregarDetalle(reservaId, data),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["habitaciones-disponibles"] });

      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Habitación agregada correctamente");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al agregar la habitación")),
  });
};

// ── Remover detalle (habitación) de la reserva (DELETE) ──

export const useRemoverDetalle = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      reservaId,
      detalleId,
    }: {
      reservaId: number;
      detalleId: number;
    }) => api.removerDetalle(reservaId, detalleId),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: ["reserva", variables.reservaId],
      });
      queryClient.invalidateQueries({ queryKey: ["reservas"] });
      queryClient.invalidateQueries({ queryKey: ["estado-diario"] });
      queryClient.invalidateQueries({ queryKey: ["habitaciones-disponibles"] });

      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Habitación removida de la reserva");
    },
    onError: (error: unknown) => toast.error(extractErrorMessage(error, "Error al remover la habitación")),
  });
};
