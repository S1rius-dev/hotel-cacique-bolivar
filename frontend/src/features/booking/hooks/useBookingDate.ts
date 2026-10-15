import { useState, useMemo } from "react";
import { differenceInDays, addDays, format } from "date-fns";
import { es } from "date-fns/locale";
import { useSearchParams } from "react-router-dom";

function parseUrlDate(param: string | null): Date | null {
  if (!param) return null;
  const match = param.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  if (!match) return null;
  const [, y, m, d] = match;
  const date = new Date(Number(y), Number(m) - 1, Number(d));
  return isNaN(date.getTime()) ? null : date;
}

export function useBookingDate() {
  const [searchParams] = useSearchParams();

  const [checkInDate, setCheckInDate] = useState(() => {
    const fromUrl = parseUrlDate(searchParams.get("checkIn"));
    return fromUrl ?? addDays(new Date(), 1);
  });

  const [checkOutDate, setCheckOutDate] = useState(() => {
    const fromUrl = parseUrlDate(searchParams.get("checkOut"));
    return fromUrl ?? addDays(new Date(), 2);
  });

  const checkIn = format(checkInDate, "dd MMM", { locale: es });
  const checkOut = format(checkOutDate, "dd MMM", { locale: es });

  const nights = useMemo(
    () => Math.max(0, differenceInDays(checkOutDate, checkInDate)),
    [checkInDate, checkOutDate],
  );

  const checkInISO = format(checkInDate, "yyyy-MM-dd");
  const checkOutISO = format(checkOutDate, "yyyy-MM-dd");

  return {
    checkInDate,
    checkOutDate,
    setCheckInDate,
    setCheckOutDate,
    checkIn,
    checkOut,
    nights,
    checkInISO,
    checkOutISO,
  };
}
