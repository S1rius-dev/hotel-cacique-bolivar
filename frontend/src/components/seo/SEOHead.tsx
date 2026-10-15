import { Helmet } from "react-helmet-async";
import { HOTEL, absoluteUrl } from "@/config/hotel";

interface SEOHeadProps {
  /**
   * Título de la página. El nombre del negocio se agrega solo si el título
   * todavía no lo contiene, para no repetir la marca dos veces.
   */
  title: string;
  description: string;
  /**
   * Ruta dentro del sitio, por ejemplo `/reservar`. Es obligatoria: sin ella
   * el canonical cae en la portada y Google deja de distinguir las páginas.
   */
  url: string;
  /** Ruta local de la imagen social, por ejemplo `/og-image.jpg`. */
  image?: string;
  type?: string;
  /** Para /pago, /mi-perfil, panel interno y similares. */
  noindex?: boolean;
}

/**
 * JPEG de 1200x630. Facebook, LinkedIn, WhatsApp y X no decodifican SVG ni
 * WebP de forma fiable, así que la imagen social debe ir en un formato
 * lossless de photographs comunes.
 */
const DEFAULT_IMAGE = "/og-image.jpg";
const IMAGE_WIDTH = 1200;
const IMAGE_HEIGHT = 630;

function buildTitle(title: string): string {
  return title.includes(HOTEL.name) ? title : `${title} | ${HOTEL.name}`;
}

export function SEOHead({
  title,
  description,
  url,
  image = DEFAULT_IMAGE,
  type = "website",
  noindex = false,
}: SEOHeadProps) {
  const fullTitle = buildTitle(title);
  const canonicalUrl = absoluteUrl(url);
  const imageUrl = image.startsWith("http") ? image : absoluteUrl(image);
  const geo = HOTEL.geo;

  return (
    <Helmet>
      <title>{fullTitle}</title>
      <meta name="description" content={description} />
      <link rel="canonical" href={canonicalUrl} />

      <meta
        name="robots"
        content={noindex ? "noindex, nofollow" : "index, follow"}
      />

      {/* Señales de ubicación, que es lo que sostiene el SEO local. */}
      <meta
        name="geo.region"
        content={`${HOTEL.address.addressCountry}-${HOTEL.address.addressRegionCode}`}
      />
      <meta
        name="geo.placename"
        content={HOTEL.address.addressLocality}
      />
      <meta
        name="geo.position"
        content={`${geo.latitude};${geo.longitude}`}
      />
      <meta name="ICBM" content={`${geo.latitude}, ${geo.longitude}`} />

      <meta property="og:type" content={type} />
      <meta property="og:site_name" content={HOTEL.name} />
      <meta property="og:locale" content="es_CO" />
      <meta property="og:title" content={fullTitle} />
      <meta property="og:description" content={description} />
      <meta property="og:url" content={canonicalUrl} />
      <meta property="og:image" content={imageUrl} />
      <meta property="og:image:width" content={String(IMAGE_WIDTH)} />
      <meta property="og:image:height" content={String(IMAGE_HEIGHT)} />
      <meta property="og:image:alt" content={fullTitle} />

      <meta name="twitter:card" content="summary_large_image" />
      <meta name="twitter:title" content={fullTitle} />
      <meta name="twitter:description" content={description} />
      <meta name="twitter:image" content={imageUrl} />
      <meta name="twitter:image:alt" content={fullTitle} />
    </Helmet>
  );
}
