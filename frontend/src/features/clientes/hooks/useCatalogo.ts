import { useQuery } from "@tanstack/react-query";

import { getCatalogoController } from "@/api/generated/catalogo-controller/catalogo-controller";

interface Props {
  municipioSearch?: string;
}

export const useClientCatalogos = ({ municipioSearch = "" }: Props = {}) => {
  const api = getCatalogoController();

  /**
   * CATÁLOGO ESTÁTICO
   */
  const tiposIdentificacion = useQuery({
    queryKey: ["catalogos", "tipos-identificacion"],

    queryFn: () => api.obtenerTiposIdentificacion(),

    staleTime: Infinity,
  });

  /**
   * BÚSQUEDA DINÁMICA
   */
  const municipios = useQuery({
    queryKey: ["municipios", municipioSearch],

    queryFn: () =>
      api.buscarMunicipios({
        search: municipioSearch,
      }),

    enabled: municipioSearch.length >= 2,

    staleTime: 1000 * 60 * 5,
  });

  return {
    tiposIdentificacion: tiposIdentificacion.data ?? [],

    municipios: municipios.data ?? [],

    isLoading: tiposIdentificacion.isLoading || municipios.isLoading,

    error: tiposIdentificacion.error || municipios.error,
  };
};
