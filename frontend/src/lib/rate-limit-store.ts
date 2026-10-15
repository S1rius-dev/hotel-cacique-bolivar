type Listener = () => void;

export interface RateLimitInfo {
  limit?: number;
  remaining?: number;
  reset?: number;
  retryAfter?: number;
}

let retryAfterEpoch = 0;
let listeners: Listener[] = [];

export const rateLimitStore = {
  get isCoolingDown(): boolean {
    return Date.now() < retryAfterEpoch;
  },

  get secondsRemaining(): number {
    const diff = Math.ceil((retryAfterEpoch - Date.now()) / 1000);
    return diff > 0 ? diff : 0;
  },

  setCooldown(seconds: number): void {
    retryAfterEpoch = Date.now() + seconds * 1000;
    listeners.forEach((l) => l());
  },

  parseHeaders(headers: Record<string, string | undefined>): RateLimitInfo {
    return {
      limit: headers["x-ratelimit-limit"]
        ? Number(headers["x-ratelimit-limit"])
        : undefined,
      remaining: headers["x-ratelimit-remaining"]
        ? Number(headers["x-ratelimit-remaining"])
        : undefined,
      reset: headers["x-ratelimit-reset"]
        ? Number(headers["x-ratelimit-reset"])
        : undefined,
      retryAfter: headers["retry-after"]
        ? Number(headers["retry-after"])
        : undefined,
    };
  },

  subscribe(listener: Listener): () => void {
    listeners.push(listener);
    return () => {
      listeners = listeners.filter((l) => l !== listener);
    };
  },
};
