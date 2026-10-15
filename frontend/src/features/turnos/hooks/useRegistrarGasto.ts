import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getCajaController } from "@/api/generated/caja-controller/caja-controller";
import type { GastoRequest, RegistrarGastoParams } from "@/api/generated/openAPIDefinition.schemas";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export function useRegistrarGasto(
  params?: RegistrarGastoParams,
  onSuccess?: () => void,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: GastoRequest) =>
      getCajaController().registrarGasto(data, params),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["turno-activo"] });
      queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Gasto registrado correctamente");
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Error al registrar gasto"));
    },
  });
}
