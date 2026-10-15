import { absoluteUrl } from "@/config/hotel";

/**
 * Migas de pan para schema.org. Sin esto, Google no puede saber que
 * `/ayuda` es una sección de la portada y la jerarquía del sitio le queda
 * plana en los resultados.
 *
 * Vive fuera de JSONLD.tsx porque ese archivo solo debe exportar componentes:
 * mezclar helpers con componentes rompe el fast refresh de React.
 */
export function breadcrumbs(
  items: { name: string; url: string }[],
): Record<string, unknown> {
  return {
    "@context": "https://schema.org",
    "@type": "BreadcrumbList",
    itemListElement: items.map((item, index) => ({
      "@type": "ListItem",
      position: index + 1,
      name: item.name,
      item: absoluteUrl(item.url),
    })),
  };
}
