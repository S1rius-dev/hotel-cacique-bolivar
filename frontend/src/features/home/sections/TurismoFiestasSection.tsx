import { useNavigate } from "react-router-dom";
import { ChevronRight } from "lucide-react";
import { useInView } from "@/lib/hooks/useInView";

/**
 * Las fechas corresponden al calendario tradicional de Amalfi. La programación
 * cambia en cada edición, así que se enlaza a la Alcaldía como fuente y se
 * pide confirmar antes de viajar.
 */
const festividades = [
  {
    fecha: "Enero",
    nombre: "Fiestas de los Reyes Magos",
    detalle:
      "Desfile de Reyes, comparsas, muestras gastronómicas y tablados en el sector Pueblo Nuevo.",
    destacado: false,
  },
  {
    fecha: "2 al 4 de junio",
    nombre: "Fiestas del Campesino",
    detalle:
      "Concursos populares, presentaciones musicales y las tradiciones de la tierra del Nordeste antioqueño.",
    destacado: false,
  },
  {
    fecha: "Mediados de julio",
    nombre: "Fiestas de la Virgen del Carmen",
    detalle:
      "Procesión, cabalgata y recorrido hacia el sector Caracolí, con los actos de la festividad religiosa.",
    destacado: false,
  },
  {
    fecha: "20 de julio",
    nombre: "Festival del Río y el Cooperativismo",
    detalle:
      "En el Valle del Río Riachón: carreras de neumáticos por el río, concursos de cometas y sancochos.",
    destacado: false,
  },
  {
    fecha: "Octubre",
    nombre: "Fiestas de la Paz y el Progreso",
    detalle:
      "Las más representativas de Amalfi, se realizan desde 1962: comparsas, desfile de carrozas, mitos y leyendas, cabalgata y festival de la canción.",
    destacado: true,
  },
  {
    fecha: "8 de diciembre",
    nombre: "Fiestas de la Inmaculada Concepción",
    detalle:
      "Fiesta patronal del municipio, enlazada con la celebración del Día de las Velitas.",
    destacado: false,
  },
];

export default function TurismoFiestasSection() {
  const navigate = useNavigate();
  const { ref: headingRef, inView: headingInView } = useInView(0.1);
  const { ref: listRef, inView: listInView } = useInView(0.05);

  return (
    <section
      id="fiestas"
      className="relative pt-12 md:pt-16 pb-12 md:pb-16 bg-stone-50 overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,var(--tw-gradient-stops))] from-rose-50/60 via-stone-50 to-transparent" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_right,var(--tw-gradient-stops))] from-rose-100/20 via-transparent to-transparent" />

      <div
        aria-hidden
        className="absolute inset-0 opacity-[0.015] pointer-events-none"
        style={{
          backgroundImage: `radial-gradient(circle at 1px 1px, #292524 1px, transparent 1px)`,
          backgroundSize: "40px 40px",
        }}
      />

      <div className="relative mx-auto max-w-[90rem] px-6">
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
              Qué hay en Amalfi
            </p>

            <h2 className="font-heading text-[2.75rem] md:text-6xl lg:text-[4.25rem] font-medium text-stone-900 leading-[1.05] tracking-tight">
              <span className="italic font-normal">Fiestas y</span> festividades
            </h2>

            <div className="mx-auto lg:mx-0 mt-6 h-px w-16 bg-linear-to-r from-rose-300 via-rose-400 to-rose-300" />

            <p className="mt-6 font-sans text-base md:text-lg leading-relaxed text-stone-500 max-w-md mx-auto lg:mx-0 font-light">
              Amalfi es{" "}
              <span className="text-stone-700">&ldquo;La Ciudad Señora del Nordeste&rdquo;</span>.
              Su calendario tradicional llena el año de desfiles, procesiones y
              música campesina. Si vienes por alguna de estas fechas, te
              esperamos a menos de una cuadra del parque.
            </p>

            <p className="mt-4 font-sans text-xs leading-relaxed text-stone-400 font-light">
              Fechas del calendario tradicional del municipio. La programación
              cambia en cada edición: consúltala con la{" "}
              <a
                href="https://amalfiantioquia.micolombiadigital.gov.co/"
                target="_blank"
                rel="noopener noreferrer"
                className="text-rose-500 underline underline-offset-2 hover:text-rose-600">
                Alcaldía de Amalfi
              </a>{" "}
              antes de reservar.
            </p>
          </div>

          <div
            ref={listRef}
            className={`
              lg:col-span-8 transition-all duration-700
              ${listInView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
            `}>
            <dl className="border-t border-stone-200">
              {festividades.map((f) => (
                <div
                  key={f.nombre}
                  className={`
                    border-b border-stone-200/80 py-6 sm:py-7
                    sm:grid sm:grid-cols-12 sm:gap-6
                    ${f.destacado ? "sm:-mx-4 sm:px-4 sm:bg-rose-50/50 sm:rounded-xl" : ""}
                  `}>
                  <dt className="font-heading text-lg sm:text-xl text-stone-900 sm:col-span-4 lg:col-span-3 mb-1 sm:mb-0">
                    {f.fecha}
                  </dt>
                  <dd className="sm:col-span-8 lg:col-span-9">
                    <p className="font-sans text-base font-medium text-stone-800 leading-snug">
                      {f.nombre}
                      {f.destacado && (
                        <span className="ml-2 align-middle font-sans text-[10px] tracking-[0.15em] uppercase text-rose-600">
                          La más grande
                        </span>
                      )}
                    </p>
                    <p className="mt-1.5 font-sans text-sm leading-relaxed text-stone-500 font-light">
                      {f.detalle}
                    </p>
                  </dd>
                </div>
              ))}
            </dl>

            <div className="mt-10 rounded-2xl border border-stone-200 bg-white/60 p-7 sm:p-9">
              <h3 className="font-heading text-xl sm:text-2xl font-medium text-stone-900 tracking-tight">
                Además, en Amalfi
              </h3>
              <p className="mt-3 font-sans text-sm sm:text-base leading-relaxed text-stone-500 font-light">
                El parque principal y su entorno, la arquitectura del centro, el
                río Riachón y el paisaje de montaña con cascadas y embalses del
                Nordeste antioqueño. Todo esto, a una caminata de tu
                habitación.
              </p>

              <button
                type="button"
                onClick={() => navigate("/reservar")}
                className="
                  mt-7 inline-flex items-center gap-2.5 px-7 py-3.5
                  bg-rose-500 text-white
                  font-sans text-xs font-medium tracking-[0.2em] uppercase rounded-full
                  hover:bg-rose-400 hover:shadow-lg hover:shadow-rose-500/30
                  hover:-translate-y-0.5
                  transition-all duration-300 group cursor-pointer
                ">
                Reservar en Amalfi
                <ChevronRight className="size-4 transition-transform duration-300 group-hover:translate-x-1" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
