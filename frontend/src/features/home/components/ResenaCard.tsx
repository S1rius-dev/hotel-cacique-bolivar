import { motion } from "motion/react";
import { Rating } from "@/components/ui/rating";
import type { ResenaResponse } from "@/api/generated/openAPIDefinition.schemas";
import { useProfileImage } from "@/lib/useProfileImage";

interface Props {
  review: ResenaResponse;
  index: number;
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return "";
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString("es-ES", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  } catch {
    return dateStr;
  }
}

export default function ResenaCard({ review, index }: Props) {
  const imgSrc = useProfileImage(review.pictureUrl);

  return (
    <motion.article
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-50px" }}
      transition={{ duration: 0.4, delay: index * 0.08, ease: "easeOut" }}
      whileHover={{ y: -4, scale: 1.01 }}
      className="group rounded-xl border border-stone-100 bg-linear-to-b from-white via-rose-50/20 to-white p-5 shadow-[0_8px_24px_-12px_rgba(28,25,23,0.12),0_2px_4px_rgba(244,63,94,0.03)] transition-all duration-300 hover:border-rose-200/60 hover:shadow-[0_20px_50px_-20px_rgba(244,63,94,0.3)]"
    >
      <Rating
        value={review.calificacion ?? 0}
        variant="rose"
        size={16}
        readOnly
      />

      <p className="mt-3 leading-relaxed text-stone-600 text-sm line-clamp-2">
        &ldquo;{review.comentario}&rdquo;
      </p>

      <div className="mt-4 pt-4 border-t border-stone-200/50 flex items-center gap-3">
        {imgSrc ? (
          <img
            src={imgSrc}
            alt=""
            className="size-9 rounded-full object-cover ring-2 ring-rose-200/50 shrink-0"
          />
        ) : (
          <div className="size-9 rounded-full flex items-center justify-center bg-linear-to-br from-rose-500/30 to-rose-700/20 border border-rose-300/20 text-rose-200 font-semibold text-xs shrink-0">
            {(review.name ?? "A").charAt(0).toUpperCase()}
          </div>
        )}
        <div className="min-w-0">
          <p className="text-stone-900 font-medium text-sm truncate">
            {review.name ?? "Anónimo"}
          </p>
          <div className="flex items-center gap-1.5 text-xs text-stone-400">
            {review.ubicacion && (
              <span className="truncate max-w-24">{review.ubicacion}</span>
            )}
            {review.ubicacion && review.createdAt && (
              <span className="text-stone-300 shrink-0">·</span>
            )}
            {review.createdAt && (
              <span className="shrink-0">{formatDate(review.createdAt)}</span>
            )}
          </div>
        </div>
      </div>
    </motion.article>
  );
}
