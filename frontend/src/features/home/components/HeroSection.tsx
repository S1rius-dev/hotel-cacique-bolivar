import amalfi2 from "@/assets/amalfi-paisaje.jpg";
import fuente from "@/assets/fuente.jpg";
import parque from "@/assets/parque.jpg";
import panoramica from "@/assets/panoramica.jpg";
import Hero from "@/layouts/hero/Hero";
import BookingSearch from "@/components/booking/BookingSearch";

export default function HeroSection() {
  return (
    <section className="relative w-full h-[70vh] sm:h-[75vh] md:h-[80vh] overflow-hidden group shadow-[0_40px_80px_-25px_rgba(0,0,0,0.4),0_15px_30px_-15px_rgba(0,0,0,0.25)]">
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
  );
}
