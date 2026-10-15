import { useInView } from "@/lib/hooks/useInView";

export default function MapEmbed() {
  const { ref, inView } = useInView(0.1);

  return (
    <div
      ref={ref}
      className={`
        relative h-125 rounded-2xl overflow-hidden
        shadow-lg shadow-stone-200/80
        hover:shadow-xl hover:shadow-rose-200/30
        ring-1 ring-stone-200
        transition-all duration-500
        ${inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
      `}>
      <div
        aria-hidden
        className="absolute -inset-1 rounded-2xl bg-linear-to-br from-rose-200/30 via-rose-100/15 to-transparent blur-2xl -z-10"
      />

      <iframe
        src="https://www.google.com/maps?q=Amalfi%2C+Antioquia%2C+Colombia&z=15&output=embed"
        width="100%"
        height="100%"
        style={{ border: 0 }}
        allowFullScreen
        loading="lazy"
        referrerPolicy="no-referrer-when-downgrade"
        title="Ubicación aproximada - Amalfi, Antioquia"
        className="grayscale-0 transition-all duration-700"
      />
    </div>
  );
}
