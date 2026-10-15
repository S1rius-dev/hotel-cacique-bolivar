import { useState, useEffect } from "react";
import { getCachedProfileUrl, prefetchProfileImage } from "./profile-image-cache";

interface ResolvedImage {
  src: string;
  url: string;
}

export function useProfileImage(src: string | undefined): string | undefined {
  // El hit de cache se resuelve en render (es una lectura pura del Map). El
  // estado solo guarda el resultado del prefetch, y se etiqueta con su `src`
  // para no devolver una imagen de la peticion anterior.
  const [resolved, setResolved] = useState<ResolvedImage | null>(null);

  useEffect(() => {
    if (!src || getCachedProfileUrl(src)) return;

    let active = true;
    prefetchProfileImage(src).then((url) => {
      if (active) setResolved({ src, url });
    });
    return () => {
      active = false;
    };
  }, [src]);

  if (!src) return undefined;
  if (resolved?.src === src) return resolved.url;
  return getCachedProfileUrl(src);
}
