import { useCallback, useSyncExternalStore } from "react";

/**
 * Suscripcion a un media query. El valor se lee via useSyncExternalStore, asi
 * que el estado vive en la media query (fuera de React) y no hace falta
 * setState en un efecto.
 */
export function useMediaQuery(query: string): boolean {
  const subscribe = useCallback(
    (onChange: () => void) => {
      const mq = window.matchMedia(query);
      mq.addEventListener("change", onChange);
      return () => mq.removeEventListener("change", onChange);
    },
    [query],
  );

  return useSyncExternalStore(
    subscribe,
    () => window.matchMedia(query).matches,
    () => false,
  );
}

export function useIsMobile(): boolean {
  return useMediaQuery("(max-width: 767px)");
}
