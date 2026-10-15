import { useInView } from "@/lib/hooks/useInView";
import RoomTabs from "@/components/home/RoomTabs";
import { useRoomsWithPrices } from "../hooks/useRoomsWithPrices";

export default function HabitacionesSection() {
  const { ref: headingRef, inView: headingInView } = useInView(0.1);
  const { rooms } = useRoomsWithPrices();

  return (
    <section
      id="habitaciones"
      className="relative pt-12 md:pt-16 pb-12 md:pb-16 bg-stone-50 overflow-hidden"
    >
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,var(--tw-gradient-stops))] from-rose-50/60 via-stone-50 to-transparent" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_right,var(--tw-gradient-stops))] from-rose-100/20 via-transparent to-transparent" />
      <div
        aria-hidden
        className="absolute inset-0 opacity-[0.015] pointer-events-none"
        style={{
          backgroundImage: `radial-gradient(circle at 1px 1px, #292524 1px, transparent 0)`,
          backgroundSize: "40px 40px",
        }}
      />

      <div
        aria-hidden
        className="absolute top-16 right-10 size-96 bg-rose-200/15 rounded-full blur-3xl pointer-events-none"
      />
      <div
        aria-hidden
        className="absolute bottom-40 left-10 size-80 bg-rose-300/10 rounded-full blur-3xl pointer-events-none"
      />
      <div
        aria-hidden
        className="absolute top-1/3 left-1/2 -translate-x-1/2 size-72 bg-rose-100/20 rounded-full blur-3xl pointer-events-none"
      />

      <div className="relative mx-auto max-w-[90rem] px-6">
        <div
          ref={headingRef}
          className={`
            max-w-2xl mb-12 lg:mb-14
            transition-all duration-700
            ${headingInView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
          `}
        >
          <p className="font-sans text-[10px] tracking-[0.35em] uppercase text-rose-400 font-medium mb-3">
            Hotel Cacique de Bolivar
          </p>

          <h2 className="font-heading text-xl md:text-2xl lg:text-3xl text-stone-900 font-medium leading-snug tracking-tight">
            Nuestras Habitaciones
          </h2>

          <p className="mt-3 font-sans text-sm md:text-base leading-relaxed text-stone-400 max-w-lg font-light">
            Contamos con <strong className="text-stone-500">43 habitaciones</strong>, con tres opciones
            diseñadas para tu comodidad, ya sea que viajes solo, en pareja o
            en familia.
          </p>
        </div>

        <RoomTabs rooms={rooms} />
      </div>
    </section>
  );
}
