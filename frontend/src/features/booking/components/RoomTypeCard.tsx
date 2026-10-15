import { useState } from "react";
import { Users, UserPlus, Camera, AlertTriangle, XCircle, Search } from "lucide-react";
import type { BookingRoomType } from "@/features/booking/types";
import { roomAmenities } from "@/features/booking/constants/room-amenities";
import { getRoomImages } from "@/components/home/rooms.data";
import ImageLightbox from "@/components/ui/ImageLightbox";
import QuantitySelector from "./QuantitySelector";

interface Props {
  room: BookingRoomType;
  quantity: number;
  onQuantityChange: (value: number) => void;
  onSelect?: () => void;
  occupantSummary?: string;
  availableCount?: number;
  totalRooms?: number;
}

export default function RoomTypeCard({
  room,
  quantity,
  onQuantityChange,
  onSelect,
  occupantSummary,
  availableCount,
  totalRooms,
}: Props) {
  const formatPrice = (value: number) => `$${value.toLocaleString("es-CO")}`;

  const capacidadLabel = `${room.capacidadBase}${room.capacidadMaxima > room.capacidadBase ? `-${room.capacidadMaxima}` : ""} ${room.capacidadMaxima === 1 ? "persona" : "personas"}`;

  const roomImages = getRoomImages(room.nombre);
  const [lightboxOpen, setLightboxOpen] = useState(false);

  const noDisponibilidad = availableCount === 0;
  const bajaDisponibilidad =
    availableCount != null &&
    availableCount > 0 &&
    (availableCount <= 2 ||
      (totalRooms != null && availableCount / totalRooms < 0.3));

  return (
    <div
      className={`
        group relative
        flex flex-col sm:flex-row
        rounded-2xl
        overflow-hidden
        transition-all duration-300
        ${
          noDisponibilidad
            ? "bg-red-50 border border-red-200 opacity-70"
            : quantity > 0
              ? "ring-2 ring-rose-400/60 bg-white shadow-lg shadow-rose-900/8"
              : "bg-white border border-stone-200"
        }
        ${noDisponibilidad ? "" : "hover:shadow-xl"}
        ${
          noDisponibilidad
            ? ""
            : quantity > 0
              ? "hover:ring-rose-400"
              : "hover:border-rose-300/50 hover:shadow-rose-900/5"
        }
      `}>
      {/* Image */}
      <div
        className={`relative sm:w-60 lg:w-75 shrink-0 aspect-4/3 sm:aspect-auto overflow-hidden bg-stone-100 ${roomImages ? "group/img" : ""}`}
        onClick={roomImages ? () => setLightboxOpen(true) : undefined}>
        {roomImages ? (
          <>
            <img
              src={roomImages[0]}
              alt={room.nombre}
              className="size-full object-cover transition-transform duration-700 ease-out group-hover/img:scale-[1.02]"
            />
            <div className="absolute inset-0 transition-opacity duration-300 opacity-0 group-hover/img:opacity-100 bg-black/15 flex items-center justify-center pointer-events-none">
              <div className="flex flex-col items-center gap-1 text-white">
                <Search className="size-5 drop-shadow-sm" />
                <span className="text-[10px] font-medium tracking-wide">Ver fotografía</span>
              </div>
            </div>

            {roomImages.length > 1 && (
              <div className="absolute bottom-3 right-3">
                <span className="inline-flex items-center px-2 py-1 rounded-md bg-black/40 backdrop-blur-sm text-white/80 text-[10px] font-medium tabular-nums">
                  1/{roomImages.length}
                </span>
              </div>
            )}
          </>
        ) : (
          <div className="size-full flex flex-col items-center justify-center text-stone-400 gap-1.5 p-4">
            <Camera className="size-8" />
            <span className="text-xs font-medium">Próximamente</span>
          </div>
        )}

        <div
          onClick={(e) => e.stopPropagation()}
          className="absolute bottom-3 left-3 sm:top-3 sm:bottom-auto sm:right-3 sm:left-auto flex gap-2">
          <span
            className="
              inline-flex items-center gap-1.5
              px-3 py-1.5 rounded-full
              bg-white/95 backdrop-blur-sm
              text-stone-800
              text-xs font-semibold
              shadow-sm
            ">
            {formatPrice(room.precioNocheEfectivo ?? room.precioBase)}
            <span className="font-normal text-stone-400">/noche</span>
            {(room.precioNocheEfectivo != null && room.precioNocheEfectivo !== room.precioBase) && (
              <span className="ml-1 text-[10px] font-normal text-emerald-600">tarifa temporada</span>
            )}
            {(room.precioNocheEfectivo == null) && (
              <span className="ml-1 text-[10px] font-normal text-stone-400">desde</span>
            )}
          </span>
          {noDisponibilidad && (
            <span className="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-red-500 text-white text-xs font-semibold shadow-sm">
              <XCircle className="size-3" />
              Agotado
            </span>
          )}
          {bajaDisponibilidad && (
            <span className="inline-flex items-center gap-1 px-3 py-1.5 rounded-full bg-amber-500 text-white text-xs font-semibold shadow-sm">
              <AlertTriangle className="size-3" />
              {availableCount}{" "}
              {availableCount === 1 ? "disponible" : "disponibles"}
            </span>
          )}
          {quantity > 0 && (
            <span className="inline-flex items-center px-3 py-1.5 rounded-full bg-rose-500 text-white text-xs font-semibold shadow-sm">
              x{quantity}
            </span>
          )}
        </div>
      </div>

      {/* Info */}
      <div className="flex-1 flex flex-col p-5 lg:p-6">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h3 className="font-heading text-xl lg:text-2xl text-stone-900 leading-tight">
              {room.nombre}
            </h3>

            <div className="flex items-center gap-1.5 mt-1">
              <Users className="size-3.5 text-rose-500" />
              <span className="text-xs text-stone-500">{capacidadLabel}</span>
            </div>
            {noDisponibilidad && (
              <div className="flex items-center gap-1.5 mt-1.5">
                <XCircle className="size-3.5 text-red-500" />
                <span className="text-xs font-medium text-red-600">
                  No hay habitaciones disponibles
                </span>
              </div>
            )}
            {bajaDisponibilidad && (
              <div className="flex items-center gap-1.5 mt-1.5">
                <AlertTriangle className="size-3.5 text-amber-600" />
                <span className="text-xs font-medium text-amber-700">
                  ¡Quedan pocas habitaciones disponibles!
                </span>
              </div>
            )}
          </div>
        </div>

        <div aria-hidden className="h-px w-10 bg-rose-400/50 my-3" />

        <div className="flex flex-col gap-4">
          <p className="text-sm text-stone-500 leading-relaxed">
            {room.descripcion}
          </p>

          <div className="flex flex-wrap gap-x-5 gap-y-2">
            {roomAmenities.map((amenity) => {
              const Icon = amenity.icon;
              return (
                <div
                  key={amenity.label}
                  className="flex items-center gap-1.5 text-stone-400">
                  <Icon className="size-3.5" />
                  <span className="text-xs">{amenity.label}</span>
                </div>
              );
            })}
          </div>

          {(room.precioPersonaExtraEfectivo ?? room.precioPersonaExtra) > 0 && (
            <div className="flex items-center gap-1.5 text-xs text-stone-400">
              <UserPlus className="size-3.5" />
              <span>
                +{formatPrice(room.precioPersonaExtraEfectivo ?? room.precioPersonaExtra)} por persona extra
              </span>
            </div>
          )}
        </div>

        {/* Quantity + Select */}
        <div className="mt-auto pt-3 border-t border-stone-100 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs text-stone-400 font-medium uppercase tracking-wider">
              Cantidad
            </span>
            <QuantitySelector
              value={quantity}
              onChange={onQuantityChange}
              disabled={noDisponibilidad}
            />
          </div>

          {!noDisponibilidad && quantity > 0 && (
            <button
              type="button"
              onClick={onSelect}
              className={`
                w-full flex items-center justify-center gap-2
                px-4 py-2 rounded-xl text-sm font-semibold
                transition-all duration-200 cursor-pointer
                ${
                  occupantSummary
                    ? "bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200"
                    : "bg-stone-100 text-stone-600 hover:bg-stone-200 border border-stone-200"
                }
              `}>
              {occupantSummary ? (
                <>{occupantSummary} — Editar</>
              ) : (
                <>Seleccionar huéspedes</>
              )}
            </button>
          )}
        </div>
      </div>

      {roomImages && (
        <ImageLightbox
          images={roomImages}
          initialIndex={0}
          open={lightboxOpen}
          onClose={() => setLightboxOpen(false)}
        />
      )}
    </div>
  );
}
