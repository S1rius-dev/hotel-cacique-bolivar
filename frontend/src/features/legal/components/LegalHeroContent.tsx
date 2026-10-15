import type { LucideIcon } from "lucide-react";
import { Link } from "react-router-dom";

interface Props {
  icon: LucideIcon;
  title: string;
  subtitle: string;
}

export default function LegalHeroContent({ icon: Icon, title, subtitle }: Props) {
  return (
    <>
      <div className="absolute inset-0 z-[1] bg-linear-to-t from-black/60 via-black/20 to-black/40" />
      <div className="absolute inset-0 z-10 flex items-center justify-center">
        <div className="text-center max-w-2xl mx-auto px-4 pt-20">
          <p className="font-sans text-[10px] sm:text-xs tracking-[0.35em] uppercase text-white/70 font-medium mb-5">
            Hotel Cacique de Bolivar
          </p>
          <h1 className="flex items-center justify-center gap-4 font-heading text-4xl sm:text-5xl md:text-6xl lg:text-7xl text-white font-medium tracking-tight">
            <Icon className="size-8 sm:size-10 md:size-12 text-rose-300 shrink-0" />
            <span>{title}</span>
          </h1>
          <p className="mt-4 text-white/70 text-sm sm:text-base max-w-lg mx-auto leading-relaxed">
            {subtitle}
          </p>
          <Link
            to="/"
            className="inline-flex items-center gap-2 mt-8 text-white/50 hover:text-white transition-colors text-sm font-sans"
          >
            <span aria-hidden>&larr;</span> Volver al inicio
          </Link>
        </div>
      </div>
    </>
  );
}
