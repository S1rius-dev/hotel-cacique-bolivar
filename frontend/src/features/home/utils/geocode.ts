const LIMPIAR_PREFIJOS = [
  /^Perímetro Urbano\s*/i,
  /^Área Urbana\s*/i,
  /^Zona Urbana\s*/i,
  /^Casco Urbano\s*/i,
  /^Comuna\s+\d+\s*/i,
  /^Corregimiento\s*/i,
] as const;

function limpiar(raw: string): string {
  let result = raw.trim();
  for (const regex of LIMPIAR_PREFIJOS) {
    result = result.replace(regex, "");
    if (result !== raw.trim()) break;
  }
  return result.trim() || raw.trim();
}

export async function reverseGeocode(lat: number, lng: number): Promise<string | null> {
  try {
    const res = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&accept-language=es`,
      { headers: { "User-Agent": "HotelCaciqueApp/1.0" } },
    );
    if (!res.ok) return null;
    const data = await res.json();
    const a = data.address;
    if (!a) return null;
    const rawCity = a.city ?? a.town ?? a.village ?? a.municipality ?? a.county ?? a.city_district;
    if (!rawCity) return null;
    const city = limpiar(rawCity);
    const state = a.state;
    return [city, state].filter(Boolean).join(", ") || null;
  } catch {
    return null;
  }
}
