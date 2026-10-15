import { useQuery } from "@tanstack/react-query";
import { getCheckoutController } from "@/api/generated/checkout-controller/checkout-controller";

const controller = getCheckoutController();

export function useConsultarStatus(reservaId: number | undefined, enabled = true) {
  return useQuery({
    queryKey: ["checkout-status", reservaId],
    queryFn: () => controller.consultarStatus({ reservaId: reservaId! }),
    enabled: enabled && reservaId != null,
    refetchInterval: (query) => {
      const data = query.state.data as unknown as { estadoPasarela?: string } | undefined;
      if (data?.estadoPasarela === "APPROVED" || data?.estadoPasarela === "EXPIRED" || data?.estadoPasarela === "VOIDED") {
        return false;
      }
      return 5000;
    },
    retry: false,
  });
}
