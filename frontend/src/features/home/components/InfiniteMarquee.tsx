import { useEffect, useMemo, useCallback, useRef, useSyncExternalStore } from "react";
import MarqueeCard from "./MarqueeCard";
import type { ResenaResponse } from "@/api/generated/openAPIDefinition.schemas";
import { prefetchProfileImage } from "@/lib/profile-image-cache";

interface Props {
  reviews: ResenaResponse[];
  isLoading?: boolean;
}

function subscribeReducedMotion(callback: () => void) {
  const mql = window.matchMedia("(prefers-reduced-motion: reduce)");
  mql.addEventListener("change", callback);
  return () => mql.removeEventListener("change", callback);
}

function getReducedMotionSnapshot() {
  return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

const SPEED = 35;

function Skeleton() {
  return (
    <div className="flex gap-5" aria-hidden>
      {Array.from({ length: 6 }).map((_, i) => (
        <div
          key={i}
          className="w-60 sm:w-72 md:w-80 lg:w-88 shrink-0 rounded-xl border border-stone-100 bg-white p-5 animate-pulse space-y-3"
        >
          <div className="h-4 w-24 rounded bg-rose-100" />
          <div className="space-y-2">
            <div className="h-3 w-full rounded bg-stone-100" />
            <div className="h-3 w-3/4 rounded bg-stone-100" />
          </div>
          <div className="flex items-center gap-3 pt-2 border-t border-stone-100">
            <div className="size-9 rounded-full bg-rose-100" />
            <div className="space-y-1.5">
              <div className="h-3 w-20 rounded bg-stone-100" />
              <div className="h-2.5 w-16 rounded bg-stone-100" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default function InfiniteMarquee({ reviews, isLoading }: Props) {
  const reducedMotion = useSyncExternalStore(subscribeReducedMotion, getReducedMotionSnapshot);

  const trackRef = useRef<HTMLDivElement>(null);
  const posRef = useRef(0);
  const rafRef = useRef(0);
  const lastTimeRef = useRef(0);
  const isPausedRef = useRef(false);
  const shouldRunRef = useRef(false);
  const setWidthRef = useRef(0);

  const shouldAnimate = reviews.length >= 2 && !reducedMotion;

  const duplicatedContent = useMemo(
    () => [...reviews, ...reviews],
    [reviews],
  );

  const handleMouseEnter = useCallback(() => {
    isPausedRef.current = true;
  }, []);

  const handleMouseLeave = useCallback(() => {
    isPausedRef.current = false;
  }, []);

  useEffect(() => {
    const urls = [...new Set(reviews.map((r) => r.pictureUrl).filter(Boolean))] as string[];
    urls.forEach(prefetchProfileImage);
  }, [reviews]);

  useEffect(() => {
    const track = trackRef.current;
    if (!shouldAnimate || !track) return;

    shouldRunRef.current = true;
    posRef.current = 0;
    lastTimeRef.current = 0;

    const measure = () => {
      if (track) {
        setWidthRef.current = track.scrollWidth / 2;
      }
    };

    measure();

    const ro = new ResizeObserver(measure);
    ro.observe(track);

    const step = (time: number) => {
      if (!shouldRunRef.current) return;

      if (isPausedRef.current) {
        rafRef.current = requestAnimationFrame(step);
        return;
      }

      if (!lastTimeRef.current) lastTimeRef.current = time;
      const rawDelta = time - lastTimeRef.current;
      const delta = Math.min(rawDelta, 50);
      lastTimeRef.current = time;

      const setWidth = setWidthRef.current;
      if (!setWidth) {
        rafRef.current = requestAnimationFrame(step);
        return;
      }

      posRef.current -= SPEED * (delta / 1000);

      if (posRef.current <= -setWidth) {
        posRef.current += setWidth;
      }

      track.style.transform = `translate3d(${posRef.current}px, 0, 0)`;

      rafRef.current = requestAnimationFrame(step);
    };

    rafRef.current = requestAnimationFrame(step);

    return () => {
      shouldRunRef.current = false;
      cancelAnimationFrame(rafRef.current);
      ro.disconnect();
    };
  }, [shouldAnimate]);

  if (isLoading) {
    return (
      <div className="overflow-hidden">
        <Skeleton />
      </div>
    );
  }

  if (!reviews.length) return null;

  if (reviews.length < 2 || reducedMotion) {
    return (
      <div className="relative overflow-hidden">
        <div className="flex gap-5">
          {reviews.map((review) => (
            <MarqueeCard key={review.id} review={review} />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div
      className="relative overflow-hidden"
      style={{
        maskImage: "linear-gradient(to right, transparent 0%, black 2%, black 98%, transparent 100%)",
        WebkitMaskImage: "linear-gradient(to right, transparent 0%, black 2%, black 98%, transparent 100%)",
      }}
    >
      <div
        ref={trackRef}
        className="flex w-max gap-5"
        style={{ willChange: "transform" }}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
      >
        {duplicatedContent.map((review, i) => (
          <MarqueeCard key={`${review.id}-${i}`} review={review} />
        ))}
      </div>
    </div>
  );
}
