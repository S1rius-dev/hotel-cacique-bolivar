import { useInView } from "@/lib/hooks/useInView";
import ServiceCard from "../components/ServiceCard";
import { services } from "./services.data";

export default function ServiciosSection() {
  const { ref, inView } = useInView(0.1);

  return (
    <section
      id="servicios"
      className="relative pt-12 md:pt-16 pb-12 md:pb-16 bg-stone-50 overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,var(--tw-gradient-stops))] from-rose-50/60 via-stone-50 to-transparent" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_left,var(--tw-gradient-stops))] from-rose-100/20 via-transparent to-transparent" />

      <div
        aria-hidden
        className="absolute inset-0 opacity-[0.015] pointer-events-none"
        style={{
          backgroundImage:
            "radial-gradient(circle at 1px 1px, #292524 1px, transparent 0)",
          backgroundSize: "40px 40px",
        }}
      />

      <div className="absolute top-0 right-0 w-96 h-96 bg-linear-to-bl from-rose-100/40 to-transparent rounded-full translate-x-1/2 -translate-y-1/2 blur-3xl" />
      <div className="absolute bottom-0 left-0 w-80 h-80 bg-linear-to-tr from-rose-100/30 to-transparent rounded-full -translate-x-1/2 translate-y-1/2 blur-3xl" />

      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 size-96 bg-rose-100/15 rounded-full blur-3xl pointer-events-none" />

      <div className="relative mx-auto max-w-[90rem] px-6">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-16 items-start">
          <div className="lg:col-span-4 lg:sticky lg:top-32">
            <div
              ref={ref}
              className={`
            relative text-center lg:text-left transition-all duration-700
            ${inView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
          `}>
              <p className="font-sans text-xs tracking-[0.3em] uppercase text-rose-500 font-medium mb-5">
                Comodidades Incluidas
              </p>

              <h2 className="font-heading text-[2.75rem] md:text-6xl lg:text-[4.25rem] font-medium text-stone-900 leading-[1.05] tracking-tight">
                Servicios de{" "}
                <span className="italic font-normal">nuestro hotel</span>
              </h2>

              <div className="mx-auto lg:mx-0 mt-6 h-px w-16 bg-linear-to-r from-rose-300 via-rose-400 to-rose-300" />

              <p className="mt-6 font-sans text-base md:text-lg leading-relaxed text-stone-500 max-w-md font-light">
                Todo lo que necesitas para una estancia cómoda en Amalfi,
                Antioquia: agua caliente, WiFi sin costo y recepción las 24
                horas.
              </p>
            </div>
          </div>

          <div className="lg:col-span-8">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 auto-rows-[minmax(150px,auto)] gap-5 lg:gap-6">
              <div className="sm:col-span-2 lg:col-span-3">
                <ServiceCard
                  service={services[5]}
                  index={5}
                  variant="featured"
                />
              </div>
              <ServiceCard service={services[0]} index={0} />
              <ServiceCard service={services[1]} index={1} />
              <ServiceCard service={services[2]} index={2} />
              <ServiceCard service={services[3]} index={3} />
              <ServiceCard service={services[4]} index={4} />
              <ServiceCard service={services[6]} index={6} />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
