import { useMemo } from "react";
import { SEOHead, JSONLD, breadcrumbs } from "@/components/seo";
import PublicLayout from "@/layouts/public/PublicLayout";
import type { Room } from "@/components/home/rooms.data";
import { HOTEL } from "@/config/hotel";
import { useRoomsWithPrices } from "@/features/home/hooks/useRoomsWithPrices";
import { useTiposHabitacion } from "../hooks/useTiposHabitacion";
import { useDisponibilidad } from "../hooks/useDisponibilidad";
import { useBookingDate } from "../hooks/useBookingDate";
import { useRoomSelection } from "../hooks/useRoomSelection";
import { useBookingSubmit } from "../hooks/useBookingSubmit";
import BookingHeroSection from "../components/BookingHeroSection";
import RoomTypeCard from "../components/RoomTypeCard";
import RoomTypeCardSkeleton from "../components/RoomTypeCardSkeleton";
import BookingSummarySidebar from "../components/BookingSummarySidebar";
import OccupantConfigModal from "../components/OccupantConfigModal";
import type { BookingRoomType } from "../types";
import type { ApiError } from "@/api/custom-instance";
import { XCircle, Phone, Clock, Info } from "lucide-react";
import { FaWhatsapp } from "react-icons/fa";

const BLOQUEO_ERROR_CODE = "RESERVAS_BLOQUEADAS";
const TELEFONO_HOTEL = HOTEL.phoneDisplay;
const WHATSAPP_HOTEL = HOTEL.whatsapp;

const formatCOP = (value: number) =>
  new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
  }).format(value);

/**
 * Los tipos de habitación y su disponibilidad llegan desde la API, así que el
 * HTML que guarda el navegador no los contenía: sin este bloque, /reservar no
 * tenía texto de habitaciones ni de tarifas que un buscador pudiera leer.
 */
const TarifasEstaticas = ({ rooms }: { rooms: Room[] }) => (
  <section
    aria-labelledby="tarifas-heading"
    className="mt-14 border-t border-stone-200 pt-10">
    <h2
      id="tarifas-heading"
      className="font-heading text-2xl sm:text-3xl text-stone-900 leading-tight tracking-tight">
      Tipos de habitación y tarifas
    </h2>
    <p className="mt-2 max-w-2xl text-sm leading-relaxed text-stone-500">
      Estas son las tarifas base por noche en el Hotel Cacique de Bolivar, Amalfi
      Antioquia. El pago en línea cubre la primera noche; el resto se abona
      directamente en el hotel.
    </p>

    <dl className="mt-7 divide-y divide-stone-200 border-y border-stone-200">
      {rooms.map((room) => (
        <div
          key={room.id}
          className="sm:grid sm:grid-cols-12 sm:gap-6 py-5">
          <dt className="sm:col-span-5">
            <p className="font-medium text-stone-800">{room.name}</p>
            <p className="mt-0.5 text-xs text-stone-400">
              {room.capacity} · {HOTEL.checkinTime} a {HOTEL.checkoutTime}
            </p>
          </dt>
          <dd className="mt-2 sm:mt-0 sm:col-span-4">
            <p className="text-sm leading-relaxed text-stone-500">
              {room.description}
            </p>
          </dd>
          <dd className="mt-2 sm:mt-0 sm:col-span-3 sm:text-right">
            <span className="text-xs text-stone-400">desde</span>{" "}
            <span className="font-medium text-stone-900">
              {room.pricePerNight
                ? formatCOP(room.pricePerNight)
                : "Consultar"}
            </span>
            <span className="block text-xs text-stone-400">por noche</span>
          </dd>
        </div>
      ))}
    </dl>
  </section>
);

const PayloadBanner = () => (
  <div className="flex min-h-14 w-full items-center justify-center gap-3 border-y border-amber-200 bg-amber-50/70 px-4 py-3">
    <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-amber-100 text-amber-700">
      <Info className="h-4 w-4" />
    </div>

    <p className="text-center text-sm tracking-wide text-stone-600">
      <span className="font-medium text-stone-800">Importante:</span> el pago en
      línea corresponde únicamente a{" "}
      <span className="font-semibold text-amber-800">la primera noche</span>. El
      valor restante de la reserva se paga{" "}
      <span className="font-semibold text-stone-800">directamente en el hotel</span>
      .
    </p>
  </div>
);

