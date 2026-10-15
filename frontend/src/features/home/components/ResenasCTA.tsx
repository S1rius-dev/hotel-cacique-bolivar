import { motion } from "motion/react";
import { MessageCircle, ArrowRight } from "lucide-react";

interface Props {
  onWriteReview: () => void;
}

export default function ResenasCTA({ onWriteReview }: Props) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 24 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-80px" }}
      transition={{ duration: 0.5, ease: "easeOut" }}
      className="relative overflow-hidden rounded-2xl bg-linear-to-br from-stone-900 to-stone-950 border border-stone-800 shadow-xl shadow-black/20 hover:shadow-2xl hover:shadow-black/30 transition-all duration-500"
    >
      <div
        aria-hidden
        className="absolute -inset-px rounded-2xl bg-linear-to-br from-rose-500/20 via-transparent to-rose-400/10 opacity-50 pointer-events-none"
      />
      <div
        aria-hidden
        className="absolute top-0 right-0 size-60 bg-rose-500/10 rounded-full blur-3xl"
      />
      <div
        aria-hidden
        className="absolute bottom-0 left-0 size-40 bg-rose-400/5 rounded-full blur-3xl"
      />

      <div className="relative p-8 md:p-10 lg:p-14 text-center">
        <h3 className="text-2xl md:text-3xl lg:text-4xl text-white leading-tight font-medium">
          ¿Ya te hospedaste con nosotros?
        </h3>
        <p className="mt-3 text-stone-400 text-base md:text-lg max-w-lg mx-auto font-light">
          Comparte tu experiencia y ayuda a futuros huéspedes.
        </p>

        <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
          <button
            onClick={onWriteReview}
            className="group inline-flex items-center gap-2.5 rounded-full px-7 py-3.5 bg-rose-500 text-white font-sans text-xs font-medium tracking-[0.2em] uppercase transition-all duration-300 hover:bg-rose-400 hover:shadow-lg hover:shadow-rose-500/30 hover:-translate-y-0.5 cursor-pointer w-full sm:w-auto justify-center"
          >
            <MessageCircle className="size-4 transition-transform duration-300 group-hover:scale-110" />
            Escribir una reseña
          </button>

          <button
            disabled
            className="group inline-flex items-center gap-2.5 rounded-full px-7 py-3.5 border border-stone-700 text-stone-500 font-sans text-xs font-medium tracking-[0.2em] uppercase transition-all duration-300 cursor-not-allowed w-full sm:w-auto justify-center"
          >
            Ver todas las reseñas
            <ArrowRight className="size-4 opacity-40" />
          </button>
        </div>
      </div>
    </motion.div>
  );
}
