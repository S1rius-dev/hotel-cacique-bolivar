import { useQuery } from "@tanstack/react-query";
import { getCustomerReservationController } from "@/api/generated/customer-reservation-controller/customer-reservation-controller";


const controller = getCustomerReservationController();

export function useTiposHabitacion() {
  return useQuery({
    queryKey: ["tipos-habitacion-public"],
    queryFn: () => controller.listarTiposHabitacionPublic(),
  });
}
