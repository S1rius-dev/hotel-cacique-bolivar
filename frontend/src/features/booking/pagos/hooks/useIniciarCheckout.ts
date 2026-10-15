import { useMutation } from "@tanstack/react-query";
import { getCheckoutController } from "@/api/generated/checkout-controller/checkout-controller";
import type { CheckoutRequest } from "@/api/generated/openAPIDefinition.schemas";

const controller = getCheckoutController();

export function useIniciarCheckout() {
  return useMutation({
    mutationFn: (data: CheckoutRequest) => controller.iniciarCheckout(data),
  });
}
