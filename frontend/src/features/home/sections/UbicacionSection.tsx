import { lazy, Suspense } from "react";
import { MapPin, Phone, Mail, Clock, ChevronRight, Navigation } from "lucide-react";
import { useInView } from "@/lib/hooks/useInView";
import { useNavigate } from "react-router-dom";
import { HOTEL } from "@/config/hotel";
import ContactCard from "../components/ContactCard";

const MapEmbed = lazy(() => import("../components/MapEmbed"));

const contactInfo = [
  {
    icon: MapPin,
    label: "Dirección",
    value: "Calle 10 #5-20, Amalfi, Antioquia",
  },
  { icon: Phone, label: "Teléfono", value: "+57 300 0000000" },
  { icon: Mail, label: "Email", value: "contacto@example.com" },
  { icon: Clock, label: "Recepción", value: "Atención personalizada 24/7" },
];

export default function UbicacionSection() {
  const navigate = useNavigate();
  const { ref: headingRef, inView: headingInView } = useInView(0.1);
  const { ref: ctaRef, inView: ctaInView } = useInView(0.1);
  const { ref: infoRef, inView: infoInView } = useInView(0.1);

  return (
    <section
      id="ubicacion"
      className="relative pt-12 md:pt-16 pb-12 md:pb-16 bg-stone-50 overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,var(--tw-gradient-stops))] from-rose-50/50 via-stone-50 to-transparent" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_left,var(--tw-gradient-stops))] from-rose-100/15 via-transparent to-transparent" />
      <div
        aria-hidden
        className="absolute inset-0 opacity-[0.015] pointer-events-none"
        style={{
          backgroundImage: `radial-gradient(circle at 1px 1px, #292524 1px, transparent 0)`,
          backgroundSize: "40px 40px",
        }}
      />
      <div className="absolute top-0 left-0 w-full h-px bg-linear-to-r from-transparent via-rose-200 to-transparent" />

      <div
        aria-hidden
        className="absolute top-32 right-20 size-80 bg-rose-200/20 rounded-full blur-3xl pointer-events-none"
      />
      <div
        aria-hidden
        className="absolute bottom-40 left-10 size-72 bg-rose-300/15 rounded-full blur-3xl pointer-events-none"
      />
      <div
        aria-hidden
        className="absolute top-1/3 left-1/3 size-64 bg-rose-100/15 rounded-full blur-3xl pointer-events-none"
      />

      <div className="relative mx-auto max-w-360 px-6">
        <div className="grid lg:grid-cols-12 gap-10 lg:gap-16 items-start">
          <div
            ref={headingRef}
            className={`
              lg:col-span-4 lg:sticky lg:top-32
              text-center lg:text-left
              transition-all duration-700
              ${headingInView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
            `}>
            <p className="font-sans text-xs tracking-[0.3em] uppercase text-rose-500 font-medium mb-5">
              Encuéntranos
            </p>

            <h2 className="font-heading text-[2.75rem] md:text-6xl lg:text-[4.25rem] font-medium text-stone-900 leading-[1.05] tracking-tight">
              <span className="italic font-normal">¿Dónde estamos?</span>
            </h2>

            <div className="mx-auto lg:mx-0 mt-6 h-px w-16 bg-linear-to-r from-rose-300 via-rose-400 to-rose-300" />

            <p className="mt-6 font-sans text-base md:text-lg leading-relaxed text-stone-500 max-w-md mx-auto lg:mx-0 font-light">
              En el corazón del Nordeste antioqueño, en Amalfi — conocida como
              &ldquo;La Ciudad Señora del Nordeste&rdquo; — te espera un destino
              rodeado de naturaleza, tradición y arquitectura única. Nos
              encontramos a menos de una cuadra del parque principal, cerca de
              restaurantes, comercio y los principales puntos de interés para
              que disfrutes Amalfi caminando.
            </p>
          </div>

          <div className="lg:col-span-8 space-y-10">
            <div className="grid lg:grid-cols-2 gap-12 items-start">
              <div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                  {contactInfo.map((item, index) => (
                    <ContactCard key={item.label} item={item} index={index} />
                  ))}
                </div>

                <div
                  ref={ctaRef}
                  className={`
                    relative mt-8 bg-linear-to-br from-stone-900 to-stone-950
                    rounded-2xl p-8 md:p-10 border border-stone-800
                    shadow-xl shadow-black/20
                    hover:shadow-2xl hover:shadow-black/30
                    transition-all duration-500
                    ${ctaInView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
                  `}>
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

                  <h3 className="relative font-heading text-2xl md:text-3xl font-medium text-white leading-tight mb-4 tracking-tight">
                    ¿Listo para tu próxima escapada?
                  </h3>
                  <p className="relative font-sans text-[15px] leading-relaxed text-stone-400 mb-7 font-light">
                    Te esperamos en Amalfi, Antioquia, un pueblo de montañas,
                    cascadas y embalses rodeado de bosques nativos. Reserva
                    directamente y descubre la calidez del Nordeste antioqueño.
                  </p>
                  <div className="relative flex flex-wrap items-center gap-3">
                    <button
                      type="button"
                      onClick={() => navigate("/reservar")}
                      className="
                        relative inline-flex items-center gap-2.5 px-7 py-3.5
                        bg-rose-500 text-white
                        font-sans text-xs font-medium tracking-[0.2em] uppercase rounded-full
                        hover:bg-rose-400 hover:shadow-lg hover:shadow-rose-500/30
                        hover:-translate-y-0.5
                        transition-all duration-300 group cursor-pointer
                      ">
                      Reservar en línea
                      <ChevronRight className="size-4 transition-transform duration-300 group-hover:translate-x-1" />
                    </button>

                    {/*
                      Enlace real y visible a la ficha de Google. Además de
                      servir al visitante que llega con el mapa abierto en el
                      móvil, es el enlace saliente que confirma al buscador que
                      el negocio existe y dónde está.
                    */}
                    <a
                      href={HOTEL.googleMapsShortUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="
                        relative inline-flex items-center gap-2.5 px-7 py-3.5
                        border border-white/25 text-white
                        font-sans text-xs font-medium tracking-[0.2em] uppercase rounded-full
                        hover:border-white/50 hover:bg-white/5
                        transition-all duration-300
                      ">
                      Cómo llegar
                      <Navigation className="size-4" />
                    </a>
                  </div>
                </div>
              </div>

              <Suspense
                fallback={
                  <div className="aspect-4/3 rounded-2xl bg-stone-100 dark:bg-stone-800 animate-pulse" />
                }>
                <MapEmbed />
              </Suspense>
            </div>

            <div
              ref={infoRef}
              className={`
                grid grid-cols-1 md:grid-cols-3 gap-6
                transition-all duration-700
                ${infoInView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
              `}>
              <div className="group relative text-center px-6 py-6 bg-white/40 backdrop-blur-sm rounded-2xl border border-stone-100 shadow-sm hover:shadow-md hover:shadow-rose-200/30 hover:-translate-y-0.5 transition-all duration-500">
                <div
                  aria-hidden
                  className="absolute inset-0 bg-rose-100/30 rounded-2xl blur-xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none"
                />
                <p className="relative font-heading text-3xl font-medium text-stone-900 mb-1">
                  +1000
                </p>
                <p className="relative font-sans text-xs tracking-[0.2em] uppercase text-stone-400">
                  Huéspedes satisfechos
                </p>
              </div>
              <div className="group relative text-center px-6 py-6 bg-white/40 backdrop-blur-sm rounded-2xl border border-stone-100 shadow-sm hover:shadow-md hover:shadow-rose-200/30 hover:-translate-y-0.5 transition-all duration-500">
                <div
                  aria-hidden
                  className="absolute inset-0 bg-rose-100/30 rounded-2xl blur-xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none"
                />
                <p className="relative font-heading text-3xl font-medium text-stone-900 mb-1">
                  Centro
                </p>
                <p className="relative font-sans text-xs tracking-[0.2em] uppercase text-stone-400">
                  ubicación privilegiada
                </p>
              </div>
              <div className="group relative text-center px-6 py-6 bg-white/40 backdrop-blur-sm rounded-2xl border border-stone-100 shadow-sm hover:shadow-md hover:shadow-rose-200/30 hover:-translate-y-0.5 transition-all duration-500">
                <div
                  aria-hidden
                  className="absolute inset-0 bg-rose-100/30 rounded-2xl blur-xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none"
                />
                <p className="relative font-heading text-3xl font-medium text-stone-900 mb-1">
                  24/7
                </p>
                <p className="relative font-sans text-xs tracking-[0.2em] uppercase text-stone-400">
                  Servicio al cliente
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
