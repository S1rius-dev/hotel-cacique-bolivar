/**
 * Fuente única de verdad de los datos del hotel.
 *
 * Antes de este archivo, la dirección, el teléfono y el correo estaban
 * duplicados en ocho o más archivos y ya habían divergido entre sí. Google
 * usa la coherencia del NAP (nombre-dirección-teléfono) como señal local, así
 * que cualquier dato que se muestre al público debe salir de aquí.
 *
 * Los valores por defecto de esta DEMO son ficticios: el dominio, la calle,
 * las coordenadas y los enlaces no identifican al negocio real. En el
 * despliegue de producción se sustituyen por los oficiales.
 */

export const HOTEL = {
  /**
   * Sin tilde, exactamente como figura en el Perfil de Negocio de Google.
   * La ficha es la fuente de verdad del NAP y Google usa la coincidencia exacta
   * del nombre para asociar el sitio con el negocio, así que aquí no se
   * "corrige" la ortografía: se copia la de Google.
   */
  name: "Hotel Cacique de Bolivar",

  /**
   * Origen canónico de la DEMO. Sin barra final: se concatena con las rutas.
   * En el despliegue real se reemplaza por el dominio propio del hotel.
   */
  baseUrl: "https://demo.example.com",

  /**
   * NIT ficticio de demostración. En el despliegue real se reemplaza por el
   * NIT oficial del titular (persona natural) con puntos y dígito de
   * verificación. No se publica el valor real en el repositorio.
   */
  nit: "00000000-0",
  nitHolder: "persona natural",

  address: {
    /** Dirección ficticia de demostración; no es la del negocio real. */
    streetAddress: "Calle 10 #5-20",
    addressLocality: "Amalfi",
    addressRegion: "Antioquia",
    /** ISO 3166-2 de Antioquia. */
    addressRegionCode: "ANT",
    addressCountry: "CO",
    addressCountryName: "Colombia",
  },

  /**
   * Coordenadas de demostración, redondeadas a nivel de municipio (≈1 km).
   * No señalan la ubicación exacta del negocio real.
   */
  geo: {
    latitude: 6.91,
    longitude: -75.08,
  },

  /**
   * Enlace genérico a Google Maps (búsqueda del municipio), no a la ficha
   * real del negocio. Se usa en `sameAs` y como enlace "Ver en Maps".
   */
  googleMapsUrl:
    "https://www.google.com/maps/search/?api=1&query=Amalfi%2C+Antioquia%2C+Colombia",

  /** Mismo destino genérico que `googleMapsUrl`, para mostrarlo en pantalla. */
  googleMapsShortUrl:
    "https://www.google.com/maps/search/?api=1&query=Amalfi%2C+Antioquia%2C+Colombia",

  /** Formato internacional para href="tel:" y para schema.org. */
  phone: "+573000000000",
  /** Formato local, como lo muestra el usuario. */
  phoneDisplay: "+57 300 0000000",
  email: "contacto@example.com",
  whatsapp: "https://wa.me/573000000000",

  checkinTime: "15:00",
  checkoutTime: "13:00",

  totalRooms: 43,
} as const;

/**
 * Perfiles externos que identifican al hotel, para `sameAs`.
 *
 * Los que todavía no existen se declaran como `undefined` en lugar de
 * omitirse: así queda constancia de que están pendientes y, al filtrarse,
 * es imposible que un perfil vacío o inventado acabe en el marcado.
 *
 * Solo entran aquí perfiles que identifiquen al negocio. No se añaden
 * directorios de reservas ni agencias: no son el hotel, y Google penaliza el
 * marcado que identifica a la empresa con terceros.
 */
export const PROFILES = {
  // Enlace genérico al municipio, no a la ficha real del negocio: el
  // repositorio de demostración no expone identificadores de la empresa.
  googleMaps: HOTEL.googleMapsUrl,
  // El perfil real de Facebook se retiró del repositorio de demostración.
  facebook: undefined as string | undefined,
  /** TODO: pegar la URL del perfil oficial cuando exista la cuenta. */
  instagram: undefined as string | undefined,
} as const;

/** Perfiles externos que identifican al hotel, para `sameAs`. */
export const SAME_AS: readonly string[] = Object.values(PROFILES).filter(
  (url): url is string => typeof url === "string",
);

/** Dirección en una sola línea, igual que aparece en pantalla. */
export const ADDRESS_LINE = `${HOTEL.address.streetAddress}, ${HOTEL.address.addressLocality}, ${HOTEL.address.addressRegion}`;

/** Construye una URL absoluta. Las rutas relativas en og:image no funcionan. */
export function absoluteUrl(path: string): string {
  return `${HOTEL.baseUrl}${path.startsWith("/") ? path : `/${path}`}`;
}
