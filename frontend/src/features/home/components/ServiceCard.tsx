import { memo } from "react";
import { useInView } from "@/lib/hooks/useInView";
import type { LucideIcon } from "lucide-react";

interface Service {
  icon: LucideIcon;
  title: string;
  description: string;
  highlight?: string;
}

interface Props {
  service: Service;
  index: number;
  variant?: "default" | "featured" | "wide";
}

const ServiceCard = memo(function ServiceCard({ service, index, variant = "default" }: Props) {
  const { ref, inView } = useInView(0.1);

  const isFeatured = variant === "featured";
  const isWide = variant === "wide";

  return (
    <article
      ref={ref}
      className={`
        group relative overflow-hidden
        ${
          isFeatured
            ? "bg-linear-to-br from-stone-900 via-stone-900 to-rose-950 text-white border border-stone-800 p-8 lg:p-10"
            : isWide
              ? "bg-linear-to-br from-white via-rose-50/40 to-white border border-stone-100 p-7"
              : "bg-linear-to-b from-white via-rose-50/20 to-white border border-stone-100 p-6"
        }
        rounded-2xl
        ${
          isFeatured
            ? "shadow-[0_25px_60px_-25px_rgba(0,0,0,0.5),0_0_0_1px_rgba(244,63,94,0.1)] hover:shadow-[0_35px_80px_-25px_rgba(244,63,94,0.4)]"
            : "shadow-[0_8px_24px_-12px_rgba(28,25,23,0.12),0_2px_4px_rgba(244,63,94,0.03)] hover:shadow-[0_25px_50px_-20px_rgba(244,63,94,0.3)]"
        }
        ${isFeatured ? "hover:border-rose-500/50" : "hover:border-rose-200/60"}
        hover:-translate-y-2
        transition-all duration-500
        ${inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
      `}
      style={{ transitionDelay: `${index * 80}ms` }}>
      <div
        aria-hidden
        className={`
          absolute -top-px left-6 right-6 h-px
          bg-linear-to-r from-transparent ${
            isFeatured ? "via-rose-400/80" : "via-rose-300/60"
          } to-transparent
          opacity-0 group-hover:opacity-100 transition-opacity duration-500
        `}
      />

      <div
        aria-hidden
        className={`
          absolute top-12 left-1/2 -translate-x-1/2 ${
            isFeatured ? "size-40 bg-rose-500/25" : "size-24 bg-rose-300/20"
          } rounded-full blur-2xl
          opacity-0 group-hover:opacity-100
          transition-opacity duration-500 pointer-events-none
        `}
      />

      {isFeatured && (
        <>
          <div
            aria-hidden
            className="absolute -bottom-20 -right-20 size-64 bg-rose-500/10 rounded-full blur-3xl pointer-events-none"
          />
          <div
            aria-hidden
            className="absolute inset-0 opacity-[0.04] pointer-events-none"
            style={{
              backgroundImage: `radial-gradient(circle at 1px 1px, #fff 1px, transparent 0)`,
              backgroundSize: "24px 24px",
            }}
          />
        </>
      )}

      <div
        className={`
          relative mb-6 inline-flex items-center justify-center
          ${
            isFeatured
              ? "size-16 rounded-2xl bg-linear-to-br from-rose-500 to-rose-600 text-white shadow-lg shadow-rose-500/40 ring-1 ring-rose-400/50"
              : isWide
                ? "size-14 rounded-xl bg-linear-to-br from-rose-50 to-rose-100/50 text-rose-500 group-hover:bg-rose-500 group-hover:text-white group-hover:shadow-lg group-hover:shadow-rose-500/30"
                : "size-12 rounded-xl bg-linear-to-br from-rose-50 to-rose-100/50 text-rose-500 group-hover:bg-rose-500 group-hover:text-white group-hover:shadow-lg group-hover:shadow-rose-500/30"
          }
          transition-all duration-500
        `}>
        <service.icon
          className={`${isFeatured ? "size-7" : isWide ? "size-6" : "size-5"}`}
        />
      </div>

      <h3
        className={`
          relative font-heading leading-snug tracking-tight mb-3
          ${
            isFeatured
              ? "text-2xl lg:text-3xl font-medium text-white"
              : isWide
                ? "text-xl md:text-[22px] font-medium text-stone-900"
                : "text-lg font-medium text-stone-900"
          }
        `}>
        {service.title}
      </h3>
      <p
        className={`
          relative font-sans leading-relaxed font-light
          ${isFeatured ? "text-[15px] text-stone-300" : "text-sm text-stone-500"}
        `}>
        {service.description}
      </p>
      {service.highlight && (
        <span
          className={`
            relative inline-block mt-3 font-sans text-[10px]
            tracking-[0.2em] uppercase font-medium
            ${isFeatured ? "text-rose-300" : "text-rose-500"}
          `}>
          · {service.highlight}
        </span>
      )}
    </article>
  );
});

export default ServiceCard;
