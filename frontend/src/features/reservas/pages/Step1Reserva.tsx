"use client";

import { SearchIcon } from "lucide-react";
import FechasBar from "@/features/reservas/components/FechasCard";
import HabitacionesDisponibles from "@/features/reservas/components/HabitacionesDisponibles";
import ResumenReserva from "@/features/reservas/components/ResumenReserva";
import { TitularCard } from "@/features/reservas/components/TitularCard";
import type { SelectedRoom } from "@/features/reservas/types/reservas.types";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Step1ReservaProps {
  filtros: { checkIn: string; checkOut: string };
  selectedRooms: SelectedRoom[];
  titular: ClienteResponse | null;
  onFiltrosChange: (filtros: { checkIn: string; checkOut: string }) => void;
  onSelectedRoomsChange: (rooms: SelectedRoom[]) => void;
  onTitularChange: (titular: ClienteResponse | null) => void;
  onNextStep: () => void;
}

const Step1Reserva: React.FC<Step1ReservaProps> = ({
  filtros,
  selectedRooms,
  titular,
  onFiltrosChange,
  onSelectedRoomsChange,
  onTitularChange,
  onNextStep,
}) => {
  const calculateNights = () => {
    if (filtros.checkIn && filtros.checkOut) {
      const start = new Date(filtros.checkIn);
      const end = new Date(filtros.checkOut);
      const diffTime = Math.abs(end.getTime() - start.getTime());
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) || 1;
    }
    return 1;
  };

  const noches = calculateNights();

  const isNextDisabled =
    !filtros.checkIn ||
    !filtros.checkOut ||
    selectedRooms.length === 0 ||
    !titular;

  const nextDisabledReason = !filtros.checkIn
    ? "Selecciona fecha de check-in"
    : !filtros.checkOut
      ? "Selecciona fecha de check-out"
      : selectedRooms.length === 0
        ? "Selecciona al menos una habitación"
        : !titular
          ? "Selecciona un titular para la reserva"
          : "";

  const handleSelectRoom = (room: SelectedRoom) => {
    if (!selectedRooms.find((r) => r.numero === room.numero)) {
      onSelectedRoomsChange([...selectedRooms, room]);
    }
  };

  const handleDeselectRoom = (numero: string) => {
    onSelectedRoomsChange(selectedRooms.filter((r) => r.numero !== numero));
  };

  const handleToggleIva = (numero: string) => {
    onSelectedRoomsChange(
      selectedRooms.map((r) =>
        r.numero === numero
          ? { ...r, precioIncluyeIva: !(r.precioIncluyeIva ?? false) }
          : r,
      ),
    );
  };

  return (
    <div className="flex flex-col min-h-0 space-y-6 pb-8">
      <div className="bg-card border rounded-2xl p-5 space-y-4 shrink-0">
        <div className="flex items-center gap-2 pb-3 border-b border-zinc-200 dark:border-zinc-700">
          <SearchIcon className="size-5 text-rose-600" />
          <h3 className="font-semibold text-zinc-900 dark:text-white">
            Datos de la reserva
          </h3>
        </div>

        <FechasBar
          filtros={filtros}
          onChange={onFiltrosChange}
          noches={noches}
        />

        <TitularCard
          selectedCliente={titular}
          onSelectCliente={onTitularChange}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start lg:items-stretch flex-1 min-h-0 pb-4">
        <div className="lg:col-span-9 flex flex-col">
          <HabitacionesDisponibles
            checkIn={filtros.checkIn}
            checkOut={filtros.checkOut}
            selectedRooms={selectedRooms}
            onSelectRoom={handleSelectRoom}
            onDeselectRoom={handleDeselectRoom}
          />
        </div>

        <div className="lg:col-span-3 h-fit pb-2">
          <ResumenReserva
            currentStep={1}
            checkIn={filtros.checkIn}
            checkOut={filtros.checkOut}
            noches={noches}
            selectedRooms={selectedRooms.map((r) => ({
              numero: r.numero,
              tipo: r.tipo,
              precio: r.precio,
              adultos: r.adultos,
              ninos: r.ninos,
              precioIncluyeIva: r.precioIncluyeIva,
            }))}
            onRemoveRoom={handleDeselectRoom}
            onClearRooms={() => onSelectedRoomsChange([])}
            onToggleIva={handleToggleIva}
            onNextStep={onNextStep}
            isNextDisabled={isNextDisabled}
            nextDisabledReason={nextDisabledReason}
          />
        </div>
      </div>
    </div>
  );
};

export default Step1Reserva;
