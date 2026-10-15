import { Camera } from "lucide-react";
import SectionHeader from "../components/SectionHeader";

export default function GaleriaSection() {
  return (
    <section
      id="galeria"
      className="relative pt-12 md:pt-16 pb-12 md:pb-16 bg-stone-50 overflow-hidden"
    >
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,var(--tw-gradient-stops))] from-rose-50/60 via-stone-50 to-transparent" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_right,var(--tw-gradient-stops))] from-rose-100/20 via-transparent to-transparent" />
      <div
        aria-hidden
        className="absolute inset-0 opacity-[0.015] pointer-events-none"
        style={{
          backgroundImage: "radial-gradient(circle at 1px 1px, #292524 1px, transparent 0)",
          backgroundSize: "40px 40px",
        }}
      />
      <div
        aria-hidden
        className="absolute top-24 right-16 size-80 bg-rose-200/20 rounded-full blur-3xl pointer-events-none"
      />
      <div
        aria-hidden
        className="absolute bottom-32 left-12 size-72 bg-rose-300/15 rounded-full blur-3xl pointer-events-none"
      />

      <div className="relative mx-auto max-w-[90rem] px-6">
        <SectionHeader
          label="Hotel Cacique de Bolivar"
          title={<>Galería<span aria-hidden className="absolute -bottom-2 left-1/2 -translate-x-1/2 w-12 h-px bg-rose-400/60" /></>}
          description="Cada espacio del hotel cuenta una historia. Así es nuestro hogar en Amalfi."
        />

        <div className="flex flex-col items-center justify-center py-20 text-stone-400 gap-3">
          <Camera className="size-16" />
          <span className="text-lg font-medium">Próximamente</span>
          <span className="text-sm text-stone-300 text-center max-w-xs leading-relaxed">
            Estaremos subiendo imágenes de nuestras instalaciones pronto.
          </span>
        </div>
      </div>
    </section>
  );
}
