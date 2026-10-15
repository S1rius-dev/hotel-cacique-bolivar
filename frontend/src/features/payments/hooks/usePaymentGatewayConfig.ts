import { useQuery } from "@tanstack/react-query";
import { customInstance } from "@/api/custom-instance";

export interface PaymentGatewayMetadata {
  gateway: string;
  name: string;
  displayName: string;
  securePaymentText: string;
  logoUrl: string | null;
  currency: string;
}

const FALLBACK: PaymentGatewayMetadata = {
  gateway: "WOMPI",
  name: "WOMPI",
  displayName: "Wompi",
  securePaymentText: "Pago seguro con Wompi",
  logoUrl: null,
  currency: "COP",
};

async function fetchGatewayMetadata(): Promise<PaymentGatewayMetadata> {
  return customInstance<PaymentGatewayMetadata>({
    url: "/api/public/payments/gateway",
    method: "GET",
  });
}

export function usePaymentGatewayConfig() {
  const query = useQuery({
    queryKey: ["payment-gateway-config"],
    queryFn: fetchGatewayMetadata,
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });

  const isLoading = query.isLoading;
  const isError = query.isError;

  // Durante loading no afirmar Wompi: gateway null evita flash
  // Solo usar FALLBACK en error (backend no disponible)
  const gateway = query.data ?? (isLoading ? null : isError ? FALLBACK : FALLBACK);
  // Si está cargando, no asumir ni Wompi ni Bold
  const effectiveGateway = query.data?.gateway ?? (isLoading ? null : FALLBACK.gateway);

  return {
    ...query,
    gateway: gateway as PaymentGatewayMetadata | null,
    isLoading,
    isError,
    isSuccess: query.isSuccess,
    isWompi: effectiveGateway === "WOMPI",
    isBold: effectiveGateway === "BOLD",
    fallback: FALLBACK,
  };
}

export const PAYMENT_GATEWAY_FALLBACK = FALLBACK;
