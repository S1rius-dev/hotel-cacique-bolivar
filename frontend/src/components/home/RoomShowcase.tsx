import { useState, useRef, useEffect } from "react";
import { ChevronRight, Camera, Search } from "lucide-react";

import RoomFeatures from "./RoomFeatures";
import type { Room } from "./rooms.data";
import { useNavigate } from "react-router-dom";
import ImageLightbox from "@/components/ui/ImageLightbox";

type RoomShowcaseProps = {
  room: Room;
};

export default function RoomShowcase({ room }: RoomShowcaseProps) {
  const navigate = useNavigate();
  const [lightboxOpen, setLightboxOpen] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [lastRoomId, setLastRoomId] = useState(room.id);
  const shouldAdvance = useRef(true);

  const hasImages = room.images.length > 0;

  // Al cambiar de habitacion se reinicia el carrusel. Se ajusta en render para
  // no pagar un render extra con el efecto.
  if (room.id !== lastRoomId) {
    setLastRoomId(room.id);
    setCurrentIndex(0);
  }

  useEffect(() => {
    if (!hasImages || lightboxOpen) return;
    const id = setInterval(() => {
      if (shouldAdvance.current) {
        setCurrentIndex((prev) => (prev + 1) % room.images.length);
      }
    }, 5000);
    return () => clearInterval(id);
  }, [hasImages, room.images.length, lightboxOpen]);

  const handleMouseEnter = () => {
    shouldAdvance.current = false;
  };
  const handleMouseLeave = () => {
    shouldAdvance.current = true;
  };

  const openLightbox = () => {
    setLightboxOpen(true);
  };

  const closeLightbox = () => {
    setLightboxOpen(false);
  };

  return (
    <>
      <style>{`@keyframes imageFadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
      }`}</style>

      <div className="grid lg:grid-cols-2 gap-10 lg:gap-14 items-center">
        <div className="relative group/img">
          <div
            aria-hidden
            className="
              absolute -inset-4 rounded-[2rem]
              bg-linear-to-br from-rose-200/25 via-rose-100/10 to-transparent
              blur-2xl opacity-70
              transition-opacity duration-700
              group-hover/img:opacity-100
            "
          />

          <div
            onClick={hasImages ? openLightbox : undefined}
            onMouseEnter={hasImages ? handleMouseEnter : undefined}
            onMouseLeave={hasImages ? handleMouseLeave : undefined}
            className={`
              relative overflow-hidden rounded-2xl
              shadow-[0_40px_80px_-30px_rgba(0,0,0,0.28)]
              ring-1 ring-stone-200/60
              aspect-4/3
              ${hasImages ? "cursor-pointer" : ""}
            `}>
            {hasImages ? (
              <>
                <img
                  key={currentIndex}
                  src={room.images[currentIndex]}
                  alt={room.name}
                  className="
                    w-full h-full object-cover
                    transition-transform duration-700 ease-out
                    group-hover/img:scale-[1.02]
                  "
                  style={{ animation: "imageFadeIn 500ms ease-out" }}
                />

                <div
                  className="
                    absolute inset-0
                    transition-opacity duration-300
                    opacity-0 group-hover/img:opacity-100
                    bg-black/15
                    flex items-center justify-center
                  "
                >
                  <div className="flex flex-col items-center gap-1.5 text-white">
                    <Search className="size-6 drop-shadow-sm" />
                    <span className="text-xs font-medium tracking-wide">
                      Ver fotografía
                    </span>
                  </div>
                </div>

                {room.images.length > 1 && (
                  <div className="absolute bottom-3 right-3">
                    <span
                      className="
                        inline-flex items-center px-2 py-1 rounded-md
                        bg-black/40 backdrop-blur-sm
                        text-white/80 text-[10px] font-medium tabular-nums
                      ">
                      {currentIndex + 1}/{room.images.length}
                    </span>
                  </div>
                )}
              </>
            ) : (
              <div className="w-full h-full flex flex-col items-center justify-center bg-stone-100 text-stone-400 gap-2">
                <Camera className="size-12" />
                <span className="text-sm font-medium">Próximamente</span>
                <span className="text-xs text-stone-300 text-center leading-tight max-w-50">
                  Estaremos subiendo imágenes pronto
                </span>
              </div>
            )}

            <div
              aria-hidden
              className="
                absolute top-0 left-0 right-0 h-0.5
                bg-linear-to-r from-transparent via-rose-400/90 to-transparent
              "
            />

            <div className="absolute top-5 left-5">
              <span
                className="
                  inline-flex items-center gap-2
                  px-3 py-1.5 rounded-full
                  bg-white/95 backdrop-blur-sm
                  text-stone-700
                  font-sans text-[10px] font-medium
                  uppercase tracking-[0.2em]
                  shadow-sm
                ">
                <span className="size-1.5 rounded-full bg-rose-500" />
                {room.tabLabel}
              </span>
            </div>
          </div>
        </div>

        <div className="flex flex-col">
          <h3
            className="
              font-heading text-3xl md:text-4xl lg:text-5xl
              text-stone-900 leading-tight tracking-tight
              mb-5
            ">
            {room.name}
          </h3>

          <div
            aria-hidden
            className="
              h-px w-12
              bg-linear-to-r from-rose-400 to-rose-200
              mb-8
            "
          />

          <RoomFeatures features={room.features} className="mb-8" />

          <p
            className="
              font-sans text-[15px] md:text-base
              leading-relaxed text-stone-500 font-light
              mb-8
            ">
            {room.description}
          </p>

          <a
            onClick={() => navigate("reservar")}
            target="_blank"
            rel="noopener noreferrer"
            className="
              group/link inline-flex items-center gap-2.5 self-start
              font-sans text-xs font-medium tracking-[0.2em] uppercase
              text-rose-600
              transition-all duration-300
              hover:text-rose-700
            ">
            <span className="relative cursor-pointer">
              Reservar ahora
              <span
                aria-hidden
                className="
                  absolute -bottom-px left-0 right-0 h-px
                  bg-rose-300 origin-right scale-x-100
                  transition-transform duration-300
                  group-hover/link:scale-x-0
                "
              />
              <span
                aria-hidden
                className="
                  absolute -bottom-px left-0 right-0 h-px
                  bg-rose-500 origin-left scale-x-0
                  transition-transform duration-300
                  group-hover/link:scale-x-100
                "
              />
            </span>
            <ChevronRight
              className="
                size-3.5
                transition-all duration-300
                group-hover/link:translate-x-1
              "
            />
          </a>
        </div>

        {hasImages && (
          <ImageLightbox
            images={room.images}
            initialIndex={currentIndex}
            open={lightboxOpen}
            onClose={closeLightbox}
          />
        )}
      </div>
    </>
  );
}
