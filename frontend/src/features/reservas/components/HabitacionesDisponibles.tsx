"use client";

import { useState, useMemo } from "react";
import { useRoomAvailability } from "@/features/reservas/hooks/useRoomAvailability";
import { RoomFilters } from "./RoomFilters";
import { RoomGrid } from "./RoomGrid";
import { RoomSkeleton } from "./RoomSkeleton";
import { RoomEmptyState } from "./RoomEmptyState";
import type { SelectedRoom } from "@/features/reservas/types/reservas.types";

interface HabitacionesDisponiblesProps {
  checkIn: string;
  checkOut: string;
  selectedRooms: SelectedRoom[];
  onSelectRoom: (room: SelectedRoom) => void;
  onDeselectRoom: (numero: string) => void;
}

export const HabitacionesDisponibles: React.FC<
  HabitacionesDisponiblesProps
> = ({ checkIn, checkOut, selectedRooms, onSelectRoom, onDeselectRoom }) => {
  const { data: habitaciones, isLoading } = useRoomAvailability(
    checkIn,
    checkOut,
  );

  const [searchQuery, setSearchQuery] = useState("");
  const [tipoFilter, setTipoFilter] = useState("all");

  const tiposUnicos = useMemo(() => {
    const tipos = new Set<string>();
    (habitaciones ?? []).forEach((h) => {
      if (h.tipoNombre) tipos.add(h.tipoNombre);
    });
    return Array.from(tipos).sort();
  }, [habitaciones]);

  const filteredHabitaciones = useMemo(() => {
    return (habitaciones ?? []).filter((h) => {
      const numero = h.numero?.toLowerCase() ?? "";
      const matchesSearch =
        searchQuery === "" || numero.includes(searchQuery.toLowerCase());
      const matchesTipo = tipoFilter === "all" || h.tipoNombre === tipoFilter;
      return matchesSearch && matchesTipo;
    });
  }, [habitaciones, searchQuery, tipoFilter]);

  const habitacionesList = useMemo(() => habitaciones ?? [], [habitaciones]);

  const hasRooms = habitacionesList.length > 0;
  const hasResults = filteredHabitaciones.length > 0;

  return (
    <div className="space-y-4 flex flex-col flex-1 min-h-0 pb-2">
      <RoomFilters
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        tipoFilter={tipoFilter}
        onTipoChange={setTipoFilter}
        tiposUnicos={tiposUnicos}
        filteredCount={filteredHabitaciones.length}
        isLoading={isLoading}
      />

      {isLoading ? (
        <RoomSkeleton />
      ) : !hasRooms ? (
        <RoomEmptyState type="no-data" />
      ) : !hasResults ? (
        <RoomEmptyState type="no-results" />
      ) : (
        <RoomGrid
          habitaciones={filteredHabitaciones}
          selectedRooms={selectedRooms}
          onSelectRoom={onSelectRoom}
          onDeselectRoom={onDeselectRoom}
        />
      )}
    </div>
  );
};

export default HabitacionesDisponibles;
