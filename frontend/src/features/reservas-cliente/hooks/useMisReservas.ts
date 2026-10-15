import { useQuery, useQueryClient, useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { misReservasApi, type MisReservasItem } from "../api/mis-reservas.api";
import { extractErrorMessage } from "@/lib/extract-error";

const KEY = ["mis-reservas"] as const;

/**
 * Listado del cliente. Usa TanStack Query en vez de useState + useEffect a mano:
 * el estado de carga/error vive en la cache y no hace falta un efecto que
 * dispare setState en cascada al abrir el modal.
 */
export const useMisReservas = (enabled: boolean) =>
  useQuery({
    queryKey: KEY,
    queryFn: async () => {
      const data = await misReservasApi.listar();
      return Array.isArray(data) ? data : [];
    },
    enabled,
    staleTime: 30_000,
  });

const replaceReserva = (
  items: MisReservasItem[],
  id: number,
  next: MisReservasItem,
) => items.map((r) => (r.id === id ? next : r));

export const useCancelarReserva = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => misReservasApi.cancelar(id),
    onSuccess: (_updated, id) => {
      // El backend ya no lista las canceladas, pero se quita de inmediato para
      // dar feedback sin esperar un refetch.
      qc.setQueryData<MisReservasItem[]>(KEY, (prev) =>
        prev?.filter((r) => r.id !== id),
      );
      toast.success("Reserva cancelada correctamente");
    },
    onError: (err) => toast.error(extractErrorMessage(err, "No se pudo cancelar la reserva")),
  });
};

export const useEditarFechasReserva = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      fechaCheckIn,
      fechaCheckOut,
    }: {
      id: number;
      fechaCheckIn: string;
      fechaCheckOut: string;
    }) => misReservasApi.editarFechas(id, fechaCheckIn, fechaCheckOut),
    onSuccess: (updated, { id }) => {
      qc.setQueryData<MisReservasItem[]>(KEY, (prev) =>
        prev ? replaceReserva(prev, id, updated) : prev,
      );
      toast.success("Fechas actualizadas correctamente");
    },
    onError: (err) =>
      toast.error(extractErrorMessage(err, "No se pudieron actualizar las fechas")),
  });
};
