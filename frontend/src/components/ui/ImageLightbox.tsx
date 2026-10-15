import { useState, useEffect, useCallback } from "react";
import { createPortal } from "react-dom";
import { X, ChevronLeft, ChevronRight } from "lucide-react";

type ImageLightboxProps = {
  images: string[];
  initialIndex: number;
  open: boolean;
  onClose: () => void;
};

export default function ImageLightbox({
  images,
  initialIndex,
  open,
  onClose,
}: ImageLightboxProps) {
  const [currentIndex, setCurrentIndex] = useState(initialIndex);
  const [lastInitialIndex, setLastInitialIndex] = useState(initialIndex);

  // Ajuste en render (no en efecto): si el llamador cambia la imagen de
  // entrada, el carrusel vuelve a esa posicion sin un render extra.
  if (initialIndex !== lastInitialIndex) {
    setLastInitialIndex(initialIndex);
    setCurrentIndex(initialIndex);
  }

  const goNext = useCallback(() => {
    setCurrentIndex((prev) => (prev + 1) % images.length);
  }, [images.length]);

  const goPrev = useCallback(() => {
    setCurrentIndex((prev) => (prev - 1 + images.length) % images.length);
  }, [images.length]);

  useEffect(() => {
    if (!open) return;
    const handler = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
      if (e.key === "ArrowLeft") goPrev();
      if (e.key === "ArrowRight") goNext();
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [open, onClose, goNext, goPrev]);

  useEffect(() => {
    if (open) {
      document.body.style.overflow = "hidden";
      return () => {
        document.body.style.overflow = "";
      };
    }
  }, [open]);

  if (!open) return null;

  return createPortal(
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-black/85 backdrop-blur-xl"
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="relative flex items-center justify-center"
      >
        <img
          src={images[currentIndex]}
          alt={`${currentIndex + 1} de ${images.length}`}
          className="max-h-[90vh] max-w-[90vw] object-contain select-none"
        />
      </div>

      <button
        onClick={onClose}
        className="absolute top-4 right-4 p-2 text-white/70 hover:text-white transition-colors cursor-pointer"
        aria-label="Cerrar"
      >
        <X className="size-8" />
      </button>

      {images.length > 1 && (
        <>
          <button
            onClick={(e) => {
              e.stopPropagation();
              goPrev();
            }}
            className="absolute left-4 top-1/2 -translate-y-1/2 p-2 text-white/70 hover:text-white transition-colors cursor-pointer"
            aria-label="Anterior"
          >
            <ChevronLeft className="size-8" />
          </button>
          <button
            onClick={(e) => {
              e.stopPropagation();
              goNext();
            }}
            className="absolute right-4 top-1/2 -translate-y-1/2 p-2 text-white/70 hover:text-white transition-colors cursor-pointer"
            aria-label="Siguiente"
          >
            <ChevronRight className="size-8" />
          </button>
          <div className="absolute bottom-6 left-1/2 -translate-x-1/2 flex items-center gap-2">
            <span className="text-white/60 text-sm font-sans">
              {currentIndex + 1} / {images.length}
            </span>
          </div>
        </>
      )}
    </div>,
    document.body,
  );
}
