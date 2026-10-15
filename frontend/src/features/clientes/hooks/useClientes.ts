import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getClienteController } from "@/api/generated/cliente-controller/cliente-controller";
import type {
  PageResponseClienteResponse,
  ListarClientesParams,
} from "@/api/generated/openAPIDefinition.schemas";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getClienteController();

interface ClientesFilters {
  page?: number;
  size?: number;
  nombre?: string;
  cedula?: string;
  telefono?: string;
  tipoIdentificacionId?: number;
  activo?: boolean;
}

export const useClientes = (filters: ClientesFilters = {}) => {
  const { page = 0, size, nombre, cedula, telefono, tipoIdentificacionId, activo } = filters;

  return useQuery<PageResponseClienteResponse>({
    queryKey: ["clientes", page, size ?? "default", nombre, cedula, telefono, tipoIdentificacionId, activo],
    queryFn: async () => {
      const params: ListarClientesParams = {
        pageable: { page },
      };
      if (size != null && size > 0) params.pageable.size = size;
      if (nombre) params.nombre = nombre;
      if (cedula) params.cedula = cedula;
      if (telefono) params.telefono = telefono;
      if (tipoIdentificacionId != null) params.tipoIdentificacionId = tipoIdentificacionId;
      if (activo != null) params.activo = activo;
      return await api.listarClientes(params);
    },

  });
};

export const useDetailsCliente = (id?: string) => {
  return useQuery({
    queryKey: ["cliente", id],
    enabled: !!id,
    queryFn: async () => {
      return await api.obtenerCliente(Number(id!));
    },
  });
};

export const useDeleteCliente = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      return await api.eliminarCliente(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["clientes"] });
      toast.success("Cliente eliminado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al eliminar cliente"));
    },
  });
};
