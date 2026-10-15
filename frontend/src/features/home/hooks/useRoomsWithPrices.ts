import { useMemo } from "react";
import {
  minPrice,
  priceRangeFrom,
  rooms,
  withRoomPrices,
} from "@/components/home/rooms.data";
import { useTiposHabitacion } from "@/features/booking/hooks/useTiposHabitacion";

/**
 * Las tarjetas de habitación del sitio, con los precios que reporta el backend.
 *
 * `useTiposHabitacion` consulta `/api/public/reservas/tipos-habitacion`, el
 * mismo endpoint público que usa el flujo de reserva, así que la portada y el
 * formulario no pueden mostrar cifras distintas entre sí. Cuando los dos
 * montan a la vez, TanStack Query deduplica la petición.
 */
export function useRoomsWithPrices() {
  const { data: tipos } = useTiposHabitacion();

  return useMemo(() => {
    const roomsWithPrices = withRoomPrices(rooms, tipos);
    return {
      rooms: roomsWithPrices,
      priceRange: priceRangeFrom(roomsWithPrices),
      minPrice: minPrice(roomsWithPrices),
    };
  }, [tipos]);
}
