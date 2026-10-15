import { Bed, Users, Bath, Square, type LucideIcon } from "lucide-react";
import type { TipoHabitacionPublicResponse } from "@/api/generated/openAPIDefinition.schemas";

import sencilla1 from "@/assets/sencilla/habitacion-sencilla-1.webp";
import sencilla2 from "@/assets/sencilla/habitacion-sencilla-2.webp";
import sencillaBano from "@/assets/sencilla/baño-sencilla.webp";
import pareja1 from "@/assets/pareja/habitacion-pareja-1.webp";
import pareja2 from "@/assets/pareja/habitacion-pareja-2.webp";
import parejaBano from "@/assets/pareja/baño-pareja.webp";
import dosCamas1 from "@/assets/dos-camas/habitacion-2camas-1.webp";
import dosCamas2 from "@/assets/dos-camas/habitacion-2camas-2.webp";
import dosCamasBano from "@/assets/dos-camas/baño-2camas.webp";

export type RoomFeature = {
  icon: LucideIcon;
  label: string;
};

export type Room = {
  id: string;
  tabLabel: string;
  name: string;
  description: string;
  images: string[];
  features: RoomFeature[];
  /**
   * Precio base por noche en COP.
   *
   * No se escribe aquí a mano: lo devuelve el backend en `precioBase` y se
   * inyecta con `withRoomPrices`. Antes vivían tres números fijos en este
   * archivo que ya no coincidían con lo que cobraba el hotel, y eso es
   * justamente lo que las políticas de datos estructurados de Google
   * prohíben: un `price` en el marcado que no aparece en la página.
   */
  pricePerNight?: number;
  capacity?: string;
  /**
   * Nombres con los que el backend puede llamar a este tipo. Sirve para
   * emparejar la tarjeta de marketing con la fila de tipos de habitación.
   * Se comparan sin tildes ni mayúsculas, así que tolera "Habitación
   * Sencilla", "SENCILLA" o "Simple".
   */
  apiAliases: string[];
};

/** Sin tildes, sin mayúsculas y sin espacios dobles, para comparar nombres. */
function normalize(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\s+/g, " ");
}

/** ¿El nombre que devuelve el backend corresponde a esta tarjeta? */
function matchesAlias(room: Room, nombre: string): boolean {
  const target = normalize(nombre);
  return target.length > 0 && room.apiAliases.some((alias) => target.includes(normalize(alias)));
}

export const rooms: Room[] = [
  {
    id: "sencilla",
    tabLabel: "Sencilla",
    name: "Habitación Sencilla",
    description:
      "Ideal para viajeros solos. Una cama cómoda en un ambiente acogedor con todo lo necesario para una estancia placentera.",
    images: [sencilla1, sencilla2, sencillaBano],
    features: [
      { icon: Users, label: "Min. 1 - Máx. 2 huéspedes (costo adicional)" },
      { icon: Bed, label: "1 cama" },
      { icon: Bath, label: "Ducha caliente" },
      { icon: Square, label: "Confortable" },
    ],
    capacity: "1-2 personas",
    apiAliases: ["sencilla", "simple"],
  },
  {
    id: "pareja",
    tabLabel: "Pareja",
    name: "Habitación Pareja",
    description:
      "Pensada para parejas que buscan descanso y privacidad. Una cama de 1.20 m en un espacio íntimo con todas las comodidades.",
    images: [pareja1, pareja2, parejaBano],
    features: [
      { icon: Users, label: "Máx. 2 huéspedes" },
      { icon: Bed, label: "1 cama 1.20 m" },
      { icon: Bath, label: "Ducha caliente" },
      { icon: Square, label: "Íntima" },
    ],
    capacity: "2 personas",
    apiAliases: ["pareja", "doble", "matrimonial"],
  },
  {
    id: "familiar",
    tabLabel: "Dos camas",
    name: "Habitación de dos camas",
    description:
      "Perfecta para familias o grupos. Amplia, con capacidad para hasta 4 personas y todas las amenidades que necesitas.",
    images: [dosCamas1, dosCamas2, dosCamasBano],
    features: [
      { icon: Users, label: "Min. 2 - Máx. 4 huéspedes (costo adicional)" },
      { icon: Bed, label: "2 camas" },
      { icon: Bath, label: "Ducha caliente" },
      { icon: Square, label: "Espaciosa" },
    ],
    capacity: "1-4 personas",
    apiAliases: ["dos camas", "2 camas", "familiar", "familia"],
  },
];

/**
 * Devuelve el tipo de habitación del backend que corresponde a esta tarjeta, o
 * `undefined` si todavía no existe con ese nombre.
 */
export function matchTipoHabitacion(
  room: Room,
  tipos: TipoHabitacionPublicResponse[] | undefined,
): TipoHabitacionPublicResponse | undefined {
  return tipos?.find((tipo) => matchesAlias(room, tipo.nombre ?? ""));
}

/**
 * Copia sobre las tarjetas del sitio los precios que reporta el backend.
 *
 * Los textos, las fotos y las amenidades viven en este archivo porque son
 * contenido editorial; los precios no, porque cambiarlos debe bastar con
 * editarlos en el panel de administración. Cuando la API no responde o el
 * nombre no empareja, `pricePerNight` queda sin definir y la interfaz muestra
 * "Consultar" en lugar de inventar una cifra.
 */
export function withRoomPrices(
  source: Room[],
  tipos: TipoHabitacionPublicResponse[] | undefined,
): Room[] {
  return source.map((room) => {
    const precioBase = matchTipoHabitacion(room, tipos)?.precioBase;
    const pricePerNight =
      typeof precioBase === "number" && precioBase > 0 ? precioBase : undefined;
    return { ...room, pricePerNight };
  });
}

/** Rango de precios para `priceRange` de schema.org, en COP. */
export function priceRangeFrom(rooms: Room[]): string | undefined {
  const precios = rooms
    .map((room) => room.pricePerNight)
    .filter((precio): precio is number => typeof precio === "number" && precio > 0)
    .sort((a, b) => a - b);
  if (precios.length === 0) return undefined;

  const min = precios[0];
  const max = precios[precios.length - 1];
  const format = (value: number) => `COP ${value.toLocaleString("es-CO")}`;
  return min === max ? format(min) : `${format(min)} - ${format(max)}`;
}

/** Precio más bajo de los tipos con precio conocido, para "desde $...". */
export function minPrice(rooms: Room[]): number | undefined {
  return rooms.reduce<number | undefined>((min, room) => {
    const precio = room.pricePerNight;
    if (typeof precio !== "number" || precio <= 0) return min;
    return min === undefined ? precio : Math.min(min, precio);
  }, undefined);
}

/**
 * Fotografías de un tipo de habitación concreto. Las imágenes no vienen de la
 * API, así que se localizan por el mismo alias que se usa para el precio.
 */
export function getRoomImages(nombre: string): string[] | undefined {
  return rooms.find((room) => matchesAlias(room, nombre))?.images;
}
