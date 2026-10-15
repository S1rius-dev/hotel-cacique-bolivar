import { useCallback, useEffect, useState, useRef } from "react";
import HeroButton from "./HeroButton";

export interface HeroSlide {
  src: string;
  /**
   * Solo la primera imagen del carrusel describe el contenido: es la única
   * que un motor de búsqueda puede ver y la única que tiene sentido leer para
   * quien usa lector de pantalla. Las demás están ocultas tras el overlay, así
   * que se marcan como decorativas.
   */
  alt: string;
}

interface HeroCarouselProps {
  slides: HeroSlide[];
  interval?: number;
}

export default function Hero({
  slides,
  interval = 5000,
}: HeroCarouselProps) {
  const [index, setIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const intervalRef = useRef<number | null>(null);

  const total = slides.length;

  const next = useCallback(
    () => setIndex((prev) => (total ? (prev + 1) % total : 0)),
    [total],
  );

  const prev = useCallback(
    () => setIndex((prev) => (total ? (prev - 1 + total) % total : 0)),
    [total],
  );

  useEffect(() => {
    if (!total) return;

    if (!isPaused) {
      if (intervalRef.current !== null) clearInterval(intervalRef.current);
      intervalRef.current = window.setInterval(next, interval);
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [isPaused, interval, total, next]);

  return (
    <div
      className="absolute inset-0 w-full h-full"
      onMouseEnter={() => setIsPaused(true)}
      onMouseLeave={() => setIsPaused(false)}>
      {slides.map((slide, i) => {
        const isActive = i === index;
        return (
          <img
            key={slide.src}
            src={slide.src}
            alt={isActive ? slide.alt : ""}
            aria-hidden={isActive ? undefined : true}
            loading={i === 0 ? "eager" : "lazy"}
            decoding={i === 0 ? "sync" : "async"}
            className={`
              absolute inset-0 w-full h-full object-cover
              transition-all duration-[1.5s] ease-in-out
              ${isActive ? "opacity-100 scale-100" : "opacity-0 scale-110"}
            `}
          />
        );
      })}

      <div className="absolute inset-0 bg-black/55 sm:bg-black/55 md:bg-black/55 lg:bg-black/65" />

      {total > 1 && (
        <div className="absolute bottom-6 left-1/2 -translate-x-1/2 flex gap-2 z-10">
          {slides.map((slide, i) => (
            <button
              key={slide.src}
              type="button"
              onClick={() => setIndex(i)}
              aria-label={`Ver imagen ${i + 1}`}
              className={`
                rounded-full transition-all duration-500 cursor-pointer
                ${i === index ? "w-8 h-1.5 bg-white" : "w-1.5 h-1.5 bg-white/40 hover:bg-white/70"}
              `}
            />
          ))}
        </div>
      )}

      <HeroButton prev={prev} next={next} />
    </div>
  );
}
