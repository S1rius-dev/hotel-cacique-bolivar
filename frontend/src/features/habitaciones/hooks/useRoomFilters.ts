import { useState, useMemo } from "react";
import type { HabitacionResponse } from "@/api/generated/openAPIDefinition.schemas";
import { extractTiposUnicos } from "@/features/reservas/utils/room.utils";

interface UseRoomFiltersOptions {
  habitaciones: HabitacionResponse[];
}

interface UseRoomFiltersReturn {
  searchQuery: string;
  setSearchQuery: (value: string) => void;
  estadoFilter: string;
  setEstadoFilter: (value: string) => void;
  tipoFilter: string;
  setTipoFilter: (value: string) => void;
  tiposUnicos: string[];
  filteredHabitaciones: HabitacionResponse[];
  disponiblesCount: number;
}

export const useRoomFilters = ({
  habitaciones,
}: UseRoomFiltersOptions): UseRoomFiltersReturn => {
  const [searchQuery, setSearchQuery] = useState("");
  const [estadoFilter, setEstadoFilter] = useState("all");
  const [tipoFilter, setTipoFilter] = useState("all");

  const tiposUnicos = useMemo(
    () => extractTiposUnicos(habitaciones),
    [habitaciones],
  );

  const filteredHabitaciones = useMemo(() => {
    return habitaciones.filter((h) => {
      const numero = h.numero?.toLowerCase() ?? "";
      const tipoNombre = h.tipo?.nombre ?? "";
      const matchesSearch =
        searchQuery === "" || numero.includes(searchQuery.toLowerCase());
      const matchesEstado = estadoFilter === "all" || h.estado === estadoFilter;
      const matchesTipo = tipoFilter === "all" || tipoNombre === tipoFilter;
      return matchesSearch && matchesEstado && matchesTipo;
    });
  }, [habitaciones, searchQuery, estadoFilter, tipoFilter]);

  const disponiblesCount = habitaciones.filter(
    (h) => h.estado === "DISPONIBLE",
  ).length;

  return {
    searchQuery,
    setSearchQuery,
    estadoFilter,
    setEstadoFilter,
    tipoFilter,
    setTipoFilter,
    tiposUnicos,
    filteredHabitaciones,
    disponiblesCount,
  };
};
