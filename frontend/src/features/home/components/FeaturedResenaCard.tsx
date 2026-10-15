import { AnimatePresence, motion } from "motion/react";
import { ChevronLeft, ChevronRight, Quote } from "lucide-react";
import { Rating } from "@/components/ui/rating";
import { cn } from "@/lib/utils";
import type { ResenaResponse } from "@/api/generated/openAPIDefinition.schemas";
import { useProfileImage } from "@/lib/useProfileImage";

interface Props {
  reviews: ResenaResponse[];
  currentIndex: number;
  onPrev: () => void;
  onNext: () => void;
  isLoading?: boolean;
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return "";
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString("es-ES", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  } catch {
    return dateStr;
  }
}

function FeaturedSkeleton() {
  return (
    <div className="relative overflow-hidden rounded-2xl border border-stone-100 bg-linear-to-b from-white via-rose-50/40 to-white p-6 lg:p-8 animate-pulse shadow-sm">
      <div className="space-y-4">
        <div className="h-5 w-32 rounded bg-rose-100" />
        <div className="space-y-3">
          <div className="h-5 w-full rounded bg-stone-100" />
          <div className="h-5 w-3/4 rounded bg-stone-100" />
        </div>
        <div className="flex items-center gap-3 pt-4">
          <div className="size-12 rounded-full bg-rose-100" />
          <div className="space-y-2">
            <div className="h-4 w-28 rounded bg-stone-100" />
            <div className="h-3 w-20 rounded bg-stone-100" />
          </div>
        </div>
      </div>
    </div>
  );
}

export default function FeaturedResenaCard({
  reviews,
  currentIndex,
  onPrev,
  onNext,
  isLoading,
}: Props) {
  const imgSrc = useProfileImage(reviews[currentIndex]?.pictureUrl);

  if (isLoading) return <FeaturedSkeleton />;

  if (!reviews.length) {
    return (
      <div className="flex items-center justify-center rounded-2xl border border-stone-100 bg-linear-to-b from-white via-rose-50/40 to-white p-10 text-center shadow-sm">
        <p className="text-stone-400 text-lg">Sé el primero en dejar una reseña</p>
      </div>
    );
  }

  const review = reviews[currentIndex];
  const total = reviews.length;
  const hasMultiple = total > 1;

  return (
    <div className="relative overflow-hidden rounded-2xl border border-stone-100 bg-linear-to-b from-white via-rose-50/40 to-white shadow-[0_8px_24px_-12px_rgba(28,25,23,0.12),0_2px_4px_rgba(244,63,94,0.03)] hover:shadow-[0_25px_50px_-20px_rgba(244,63,94,0.3)] transition-shadow duration-500">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,rgba(244,63,94,.06),transparent_45%)] pointer-events-none" />
      <Quote className="absolute top-6 right-6 size-8 text-rose-200/40 rotate-180" />

      <div className="relative p-6 lg:p-8">
        <AnimatePresence mode="wait">
          <motion.div
            key={review.id ?? currentIndex}
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -16 }}
            transition={{ duration: 0.3, ease: "easeInOut" }}
          >
            <Rating
              value={review.calificacion ?? 0}
              variant="rose"
              size={20}
              readOnly
            />

            <p className="mt-5 text-lg lg:text-xl leading-relaxed text-stone-700 font-light italic">
              &ldquo;{review.comentario}&rdquo;
            </p>

            <div className="mt-6 flex items-center gap-3">
              {imgSrc ? (
                <img
                  src={imgSrc}
                  alt=""
                  className="size-12 rounded-full object-cover ring-2 ring-rose-200/50 shrink-0"
                />
              ) : (
                <div className="size-12 rounded-full flex items-center justify-center bg-linear-to-br from-rose-500/30 to-rose-700/20 border border-rose-300/20 text-rose-200 font-semibold text-base shrink-0">
                  {(review.name ?? "A").charAt(0).toUpperCase()}
                </div>
              )}
              <div>
                <p className="text-stone-900 font-medium">{review.name ?? "Anónimo"}</p>
                <div className="flex items-center gap-2 text-sm text-stone-500">
                  {review.ubicacion && <span>{review.ubicacion}</span>}
                  {review.ubicacion && review.createdAt && (
                    <span className="text-stone-300" aria-hidden>·</span>
                  )}
                  {review.createdAt && <span>{formatDate(review.createdAt)}</span>}
                </div>
              </div>
            </div>
          </motion.div>
        </AnimatePresence>

        {hasMultiple && (
          <div className="mt-8 flex items-center justify-between border-t border-stone-200/50 pt-5">
            <div className="flex gap-2">
              {reviews.slice(0, 5).map((_, i) => (
                <span
                  key={i}
                  className={cn(
                    "size-2 rounded-full transition-all duration-300",
                    i === currentIndex
                      ? "bg-rose-400 w-5"
                      : "bg-stone-300",
                  )}
                />
              ))}
              {total > 5 && (
                <span className="text-xs text-stone-400 self-center ml-1">
                  +{total - 5}
                </span>
              )}
            </div>

            <div className="flex gap-2">
              <button
                onClick={onPrev}
                disabled={currentIndex === 0}
                className="flex items-center justify-center size-9 rounded-full border border-stone-200 text-stone-500 hover:bg-rose-50 hover:border-rose-200/60 transition-all duration-200 disabled:opacity-30 disabled:cursor-not-allowed"
                aria-label="Reseña anterior"
              >
                <ChevronLeft className="size-4" />
              </button>
              <button
                onClick={onNext}
                disabled={currentIndex === total - 1}
                className="flex items-center justify-center size-9 rounded-full border border-stone-200 text-stone-500 hover:bg-rose-50 hover:border-rose-200/60 transition-all duration-200 disabled:opacity-30 disabled:cursor-not-allowed"
                aria-label="Siguiente reseña"
              >
                <ChevronRight className="size-4" />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
