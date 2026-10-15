import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getResenaController } from "@/api/generated/resena-controller/resena-controller";
import type { PageResponseResenaResponse, ListarResenasParams, ResenaRequest } from "@/api/generated/openAPIDefinition.schemas";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getResenaController();

export const useResenas = (page = 0, size = 20) => {
  return useQuery<PageResponseResenaResponse>({
    queryKey: ["resenas", page, size],
    queryFn: async () => {
      const params: ListarResenasParams = {
        pageable: { page, size },
      };
      return await api.listarResenas(params);
    },
  });
};

export const useCreateResena = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: ResenaRequest) => {
      return await api.crearResena(data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["resenas"] });
      toast.success("Reseña creada exitosamente");
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Error al crear la reseña"));
    },
  });
};
