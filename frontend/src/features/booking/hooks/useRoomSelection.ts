import { useState, useMemo } from "react";
import type { BookingRoomType, OccupantConfig } from "../types";

interface AvailabilityEntry {
  tipoHabitacionId: number;
  habitacionesDisponibles: number;
  totalHabitaciones: number;
}

export function useRoomSelection(
  tipos: BookingRoomType[],
  disponibilidad: AvailabilityEntry[] | undefined,
  nights: number,
) {
  // Solo se guardan los tipos con cantidad > 0. Los que no estan en el objeto se
  // leen como 0 via `selections[t.id] ?? 0`, asi que no hace falta sembrar una
  // entrada por tipo cuando llegan los datos.
  const [selections, setSelections] = useState<Record<number, number>>({});
  const [occupantConfig, setOccupantConfig] = useState<OccupantConfig>({});
  const [configModalType, setConfigModalType] = useState<number | null>(null);

  const availabilityMap = useMemo(() => {
    if (!disponibilidad) return null;
    const map: Record<number, { disponibles: number; total: number }> = {};
    for (const d of disponibilidad) {
      if (d.tipoHabitacionId != null && d.habitacionesDisponibles != null) {
        map[d.tipoHabitacionId] = {
          disponibles: d.habitacionesDisponibles,
          total: d.totalHabitaciones ?? 0,
        };
      }
    }
    return map;
  }, [disponibilidad]);

  const allUnavailable = useMemo(
    () => tipos.length > 0 && availabilityMap !== null && tipos.every((t) => (availabilityMap[t.id]?.disponibles ?? 0) === 0),
    [availabilityMap, tipos],
  );

  const totalRooms = Object.values(selections).reduce((a, b) => a + b, 0);

  const effectiveNoche = (t: BookingRoomType) => t.precioNocheEfectivo ?? t.precioBase;
  const effectiveExtra = (t: BookingRoomType) => t.precioPersonaExtraEfectivo ?? t.precioPersonaExtra;

  const totalPrice = useMemo(
    () => tipos.reduce(
      (sum, tipo) => sum + effectiveNoche(tipo) * (selections[tipo.id] ?? 0) * nights,
      0,
    ),
    [tipos, selections, nights],
  );

  const extraPersonCharge = useMemo(() => {
    return tipos.reduce((sum, tipo) => {
      const qty = selections[tipo.id] ?? 0;
      if (qty === 0) return sum;
      const config = occupantConfig[tipo.id];
      if (!config || config.length === 0) return sum;
      const rooms = Math.min(config.length, qty);
      const extra = config.slice(0, rooms).reduce((roomSum, occupants) => {
        const extraPeople = Math.max(0, occupants - tipo.capacidadBase);
        return roomSum + extraPeople * effectiveExtra(tipo) * nights;
      }, 0);
      return sum + extra;
    }, 0);
  }, [tipos, selections, occupantConfig, nights]);

  const totalGuests = useMemo(() => {
    const tiposWithConfig = tipos.filter((t) => (selections[t.id] ?? 0) > 0);
    return tiposWithConfig.reduce((sum, t) => {
      const config = occupantConfig[t.id];
      if (config && config.length > 0) {
        const rooms = Math.min(config.length, selections[t.id] ?? 0);
        return sum + config.slice(0, rooms).reduce((a, b) => a + b, 0);
      }
      return sum + (selections[t.id] ?? 0) * t.capacidadBase;
    }, 0);
  }, [tipos, selections, occupantConfig]);

  const handleQuantityChange = (roomId: number, value: number) => {
    setSelections((prev) => ({ ...prev, [roomId]: value }));
    setOccupantConfig((prev) => {
      const next = { ...prev };
      if (value === 0) {
        delete next[roomId];
      } else if (next[roomId] && next[roomId].length > value) {
        next[roomId] = next[roomId].slice(0, value);
      }
      return next;
    });
  };

  const configModalRoomType = useMemo(
    () => (configModalType !== null ? tipos.find((t) => t.id === configModalType) ?? null : null),
    [configModalType, tipos],
  );

  const handleSelectRoom = (roomId: number) => {
    setConfigModalType(roomId);
  };

  const getOccupantSummary = (roomId: number): string | undefined => {
    const config = occupantConfig[roomId];
    if (!config || config.length === 0) return undefined;
    const total = config.reduce((a, b) => a + b, 0);
    return `${total} ${total === 1 ? "huésped" : "huéspedes"}`;
  };

  return {
    selections,
    occupantConfig,
    setOccupantConfig,
    configModalType,
    setConfigModalType,
    configModalRoomType,
    availabilityMap,
    allUnavailable,
    totalRooms,
    totalPrice,
    extraPersonCharge,
    totalGuests,
    handleQuantityChange,
    handleSelectRoom,
    getOccupantSummary,
  };
}
