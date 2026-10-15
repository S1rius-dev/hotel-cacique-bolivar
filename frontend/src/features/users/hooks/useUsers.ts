import { useQuery } from "@tanstack/react-query";
import { getUserController } from "@/api/generated/user-controller/user-controller";
import type { PageResponseUserResponse } from "@/api/generated/openAPIDefinition.schemas";

export const useUsers = (page = 0, size?: number) => {
  const api = getUserController();

  return useQuery<PageResponseUserResponse>({
    queryKey: ["users", page, size ?? "default"],
    queryFn: () => {
      const params: { page: number; size?: number } = { page };
      if (size != null && size > 0) params.size = size;
      return api.listarUsuarios({ pageable: params });
    },
  });
};