export default function BookingPage() {
  const {
    checkInDate,
    checkOutDate,
    setCheckInDate,
    setCheckOutDate,
    checkIn,
    checkOut,
    nights,
    checkInISO,
    checkOutISO,
  } = useBookingDate();
  const {
    data: tiposHabitacion,
    isLoading,
    error: fetchError,
  } = useTiposHabitacion();
  const { rooms } = useRoomsWithPrices();
  const {
    data: disponibilidad,
    error: disponibilidadError,
    isLoading: isLoadingDisponibilidad,
  } = useDisponibilidad(checkInISO, checkOutISO);

  const bloqueada =
    (disponibilidadError as ApiError | null)?.code === BLOQUEO_ERROR_CODE;
  const bloqueoMensaje = bloqueada
    ? ((disponibilidadError as ApiError | null)?.message ??
      "En este momento no es posible realizar nuevas reservas a través de nuestra página web. Por favor, contáctanos.")
    : "";

  const ambosCargando = isLoading || isLoadingDisponibilidad;

  const tipos = useMemo(() => {
    const base = (tiposHabitacion ?? []) as BookingRoomType[];
    if (!disponibilidad || !checkInISO || !checkOutISO) return base;
    const effMap = new Map(
      (disponibilidad as Array<{ tipoHabitacionId: number; precioNocheEfectivo?: number; precioPersonaExtraEfectivo?: number; totalEstimadoPorNoche?: number }>).map((d) => [
        d.tipoHabitacionId,
        { precioNocheEfectivo: d.precioNocheEfectivo, precioPersonaExtraEfectivo: d.precioPersonaExtraEfectivo, totalEstimadoPorNoche: d.totalEstimadoPorNoche },
      ])
    );
    return base.map((t) => {
      const eff = effMap.get(t.id);
      if (!eff) return t;
      return {
        ...t,
        precioNocheEfectivo: eff.precioNocheEfectivo ?? t.precioBase,
        precioPersonaExtraEfectivo: eff.precioPersonaExtraEfectivo ?? t.precioPersonaExtra,
        totalEstimadoPorNoche: eff.totalEstimadoPorNoche,
      };
    });
  }, [tiposHabitacion, disponibilidad, checkInISO, checkOutISO]);

  const availabilityData = (disponibilidad ?? []) as {
    tipoHabitacionId: number;
    habitacionesDisponibles: number;
    totalHabitaciones: number;
  }[];

  const {
    selections,
    occupantConfig,
    setOccupantConfig,
    configModalType,
    setConfigModalType,
    configModalRoomType,
    availabilityMap,
    allUnavailable,
    totalRooms,
    totalPrice,
    extraPersonCharge,
    totalGuests,
    handleQuantityChange,
    handleSelectRoom,
    getOccupantSummary,
  } = useRoomSelection(tipos, availabilityData, nights);

  const { handleBook, isSubmitting } = useBookingSubmit({
    tipos,
    selections,
    occupantConfig,
    checkIn,
    checkOut,
    checkInISO,
    checkOutISO,
    nights,
    totalPrice,
    totalRooms,
    totalGuests,
    extraPersonCharge,
  });

  const selectionsForSidebar = useMemo(
    () =>
      tipos
        .filter((t) => (selections[t.id] ?? 0) > 0)
        .map((t) => ({ tipo: t, quantity: selections[t.id] ?? 0 })),
    [tipos, selections],
  );

  return (
    <>
      <SEOHead
        title="Reservar habitación en Amalfi, Antioquia"
        description="Reserva tu habitación en el Hotel Cacique de Bolivar, Amalfi Antioquia. Tres tipos de habitación con las tarifas al día, agua caliente y WiFi gratis. Reserva sin intermediarios."
        url="/reservar"
      />
      <JSONLD
        data={breadcrumbs([
          { name: "Inicio", url: "/" },
          { name: "Reservar", url: "/reservar" },
        ])}
      />

      <PublicLayout
        heroContent={
          !ambosCargando ? (
            <BookingHeroSection
              checkIn={checkIn}
              checkOut={checkOut}
              nights={nights}
              checkInDate={checkInDate}
              checkOutDate={checkOutDate}
              onCheckInChange={setCheckInDate}
              onCheckOutChange={setCheckOutDate}
            />
          ) : undefined
        }>
        <PayloadBanner></PayloadBanner>

        <div className="mx-auto max-w-full px-4 md:px-10 py-10 lg:py-14">
          {!ambosCargando && !bloqueada && (
            <div className="mb-8">
              <p className="font-sans text-xs tracking-[0.3em] uppercase text-rose-500 font-medium mb-2">
                Hotel Cacique de Bolivar
              </p>
              <h2 className="font-heading text-2xl sm:text-3xl text-stone-900 leading-tight tracking-tight">
                Selecciona tus habitaciones
              </h2>
              <p className="text-sm text-stone-500 mt-2 max-w-lg leading-relaxed">
                Elige cuántas habitaciones deseas de cada tipo. El sistema
                verificará disponibilidad automáticamente.
              </p>
              <div
                aria-hidden
                className="h-px w-12 bg-linear-to-r from-rose-300 via-rose-400 to-rose-300 mt-5"
              />
            </div>
          )}

          <div className="flex flex-col lg:flex-row gap-10 lg:gap-12">
            <div className="flex-1 min-w-0">
              {fetchError && (
                <div className="flex items-center gap-2 mb-4 p-3 rounded-xl bg-amber-50 border border-amber-200 text-sm text-amber-700">
                  <span>No se pudieron cargar los tipos de habitación.</span>
                  <button
                    type="button"
                    onClick={() => window.location.reload()}
                    className="ml-auto shrink-0 px-3 py-1 rounded-lg bg-amber-200/60 hover:bg-amber-300/60 text-amber-800 font-medium transition-colors cursor-pointer text-xs">
                    Reintentar
                  </button>
                </div>
              )}

              {!ambosCargando && bloqueada && (
                <div className="flex flex-col gap-2 mb-6 p-5 rounded-xl bg-amber-50 border border-amber-300">
                  <div className="flex items-start gap-3">
                    <Clock className="size-5 text-amber-600 shrink-0 mt-0.5" />
                    <div>
                      <p className="text-sm font-semibold text-amber-800">
                        Reservas deshabilitadas por el momento
                      </p>
                      <p className="text-sm text-amber-700 mt-1">
                        {bloqueoMensaje}
                      </p>
                      <div className="flex flex-wrap gap-3 mt-3">
                        <a
                          href="tel:+573000000000"
                          className="inline-flex items-center gap-1.5 text-xs font-semibold text-amber-900 hover:underline cursor-pointer">
                          <Phone className="size-3.5" />
                          {TELEFONO_HOTEL}
                        </a>
                        <a
                          href={WHATSAPP_HOTEL}
                          target="_blank"
                          rel="noreferrer"
                          className="inline-flex items-center gap-1.5 text-xs font-semibold text-amber-900 hover:underline cursor-pointer">
                          <FaWhatsapp className="size-3.5" />
                          WhatsApp
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {!isLoading && allUnavailable && !bloqueada && (
                <div className="flex items-start gap-3 mb-6 p-4 rounded-xl bg-red-50 border border-red-200">
                  <XCircle className="size-5 text-red-400 shrink-0 mt-0.5" />
                  <div>
                    <p className="text-sm font-semibold text-red-800">
                      No hay habitaciones disponibles
                    </p>
                    <p className="text-sm text-red-600 mt-1">
                      Para las fechas seleccionadas no hay habitaciones
                      disponibles. Intenta con otro rango de fechas.
                    </p>
                  </div>
                </div>
              )}

              {!bloqueada &&
                (isLoading || isLoadingDisponibilidad || fetchError ? (
                  <div className="space-y-5">
                    {[1, 2, 3].map((i) => (
                      <RoomTypeCardSkeleton key={i} />
                    ))}
                  </div>
                ) : (
                  <div className="space-y-5">
                    {tipos.map((tipo) => {
                      const avail = availabilityMap?.[tipo.id];
                      return (
                        <RoomTypeCard
                          key={tipo.id}
                          room={tipo}
                          quantity={selections[tipo.id] ?? 0}
                          onQuantityChange={(v) =>
                            handleQuantityChange(tipo.id, v)
                          }
                          onSelect={() => handleSelectRoom(tipo.id)}
                          occupantSummary={getOccupantSummary(tipo.id)}
                          availableCount={avail?.disponibles}
                          totalRooms={avail?.total}
                        />
                      );
                    })}
                  </div>
                ))}

              {totalRooms > 0 && (
                <div className="lg:hidden mt-8 p-5 rounded-2xl bg-white border border-stone-200 shadow-sm">
                  <div className="flex items-center justify-between mb-3">
                    <div>
                      <p className="text-xs text-stone-500 uppercase tracking-wider font-medium">
                        Total estimado
                      </p>
                      <p className="text-2xl font-bold text-stone-900 tabular-nums mt-0.5">
                        ${totalPrice.toLocaleString("es-CO")}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={handleBook}
                      disabled={allUnavailable || isSubmitting}
                      className="px-6 py-2.5 rounded-xl bg-rose-700 text-white font-semibold text-sm cursor-pointer shadow-lg shadow-rose-900/20 hover:bg-rose-800 hover:shadow-xl hover:shadow-rose-900/30 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-lg disabled:hover:bg-rose-700">
                      {isSubmitting ? "Creando reserva…" : "Continuar al pago"}
                    </button>
                  </div>
                  <p className="text-[10px] text-stone-400 text-center">
                    No incluye impuestos ni cargos adicionales
                  </p>
                </div>
              )}
            </div>

            {!bloqueada && (
              <div className="w-full lg:w-95 xl:w-105 shrink-0 lg:sticky lg:top-30 lg:self-start">
                <BookingSummarySidebar
                  checkIn={checkIn}
                  checkOut={checkOut}
                  nights={nights}
                  selections={selectionsForSidebar}
                  totalPrice={totalPrice}
                  totalRooms={totalRooms}
                  totalGuests={totalGuests}
                  extraPersonCharge={extraPersonCharge}
                  allUnavailable={allUnavailable}
                  isSubmitting={isSubmitting}
                  onBook={handleBook}
                />
              </div>
            )}
          </div>

          <TarifasEstaticas rooms={rooms} />
        </div>
      </PublicLayout>

      {configModalRoomType && (
        <OccupantConfigModal
          open={configModalType !== null}
          onClose={() => setConfigModalType(null)}
          roomType={configModalRoomType}
          quantity={selections[configModalRoomType.id] ?? 0}
          occupants={occupantConfig[configModalRoomType.id] ?? []}
          onOccupantsChange={(occupants) =>
            setOccupantConfig((prev) => ({
              ...prev,
              [configModalRoomType.id]: occupants,
            }))
          }
          onConfirm={() => setConfigModalType(null)}
        />
      )}
    </>
  );
}
