import { getCatalogoController } from "@/api/generated/catalogo-controller/catalogo-controller";
import { useQuery } from "@tanstack/react-query";

export const useRoles = () => {
  const api = getCatalogoController();

  return useQuery({
    queryKey: ["catalogos", "roles"],
    queryFn: () => api.obtenerRoles(),
    staleTime: Infinity,
  });
};