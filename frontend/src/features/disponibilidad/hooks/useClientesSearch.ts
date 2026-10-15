import { useQuery } from "@tanstack/react-query";
import { getClienteController } from "@/api/generated/cliente-controller/cliente-controller";
import { useDebounce } from "@/lib/hooks/useDebounce";

export const useClientesSearch = (search: string) => {
  const debouncedSearch = useDebounce(search, 400);

  const { data, ...rest } = useQuery({
    queryKey: ["clientes", "search", debouncedSearch],
    queryFn: async () => {
      const res = await getClienteController().buscarClientes({
        q: debouncedSearch,
        limit: 20,
      });
      return res;
    },
    enabled: debouncedSearch.length >= 2,
    staleTime: 30_000,
    placeholderData: (prev) => prev,
  });

  return { data: data ?? [], ...rest };
};
