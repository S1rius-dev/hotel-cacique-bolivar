import { useInView } from "@/lib/hooks/useInView";
import type { LucideIcon } from "lucide-react";

interface ContactItem {
  icon: LucideIcon;
  label: string;
  value: string;
}

interface Props {
  item: ContactItem;
  index: number;
}

export default function ContactCard({ item, index }: Props) {
  const { ref, inView } = useInView(0.1);

  return (
    <div
      ref={ref}
      className={`
        group relative bg-linear-to-b from-white via-rose-50/20 to-white
        rounded-2xl p-6 border border-stone-100
        shadow-sm shadow-stone-200/50
        hover:border-rose-200/60
        hover:shadow-[0_20px_40px_-15px_rgba(244,63,94,0.25)]
        hover:-translate-y-2
        transition-all duration-500
        ${inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
      `}
      style={{ transitionDelay: `${index * 100}ms` }}>
      <div
        aria-hidden
        className="
          absolute -top-px left-4 right-4 h-px
          bg-linear-to-r from-transparent via-rose-300/50 to-transparent
          opacity-0 group-hover:opacity-100 transition-opacity duration-500
        "
      />

      <div
        aria-hidden
        className="
          absolute top-8 left-1/2 -translate-x-1/2 size-16
          bg-rose-300/15 rounded-full blur-2xl
          opacity-0 group-hover:opacity-100
          transition-opacity duration-500 pointer-events-none
        "
      />

      <div className="relative inline-flex items-center justify-center size-10 rounded-lg bg-linear-to-br from-rose-50 to-rose-100/50 text-rose-500 mb-4 transition-all duration-300 group-hover:bg-rose-500 group-hover:text-white group-hover:shadow-md group-hover:shadow-rose-500/25">
        <item.icon className="size-5" />
      </div>
      <p className="relative font-sans text-[11px] tracking-[0.2em] uppercase text-stone-400 font-medium mb-2">
        {item.label}
      </p>
      <p className="relative font-sans text-[15px] font-medium text-stone-800 leading-snug break-words">
        {item.value}
      </p>
    </div>
  );
}
