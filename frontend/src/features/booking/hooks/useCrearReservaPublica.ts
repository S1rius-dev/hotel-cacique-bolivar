import { useMutation } from "@tanstack/react-query";
import { getCustomerReservationController } from "@/api/generated/customer-reservation-controller/customer-reservation-controller";
import type { CustomerReservationRequest } from "@/api/generated/openAPIDefinition.schemas";


const controller = getCustomerReservationController();

export function useCrearReservaPublica() {
  return useMutation({
    mutationFn: (data: CustomerReservationRequest) => controller.crearReservaPublica(data),
  });
}
