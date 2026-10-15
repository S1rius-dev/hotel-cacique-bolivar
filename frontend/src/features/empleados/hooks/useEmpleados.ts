import { getEmpleadoController } from "@/api/generated/empleado-controller/empleado-controller";
import type {
  EmpleadoRequest,
  EmpleadoUpdateRequest,
} from "@/api/generated/openAPIDefinition.schemas";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export const useEmpleados = (page: number = 0, size: number = 14) => {
  const api = getEmpleadoController();
  return useQuery({
    queryKey: ["empleados", page, size],
    queryFn: () => api.listarEmpleados({ pageable: { page, size } }),
  });
};

export const useEmpleado = (id: number | null) => {
  const api = getEmpleadoController();
  return useQuery({
    queryKey: ["empleados", id],
    queryFn: () => api.obtenerEmpleado(id!),
    enabled: !!id,
  });
};

export const useCreateEmpleado = () => {
  const api = getEmpleadoController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: EmpleadoRequest) => api.crearEmpleado(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["empleados"] });
      toast.success("Empleado creado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al crear empleado"));
    },
  });
};

export const useUpdateEmpleado = () => {
  const api = getEmpleadoController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: EmpleadoUpdateRequest }) =>
      api.actualizarEmpleado(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["empleados"] });
      toast.success("Empleado actualizado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al actualizar empleado"));
    },
  });
};

export const useDeleteEmpleado = () => {
  const api = getEmpleadoController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => api.eliminarEmpleado(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["empleados"] });
      toast.success("Empleado eliminado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al eliminar empleado"));
    },
  });
};
