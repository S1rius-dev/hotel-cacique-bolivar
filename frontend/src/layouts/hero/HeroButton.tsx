import { ChevronLeft, ChevronRight } from "lucide-react";

interface HeroButtonProps {
  prev: () => void;
  next: () => void;
}

export default function HeroButton({ prev, next }: HeroButtonProps) {
  return (
    <>
      <button
        onClick={prev}
        className="
          absolute left-4 md:left-8 top-1/2 -translate-y-1/2 z-10
          size-10 md:size-12 rounded-full
          bg-white/10 backdrop-blur-sm border border-white/20
          flex items-center justify-center
          text-white
          opacity-0 group-hover:opacity-100
          hover:bg-white/20 hover:scale-105
          transition-all duration-300
          cursor-pointer
        "
      >
        <ChevronLeft className="size-5" />
      </button>

      <button
        onClick={next}
        className="
          absolute right-4 md:right-8 top-1/2 -translate-y-1/2 z-10
          size-10 md:size-12 rounded-full
          bg-white/10 backdrop-blur-sm border border-white/20
          flex items-center justify-center
          text-white
          opacity-0 group-hover:opacity-100
          hover:bg-white/20 hover:scale-105
          transition-all duration-300
          cursor-pointer
        "
      >
        <ChevronRight className="size-5" />
      </button>
    </>
  );
}
