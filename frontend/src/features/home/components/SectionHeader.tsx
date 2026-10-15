import { useInView } from "@/lib/hooks/useInView";
import type { ReactNode } from "react";

interface Props {
  label: string;
  title: ReactNode;
  description: ReactNode;
}

export default function SectionHeader({ label, title, description }: Props) {
  const { ref, inView } = useInView(0.1);

  return (
    <div
      ref={ref}
      className={`
        relative text-center mb-16 md:mb-20 transition-all duration-700
        ${inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
      `}>
      <div
        aria-hidden
        className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-24 bg-rose-200/20 rounded-full blur-3xl"
      />
      <div
        aria-hidden
        className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-72 h-px bg-linear-to-r from-transparent via-rose-300/50 to-transparent"
      />

      <p className="relative font-sans text-xs tracking-[0.3em] uppercase text-rose-500 font-medium mb-5">
        {label}
      </p>
      <h2 className="relative font-heading text-[2.75rem] md:text-6xl lg:text-[5rem] font-medium text-stone-900 leading-[1.05] tracking-tight">
        {title}
      </h2>
      <div className="relative mx-auto mt-6 h-px w-16 bg-linear-to-r from-rose-300 via-rose-400 to-rose-300" />
      <p className="relative mt-6 font-sans text-base md:text-lg leading-relaxed text-stone-500 max-w-2xl mx-auto font-light">
        {description}
      </p>
    </div>
  );
}
