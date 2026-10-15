import { useQuery } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";

export const useRoomAvailability = (checkIn: string, checkOut: string) => {
  return useQuery({
    queryKey: ["habitaciones-disponibles", checkIn, checkOut],
    queryFn: () =>
      getReservaController().consultarDisponibilidad({ checkIn, checkOut }),
    enabled: !!checkIn && !!checkOut,
  });
};
