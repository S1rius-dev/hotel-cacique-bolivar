export const formatPrice = (price: number | undefined): string => {
  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    minimumFractionDigits: 0,
  }).format(price ?? 0);
};

export const isRoomSelected = (
  numero: string,
  selectedRooms: { numero: string }[],
): boolean => selectedRooms.some((r) => r.numero === numero);

export const isRoomDisabled = (estado: string): boolean => estado !== "DISPONIBLE";

export const extractTiposUnicos = (habitaciones: { tipo?: { nombre?: string } }[]): string[] => {
  const tipos = new Set<string>();
  habitaciones.forEach((h) => {
    const nombre = h.tipo?.nombre;
    if (nombre) tipos.add(nombre);
  });
  return Array.from(tipos).sort();
};