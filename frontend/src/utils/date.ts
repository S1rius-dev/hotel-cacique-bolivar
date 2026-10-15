type DateInput = Date | string | null | undefined;

function parseDate(date: DateInput): Date | null {
  if (!date) return null;
  if (date instanceof Date) return date;
  if (typeof date === "string" && /^\d{4}-\d{2}-\d{2}$/.test(date)) {
    const [y, m, d] = date.split("-").map(Number);
    const parsed = new Date(y, m - 1, d);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }
  const parsed = new Date(date);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

export function formatDate(date: DateInput): string {
  const parsed = parseDate(date);
  if (!parsed) return "—";
  return parsed.toLocaleDateString("es-CO", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

export function formatDateShort(date: DateInput): string {
  const parsed = parseDate(date);
  if (!parsed) return "—";
  return parsed.toLocaleDateString("es-CO", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

export function formatDateDayMonth(date: DateInput): string {
  const parsed = parseDate(date);
  if (!parsed) return "—";
  return parsed.toLocaleDateString("es-CO", {
    month: "short",
    day: "numeric",
  });
}

export function formatDateTimeCompact(date: DateInput): string {
  const parsed = parseDate(date);
  if (!parsed) return "-";

  const day = parsed.toLocaleDateString("es-ES", { weekday: "short" });
  const dayNumber = parsed.toLocaleDateString("es-ES", { day: "2-digit" });
  const month = parsed.toLocaleDateString("en-US", { month: "short" });
  const time = parsed.toLocaleTimeString("en-US", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  });

  const capitalizedDay = day.charAt(0).toUpperCase() + day.slice(1);

  return `${capitalizedDay}, ${dayNumber} ${month} · ${time}`;
}

export function formatDateTimeFull(date: DateInput): string {
  const parsed = parseDate(date);
  if (!parsed) return "—";
  return parsed.toLocaleDateString("es-CO", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function formatTimeOnly(value?: string | null): string | null {
  if (!value) return null;
  if (/^\d{2}:\d{2}(:\d{2})?$/.test(value)) return value.slice(0, 5);
  try {
    const d = new Date(value);
    if (isNaN(d.getTime())) return null;
    return d.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit", hour12: true });
  } catch {
    return null;
  }
}

export function formatDateTimeWithAlas(date: DateInput): string {
  const parsed = parseDate(date);
  if (!parsed) return "—";
  const fecha = parsed.toLocaleDateString("es-CO", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
  const hora = parsed.toLocaleTimeString("es-CO", {
    hour: "2-digit",
    minute: "2-digit",
  });
  return `${fecha} a las ${hora}`;
}
