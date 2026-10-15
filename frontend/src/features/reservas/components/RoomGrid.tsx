import { useState } from "react";
import { RoomCard } from "./RoomCard";
import { OccupantCounterModal } from "./OccupantCounterModal";
import type { DisponibilidadResponse } from "@/api/generated/openAPIDefinition.schemas";
import type { SelectedRoom } from "@/features/reservas/types/reservas.types";

interface RoomGridProps {
  habitaciones: DisponibilidadResponse[];
  selectedRooms: SelectedRoom[];
  onSelectRoom: (room: SelectedRoom) => void;
  onDeselectRoom: (numero: string) => void;
}

export const RoomGrid: React.FC<RoomGridProps> = ({
  habitaciones,
  selectedRooms,
  onSelectRoom,
  onDeselectRoom,
}) => {
  const [roomToConfigure, setRoomToConfigure] =
    useState<DisponibilidadResponse | null>(null);

  const handleConfirmOccupants = (adultos: number, ninos: number) => {
    if (!roomToConfigure) return;
    const numero = roomToConfigure.numero ?? "";
    onSelectRoom({
      numero,
      tipo: roomToConfigure.tipoNombre ?? "Sin tipo",
      capacidad: roomToConfigure.capacidadMaxima ?? 0,
      precio: roomToConfigure.precioBase ?? 0,
      id: roomToConfigure.id,
      adultos,
      ninos,
    });
    setRoomToConfigure(null);
  };

  return (
    <>
      <div className="flex-1 min-h-0 overflow-y-auto">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 pb-3">
          {habitaciones.map((habitacion) => {
            const numero = habitacion.numero ?? "";
            const isSelected = selectedRooms.some(
              (room) => room.numero === numero,
            );

            return (
              <RoomCard
                key={numero}
                habitacion={habitacion}
                isSelected={isSelected}
                isDisabled={false}
                onSelect={() => setRoomToConfigure(habitacion)}
                onDeselect={() => onDeselectRoom(numero)}
              />
            );
          })}
        </div>
      </div>

      <OccupantCounterModal
        key={roomToConfigure?.numero ?? "none"}
        open={roomToConfigure !== null}
        roomNumber={roomToConfigure?.numero ?? ""}
        roomType={roomToConfigure?.tipoNombre ?? ""}
        capacidadMax={roomToConfigure?.capacidadMaxima ?? 0}
        capacidadBase={roomToConfigure?.capacidadBase ?? 0}
        precioPersonaExtra={roomToConfigure?.precioPersonaExtra ?? 0}
        defaultAdults={roomToConfigure?.capacidadBase ?? 1}
        onConfirm={handleConfirmOccupants}
        onCancel={() => setRoomToConfigure(null)}
      />
    </>
  );
};
