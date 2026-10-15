import { useQuery } from "@tanstack/react-query";
import { getHabitacionController } from "@/api/generated/habitacion-controller/habitacion-controller";
import { format } from "date-fns";

export const useEstadoDiario = (fecha: Date) => {
  const fechaStr = format(fecha, "yyyy-MM-dd");

  return useQuery({
    queryKey: ["estado-diario", fechaStr],
    queryFn: async () => {
      return await getHabitacionController().estadoDiarioHabitaciones({
        fecha: fechaStr,
      });
    },
  });
};
