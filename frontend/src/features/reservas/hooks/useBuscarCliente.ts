import { useQuery } from "@tanstack/react-query";
import { getClienteController } from "@/api/generated/cliente-controller/cliente-controller";
import type { PageResponseClienteResponse } from "@/api/generated/openAPIDefinition.schemas";
import { useDebounce } from "@/lib/hooks/useDebounce";

export const useBuscarCliente = (search?: string) => {
  const debouncedSearch = useDebounce(search, 400);
  const isFetchAllMode = debouncedSearch === undefined;
  const hasSearch = !isFetchAllMode && debouncedSearch.length >= 2;

  return useQuery({
    queryKey: isFetchAllMode
      ? ["clientes", "lista-completa"]
      : ["clientes", "buscar", debouncedSearch ?? ""],
    queryFn: async () => {
      if (isFetchAllMode) {
        const res = await getClienteController().listarClientes({
          pageable: { page: 0, size: 500 },
        });
        return res;
      }
      const data = await getClienteController().buscarClientes({
        q: debouncedSearch!,
        limit: 14,
      });
      return { content: data, totalElements: data.length, totalPages: 1 } as PageResponseClienteResponse;
    },
    enabled: isFetchAllMode || hasSearch,
    staleTime: isFetchAllMode ? 5 * 60 * 1000 : 30_000,
    placeholderData: isFetchAllMode ? undefined : (prev) => prev,
  });
};
