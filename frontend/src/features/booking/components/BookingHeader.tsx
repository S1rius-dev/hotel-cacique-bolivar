import Navbar from "@/components/layout/Navbar";
import BookingSearch from "@/components/booking/BookingSearch";
import Hero from "@/layouts/hero/Hero";
import { useScroll } from "@/features/home/hooks/useScroll";

import amalfi2 from "@/assets/amalfi-paisaje.jpg";
import fuente from "@/assets/fuente.jpg";
import parque from "@/assets/parque.jpg";
import panoramica from "@/assets/panoramica.jpg";

export default function BookingHeader() {
  const scrolled = useScroll(400);

  return (
    <>
      <div
        className={`
          fixed top-0 left-0 w-full z-50 transition-all duration-300
          ${
            scrolled
              ? "bg-white/90 backdrop-blur-md shadow-[0_12px_18px_-6px_rgba(244,63,94,0.25)]"
              : "bg-transparent"
          }
        `}
      >
        <Navbar
          links={[{ title: "Inicio", href: "/" }]}
          className={scrolled ? "text-stone-800" : "text-white"}
          linkClassName={
            scrolled
              ? "hover:bg-rose-50 hover:text-rose-600"
              : "hover:bg-white/10"
          }
          mobileMenuClassName={
            scrolled
              ? "bg-white/95 backdrop-blur-md"
              : "bg-black/80 backdrop-blur-md"
          }
        />
      </div>

      <section className="relative w-full h-[70vh] sm:h-[75vh] md:h-[80vh] overflow-hidden shadow-[0_40px_80px_-25px_rgba(0,0,0,0.4),0_15px_30px_-15px_rgba(0,0,0,0.25)]">
        <Hero
          slides={[
            {
              src: amalfi2,
              alt: "Paisaje de Amalfi, Antioquia, desde el Hotel Cacique de Bolivar",
            },
            { src: panoramica, alt: "" },
            { src: fuente, alt: "" },
            { src: parque, alt: "" },
          ]}
          interval={5000}
        />

        <div className="absolute bottom-24 sm:bottom-10 md:bottom-16 left-3 sm:left-5 md:left-10 lg:left-16 z-20 flex flex-col items-start gap-3 sm:gap-4 max-w-[calc(100vw-1.5rem)] sm:max-w-none">
          <div className="hidden sm:flex items-center gap-3 opacity-90">
            <span className="h-px w-8 bg-rose-300/70" />
            <p className="font-sans text-[10px] sm:text-xs tracking-[0.35em] uppercase text-white/85 font-medium">
              Reservar estancia
            </p>
          </div>
          <BookingSearch />
        </div>
      </section>
    </>
  );
}
