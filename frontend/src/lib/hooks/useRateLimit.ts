import { useSyncExternalStore } from "react";
import { rateLimitStore } from "@/lib/rate-limit-store";

export function useRateLimit() {
  const isCoolingDown = useSyncExternalStore(
    rateLimitStore.subscribe,
    () => rateLimitStore.isCoolingDown,
    () => false,
  );

  const secondsRemaining = useSyncExternalStore(
    rateLimitStore.subscribe,
    () => rateLimitStore.secondsRemaining,
    () => 0,
  );

  return { isCoolingDown, secondsRemaining };
}
