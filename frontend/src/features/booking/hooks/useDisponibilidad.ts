import { useQuery } from "@tanstack/react-query";
import { getCustomerReservationController } from "@/api/generated/customer-reservation-controller/customer-reservation-controller";

const controller = getCustomerReservationController();

export function useDisponibilidad(checkIn: string, checkOut: string) {
  const query = useQuery({
    queryKey: ["disponibilidad-public", checkIn, checkOut],
    queryFn: () =>
      controller.consultarDisponibilidadPublic({ checkIn, checkOut }),
    enabled: !!checkIn && !!checkOut,
  });

  return {
    data: query.data,
    error: query.error,
    isLoading: query.isLoading,
  };
}
