import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getCajaController } from "@/api/generated/caja-controller/caja-controller";
import type {
  AbrirTurnoParams,
  AbrirTurnoRequest,
  CerrarTurnoParams,
  CerrarTurnoRequest,
} from "@/api/generated/openAPIDefinition.schemas";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export function useAbrirTurno(
  params?: AbrirTurnoParams,
  onSuccess?: () => void,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: AbrirTurnoRequest) =>
      getCajaController().abrirTurno(data, params),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["turno-activo"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Turno abierto correctamente");
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Error al abrir turno"));
    },
  });
}

export function useCerrarTurno(
  params?: CerrarTurnoParams,
  onSuccess?: () => void,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CerrarTurnoRequest) =>
      getCajaController().cerrarTurno(data, params),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["turno-activo"] });
      queryClient.invalidateQueries({ queryKey: ["turno-historial"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Turno cerrado correctamente");
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Error al cerrar turno"));
    },
  });
}
