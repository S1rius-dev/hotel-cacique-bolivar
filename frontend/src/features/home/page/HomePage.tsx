import { useMemo, useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/auth/AuthContext";
import { SEOHead, JSONLD } from "@/components/seo";
import { HOTEL, SAME_AS, absoluteUrl } from "@/config/hotel";
import { formatCurrency } from "@/utils/format";
import PublicLayout from "@/layouts/public/PublicLayout";
import { usePublicLayout } from "@/layouts/public/PublicLayoutContext";
import type { Room } from "@/components/home/rooms.data";
import HabitacionesSection from "../sections/HabitacionesSection";
import ServiciosSection from "../sections/ServiciosSection";
import { services } from "../sections/services.data";
import ResenasSection from "../sections/ReseñasSection";
import UbicacionSection from "../sections/UbicacionSection";
import TurismoFiestasSection from "../sections/TurismoFiestasSection";
import WhatsAppButton from "../components/WhatsAppButton";
import OnboardingBanner from "../components/OnboardingBanner";
import { useRoomsWithPrices } from "../hooks/useRoomsWithPrices";
import { Calendar } from "lucide-react";
import BookingSearch from "@/components/booking/BookingSearch";

/**
 * La portada no tenía ningún encabezado de nivel 1: lo único era un
 * párrafo "Reservar estancia". Para el SEO local ese hueco importa, porque es
 * la frase que le dice a Google de qué trata la página.
 *
 * El precio solo aparece si el backend ya respondió. Mientras la consulta
 * viaja, o si el backend está caído, se omite: una cifra inventada en la
 * portada contradice las políticas de datos estructurados de Google, que
 * exigen que el precio del marcado sea el mismo que ve el usuario.
 */
function HomeHero({ desdePrecio }: { desdePrecio?: number }) {
  return (
    <div className="absolute bottom-24 sm:bottom-10 md:bottom-16 left-3 sm:left-5 md:left-10 lg:left-16 z-20 flex flex-col items-start gap-3 sm:gap-5 max-w-[calc(100vw-1.5rem)] sm:max-w-none">
      <div className="flex items-center gap-3">
        <span className="h-px w-8 shrink-0 bg-rose-300/70" />
        <span className="font-sans text-[10px] sm:text-xs tracking-[0.35em] uppercase text-white/85 font-medium">
          Amalfi, Antioquia
        </span>
      </div>

      <h1 className="font-heading text-[2rem] sm:text-4xl md:text-5xl lg:text-6xl font-medium text-white tracking-tight leading-[1.05] drop-shadow-[0_2px_12px_rgba(0,0,0,0.5)] max-w-3xl">
        Hotel en Amalfi, Antioquia
      </h1>

      <p className="font-sans text-sm sm:text-base text-white/80 font-light max-w-xl drop-shadow-[0_1px_8px_rgba(0,0,0,0.5)]">
        {HOTEL.totalRooms} habitaciones
        {desdePrecio ? ` desde ${formatCurrency(desdePrecio)} la noche` : ""}, a menos de una
        cuadra del parque principal.
      </p>

      <BookingSearch />
    </div>
  );
}

const PurposeBanner = () => (
  <div className="flex h-14 w-full items-center justify-center gap-2 bg-stone-50 border-y border-stone-200/60">
    <Calendar className="size-4 text-rose-400 shrink-0" />
    <p className="text-sm text-stone-500 font-light tracking-wide">
      Consulta disponibilidad, reserva habitaciones y administra tus reservas
      en línea de forma rápida y segura.
    </p>
  </div>
);

/**
 * Nota: no se declara `aggregateRating`. Google exige que la valoración en el
 * marcado se corresponda con reseñas visibles para el usuario, y publicar un
 * 4.5 autoinformado sin reseñas que lo respalden es una infracción de sus
 * políticas de datos estructurados. Las reseñas del sitio se muestran en la
 * sección correspondiente, pero son del sistema propio, no de Google.
 *
 * Los precios y `priceRange` tampoco se fijan a mano: se derivan del backend y
 * se omiten cuando todavía no se conocen. Google exige que el `price` del
 * marcado coincida con el precio que ve el usuario, así que es preferible no
 * publicarlo a publicar el equivocado.
 */
function buildHotelSchema(rooms: Room[], priceRange: string | undefined) {
  const ofertas = rooms.flatMap((room) =>
    typeof room.pricePerNight === "number"
      ? [
          {
            "@type": "Offer",
            name: room.name,
            description: room.description,
            url: absoluteUrl("/reservar"),
            priceCurrency: "COP",
            price: room.pricePerNight,
            availability: "https://schema.org/InStock",
          },
        ]
      : [],
  );

  return {
    "@context": "https://schema.org",
    "@type": "Hotel",
    name: HOTEL.name,
    description: `${HOTEL.name} es un hotel en Amalfi, Antioquia, Colombia, con ${HOTEL.totalRooms} habitaciones, agua caliente, WiFi gratuito y recepción 24 horas, a menos de una cuadra del parque principal.`,
    url: HOTEL.baseUrl,
    telephone: HOTEL.phone,
    email: HOTEL.email,
    image: absoluteUrl("/og-image.jpg"),
    address: {
      "@type": "PostalAddress",
      streetAddress: HOTEL.address.streetAddress,
      addressLocality: HOTEL.address.addressLocality,
      addressRegion: HOTEL.address.addressRegion,
      addressCountry: HOTEL.address.addressCountry,
    },
    geo: {
      "@type": "GeoCoordinates",
      latitude: HOTEL.geo.latitude,
      longitude: HOTEL.geo.longitude,
    },
    areaServed: [
      {
        "@type": "City",
        name: "Amalfi",
      },
      {
        "@type": "AdministrativeArea",
        name: "Antioquia",
      },
    ],
    ...(priceRange ? { priceRange } : {}),
    currenciesAccepted: "COP",
    numberOfRooms: HOTEL.totalRooms,
    checkinTime: HOTEL.checkinTime,
    checkoutTime: HOTEL.checkoutTime,
    sameAs: [...SAME_AS],
    amenityFeature: services.map((service) => ({
      "@type": "LocationFeatureSpecification",
      name: service.title,
      value: true,
    })),
    ...(ofertas.length > 0
      ? {
          hasOfferCatalog: {
            "@type": "OfferCatalog",
            name: "Tipos de habitación",
            itemListElement: ofertas,
          },
        }
      : {}),
  };
}

interface HomeProps {
  /**
   * /mi-perfil y /mis-reservas montan la portada con un modal encima y
   * emiten su propio SEOHead en noindex. No basta con ponerlo después en el
   * árbol: esta página es lazy, así que su SEOHead se monta al resolver el
   * suspense y su commit pisa al de la ruta. Ganaría el indexable y Google
   * vería dos URLs con el mismo contenido que la portada.
   */
  hideSeo?: boolean;
}

const Home: React.FC<HomeProps> = ({ hideSeo = false }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const [dismissedBanner, setDismissedBanner] = useState(false);
  const { user } = useAuth();
  const { openLogin, openCompleteProfile } = usePublicLayout();

  const { openLogin: openLoginRequest } = location.state ?? {};

  useEffect(() => {
    if (openLoginRequest) {
      openLogin();
      navigate("/", { replace: true });
    }
  }, [openLoginRequest, openLogin, navigate]);

  const roles = user?.roles ?? [];
  const needsClientProfile = roles.includes("CLIENTE") && !user?.clienteId;
  const showBanner = needsClientProfile && !dismissedBanner;

  const { rooms: roomsWithPrices, priceRange, minPrice } = useRoomsWithPrices();
  const hotelSchema = useMemo(
    () => buildHotelSchema(roomsWithPrices, priceRange),
    [roomsWithPrices, priceRange],
  );

  return (
    <>
      {!hideSeo && (
        <>
          <SEOHead
            title="Hotel en Amalfi, Antioquia"
            description={`${HOTEL.name} en Amalfi, Antioquia. ${HOTEL.totalRooms} habitaciones con agua caliente, WiFi gratis y recepción 24 horas, a una cuadra del parque. Reserva online.`}
            url="/"
          />
          <JSONLD data={hotelSchema} />
        </>
      )}

      <PublicLayout heroContent={<HomeHero desdePrecio={minPrice} />}>
        {showBanner && (
          <OnboardingBanner
            onCompleteProfile={openCompleteProfile}
            onDismiss={() => setDismissedBanner(true)}
          />
        )}

        <PurposeBanner />
        <HabitacionesSection />
        <ServiciosSection />
        <ResenasSection onLoginRequired={openLogin} />
        <UbicacionSection />
        <TurismoFiestasSection />
        <WhatsAppButton />
      </PublicLayout>
    </>
  );
};

export default Home;
