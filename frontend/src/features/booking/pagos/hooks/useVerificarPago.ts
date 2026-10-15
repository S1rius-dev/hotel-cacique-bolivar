import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getCheckoutController } from "@/api/generated/checkout-controller/checkout-controller";

const controller = getCheckoutController();

export function useVerificarPago() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (reservaId: number) => controller.verificar(reservaId),
    onSuccess: (_data, reservaId) => {
      qc.invalidateQueries({ queryKey: ["checkout-status", reservaId] });
    },
  });
}
