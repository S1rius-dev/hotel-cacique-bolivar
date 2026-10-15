const blobCache = new Map<string, string>();
const inflight = new Map<string, Promise<string>>();

function loadImageAsBlob(url: string): Promise<string> {
  return fetch(url, { mode: "cors", credentials: "omit" })
    .then((res) => {
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return res.blob();
    })
    .then((blob) => URL.createObjectURL(blob))
    .catch(() => url);
}

export function getCachedProfileUrl(url: string | undefined): string | undefined {
  if (!url) return undefined;
  return blobCache.get(url);
}

export function prefetchProfileImage(url: string): Promise<string> {
  if (blobCache.has(url)) return Promise.resolve(blobCache.get(url)!);

  if (!inflight.has(url)) {
    const promise = loadImageAsBlob(url);
    inflight.set(
      url,
      promise.then((blobUrl) => {
        blobCache.set(url, blobUrl);
        inflight.delete(url);
        return blobUrl;
      }),
    );
  }

  return inflight.get(url)!;
}
