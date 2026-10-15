import { customInstance } from "@/api/custom-instance";
import { useQuery } from "@tanstack/react-query";
import type { PQRSResponse } from "../types";

export const usePqrsTracking = (codigo: string | null) => {
  return useQuery({
    queryKey: ["pqrs", "tracking", codigo],
    queryFn: async () => {
      return await customInstance<PQRSResponse>({
        url: `/api/pqrs/codigo/${encodeURIComponent(codigo!)}`,
        method: "GET",
      });
    },
    enabled: !!codigo,
    retry: false,
  });
};
