import { customInstance } from "@/api/custom-instance";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";
import type { PQRSResponse, EstadoPqrs, TipoPqrs } from "../types";

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  page: number;
}

interface PqrsFilters {
  page?: number;
  size?: number;
  estado?: EstadoPqrs;
  tipo?: TipoPqrs;
}

export const usePqrsList = (filters: PqrsFilters = {}) => {
  const { page = 0, size = 15, estado, tipo } = filters;
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (estado) params.set("estado", estado);
  if (tipo) params.set("tipo", tipo);

  const path = estado
    ? `/api/pqrs/estado/${estado}?${params}`
    : tipo
      ? `/api/pqrs/tipo/${tipo}?${params}`
      : `/api/pqrs?${params}`;

  return useQuery({
    queryKey: ["pqrs", filters],
    queryFn: async () =>
      await customInstance<PageResponse<PQRSResponse>>({
        url: path,
        method: "GET",
      }),
  });
};

export const useCambiarEstadoPqrs = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, estado, respuesta }: { id: number; estado: EstadoPqrs; respuesta?: string }) => {
      return await customInstance<PQRSResponse>({
        url: `/api/pqrs/${id}/estado`,
        method: "PATCH",
        data: { estado, respuesta },
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pqrs"] });
      toast.success("Estado actualizado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al cambiar estado"));
    },
  });
};

export const useDeletePqrs = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      return await customInstance<void>({
        url: `/api/pqrs/${id}`,
        method: "DELETE",
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pqrs"] });
      toast.success("PQRS eliminada correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al eliminar PQRS"));
    },
  });
};
