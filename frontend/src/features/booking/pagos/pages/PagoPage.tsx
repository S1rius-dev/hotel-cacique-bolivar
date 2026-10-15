import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "sonner";
import { SEOHead } from "@/components/seo";
import PublicLayout from "@/layouts/public/PublicLayout";
import PaymentWidget, {
  type PaymentWidgetHandle,
  type PaymentWidgetConfig,
} from "../components/PaymentWidget";
import { usePaymentGatewayConfig } from "@/features/payments/hooks/usePaymentGatewayConfig";
import { useIniciarCheckout } from "../hooks/useIniciarCheckout";
import { useConsultarStatus } from "../hooks/useConsultarStatus";
import { useVerificarPago } from "../hooks/useVerificarPago";
import { useReservaPagoData } from "../hooks/useReservaPagoData";
import { buildBookingSelections, type HabitacionTipoRef } from "../utils/buildSelections";
import { useTiposHabitacion } from "@/features/booking/hooks/useTiposHabitacion";
import { formatDateShort } from "@/utils/date";
import { extractErrorMessage } from "@/lib/extract-error";
import type { CheckoutRequest } from "@/api/generated/openAPIDefinition.schemas";
import type { BookingRoomType } from "@/features/booking/types";
import { ArrowLeft } from "lucide-react";
import PaymentStepper from "../components/PaymentStepper";
import BookingSummaryCard from "../components/BookingSummaryCard";
import PaymentMethodCard from "../components/PaymentMethodCard";
import TrustBenefitsBar from "../components/TrustBenefitsBar";

interface BookingState {
  reservaId: number;
  codigoReserva: string;
  checkIn?: string;
  checkOut?: string;
  nights?: number;
  totalRooms?: number;
  totalGuests?: number;
  totalPrice?: number;
  extraPersonCharge?: number;
  selections?: { tipo: BookingRoomType; quantity: number }[];
  habitaciones?: HabitacionTipoRef[];
}

const formatPrice = (value: number) => `$${value.toLocaleString("es-CO")}`;

export default function PagoPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const state = useMemo(() => (location.state ?? {}) as BookingState, [location.state]);
  const widgetRef = useRef<PaymentWidgetHandle>(null);
  const { gateway, isLoading: gatewayLoading, isSuccess: gatewayReady } = usePaymentGatewayConfig();
  const gatewayDisplayName = gateway?.displayName ?? (gatewayLoading ? "Cargando pasarela..." : "Pasarela");
  const gatewayName = gateway?.gateway ?? "";
  const isBoldGateway = gatewayName.toUpperCase() === "BOLD";
  const displayForToast = gatewayDisplayName;

  const { mutate, isPending, data: checkout, error } = useIniciarCheckout();
  const verificarMut = useVerificarPago();

  const reservaIdFromUrl = Number(searchParams.get("reserva"));
  const reservaIdUrl = Number.isInteger(reservaIdFromUrl) && reservaIdFromUrl > 0 ? reservaIdFromUrl : undefined;
  const reservaId = state?.reservaId ?? reservaIdUrl;
  const isDeepLink = state?.reservaId == null && reservaId != null;

  const [widgetError, setWidgetError] = useState<string | null>(null);
  const [checkoutAt, setCheckoutAt] = useState<number | null>(null);
  const [remainingSec, setRemainingSec] = useState<number>(20 * 60);
  const [manualVerifying, setManualVerifying] = useState(false);

  const { data: statusData, refetch: refetchStatus } = useConsultarStatus(reservaId, !!checkout || !!reservaId);
  const reservaQuery = useReservaPagoData(reservaId, isDeepLink);
  const { data: tipos } = useTiposHabitacion();
  const isHydrating = isDeepLink && reservaQuery.isFetching;

  const codigoReserva = state?.codigoReserva || statusData?.codigoReserva || reservaQuery.data?.codigoReserva;
  const totalPrice = state?.totalPrice ?? statusData?.totalReserva ?? reservaQuery.data?.total;
  const nights = state?.nights ?? reservaQuery.data?.noches;
  const checkIn = state?.checkIn ?? (reservaQuery.data ? formatDateShort(reservaQuery.data.fechaCheckIn) : undefined);
  const checkOut = state?.checkOut ?? (reservaQuery.data ? formatDateShort(reservaQuery.data.fechaCheckOut) : undefined);

  const habitaciones = useMemo<HabitacionTipoRef[] | undefined>(() => {
    const fromState = state?.habitaciones;
    if (fromState?.length) return fromState;
    const fromApi = reservaQuery.data?.habitaciones;
    if (!fromApi?.length) return undefined;
    return fromApi.map((h) => ({ tipo: h.tipoHabitacion }));
  }, [state?.habitaciones, reservaQuery.data?.habitaciones]);

  const selections = useMemo(
    () =>
      state?.selections?.length
        ? state.selections
        : buildBookingSelections(habitaciones, tipos),
    [state, habitaciones, tipos],
  );

  const estadoPasarela = statusData?.estadoPasarela;
  const isExpired = estadoPasarela === "EXPIRED" || estadoPasarela === "VOIDED";
  const isApproved = estadoPasarela === "APPROVED";
  const isDeclined = estadoPasarela === "DECLINED" || estadoPasarela === "ERROR" || estadoPasarela === "REJECTED";

  useEffect(() => {
    if (!reservaId) {
      toast.error("No se encontró la reserva a pagar");
      navigate("/reservar", { replace: true });
    }
  }, [reservaId, navigate]);

  useEffect(() => {
    if (!checkoutAt) return;
    const id = setInterval(() => {
      const elapsed = Math.floor((Date.now() - checkoutAt) / 1000);
      const remaining = Math.max(0, 20 * 60 - elapsed);
      setRemainingSec(remaining);
      if (remaining === 0) clearInterval(id);
    }, 1000);
    return () => clearInterval(id);
  }, [checkoutAt]);

  useEffect(() => {
    if (isApproved) {
      toast.success("¡Pago aprobado! Confirmando reserva…");
      const codigo = statusData?.codigoReserva ?? codigoReserva;
      navigate(`/pago/exito?codigoReserva=${encodeURIComponent(codigo ?? "")}`, { replace: true });
    }
  }, [isApproved, statusData, codigoReserva, navigate]);

  useEffect(() => {
    if (isExpired) toast.error("La reserva expiró por falta de pago. Las habitaciones fueron liberadas.");
  }, [isExpired]);

  useEffect(() => {
    if (isDeclined) toast.error("El pago fue rechazado. Puedes reintentar con otro medio de pago.");
  }, [isDeclined]);

  const handleStartPayment = useCallback(() => {
    if (!reservaId) return;
    setWidgetError(null);
    // Decisión 3: frontend solo inicia intención — no envía TARJETA_CREDITO hardcodeado; Wompi/webhook es fuente de verdad
    const request: CheckoutRequest = {
      reservaId,
      monto: totalPrice ?? 0,
      observaciones: `Anticipo reserva ${codigoReserva ?? ""}`,
    } as CheckoutRequest;
    mutate(request, {
      onSuccess: () => {
        setCheckoutAt(Date.now());
        setRemainingSec(20 * 60);
        setTimeout(() => refetchStatus(), 800);
      },
      onError: (err: unknown) => {
        const msg = extractErrorMessage(err, "No se pudo iniciar el pago");
        // La reserva expirada merece un aviso propio, pero cualquier otro fallo
        // debe mostrar el mensaje real del backend en vez de desaparecer.
        if (msg.toLowerCase().includes("cancelada") || msg.toLowerCase().includes("expir")) {
          toast.error("Esta reserva ya expiró. Por favor crea una nueva reserva.");
        } else {
          toast.error(msg);
        }
      },
    });
  }, [reservaId, totalPrice, codigoReserva, mutate, refetchStatus]);

  const handleVerificar = useCallback(async () => {
    if (!reservaId) return;
    setManualVerifying(true);
    try {
      const res = await verificarMut.mutateAsync(reservaId);
      await refetchStatus();
      if (res?.estadoPasarela === "APPROVED") {
        toast.success("Pago verificado y aprobado");
        navigate(`/pago/exito?codigoReserva=${encodeURIComponent(res.codigoReserva ?? codigoReserva ?? "")}`, { replace: true });
      } else if (res?.estadoPasarela === "EXPIRED") {
        toast.error("La reserva expiró");
      } else {
        toast.info(`Estado actual: ${res?.estadoPasarela ?? "PENDIENTE"}`);
      }
    } catch (error) {
      toast.error(
        extractErrorMessage(error, `No se pudo verificar el pago en ${displayForToast}`),
      );
    } finally {
      setManualVerifying(false);
    }
  }, [reservaId, verificarMut, refetchStatus, navigate, codigoReserva, displayForToast]);

  const handleRetry = useCallback(() => {
    setWidgetError(null);
    handleStartPayment();
  }, [handleStartPayment]);

  const widgetConfig = useMemo<PaymentWidgetConfig | null>(() => {
    if (
      !checkout?.publicKey ||
      !checkout?.referenciaExterna ||
      !checkout?.signatureIntegrity ||
      (checkout.amountInCents == null && checkout.monto == null)
    )
      return null;
    const amountInCents =
      checkout.amountInCents != null ? Number(checkout.amountInCents) : Math.round(Number(checkout.monto) * 100);
    if (!amountInCents) return null;
    const cfg = {
      publicKey: checkout.publicKey,
      amountInCents,
      currency: checkout.moneda ?? "COP",
      reference: checkout.referenciaExterna,
      integritySignature: checkout.signatureIntegrity,
      redirectUrl: checkout.redirectUrl ?? undefined,
    };
    console.debug("[PagoPage] widgetConfig:", cfg, " raw checkout:", checkout, " gateway:", gatewayName);
    return cfg;
  }, [checkout, gatewayName]);

  useEffect(() => {
    const handler = (e: ErrorEvent) => {
      if (String(e.message).includes("403") || String(e.filename).includes("checkout.wompi.co") || String(e.filename).includes("bold.co")) {
        console.error("[PagoPage] Error 403 detectado en pasarela:", e.message, e.filename);
      }
    };
    const rejHandler = (e: PromiseRejectionEvent) => {
      console.error("[PagoPage] Unhandled rejection (posible pasarela):", e.reason);
    };
    window.addEventListener("error", handler);
    window.addEventListener("unhandledrejection", rejHandler);
    return () => {
      window.removeEventListener("error", handler);
      window.removeEventListener("unhandledrejection", rejHandler);
    };
  }, []);

  useEffect(() => {
    if (widgetConfig && widgetRef.current && !isExpired && !isApproved) {
      const t = setTimeout(() => widgetRef.current?.open(), 300);
      return () => clearTimeout(t);
    }
  }, [widgetConfig, isExpired, isApproved]);

  const minutes = Math.floor(remainingSec / 60);
  const seconds = remainingSec % 60;
  const ttlLabel = `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;

  return (
    <>
      <SEOHead
        title="Pago"
        description="Completa el pago de tu reserva de forma segura"
        url="/pago"
        noindex
      />
      <PublicLayout>
        <div className="bg-stone-50">
          <div className="mx-auto max-w-6xl px-4 md:px-10 pt-6 pb-10">
            {/* Stepper */}
            <PaymentStepper currentStep={3} />

            {/* Contenido principal */}
            <div className="mt-8 grid grid-cols-1 lg:grid-cols-[1.45fr_0.95fr] gap-8 items-start">
              {/* COL IZQUIERDA */}
              <div className="space-y-4 order-1">
                <button
                  type="button"
                  onClick={() => navigate("/reservar")}
                  className="inline-flex items-center gap-2 text-sm text-stone-500 hover:text-rose-700 transition-colors cursor-pointer"
                >
                  <ArrowLeft className="size-4" />
                  Volver a la reserva
                </button>

                <div>
                  <h1 className="font-heading text-2xl sm:text-3xl text-stone-900 leading-tight tracking-tight">
                    Pago seguro
                  </h1>
                  <p className="text-sm text-stone-500 mt-2 leading-relaxed max-w-md">
                    {gatewayLoading || isHydrating ? (
                      <span className="inline-flex items-center gap-2">
                        <span className="size-3 animate-pulse rounded-full bg-stone-300" />
                        {gatewayLoading ? "Cargando pasarela..." : "Cargando reserva..."}
                      </span>
                    ) : (
                      <>Completa tu pago de forma segura a través de {gatewayDisplayName}.</>
                    )}
                  </p>
                </div>

                {gatewayLoading || isHydrating ? (
                  <div className="rounded-2xl border border-stone-200 bg-white shadow-sm p-6 animate-pulse">
                    <div className="h-5 w-32 bg-stone-200 rounded mb-3" />
                    <div className="h-3 w-full bg-stone-100 rounded" />
                  </div>
                ) : (
                  <PaymentMethodCard
                    checkoutMonto={checkout?.monto ?? null}
                    ttlLabel={ttlLabel}
                    showTtl={!!checkout}
                    isPending={isPending}
                    error={error}
                    widgetError={widgetError}
                    estadoPasarela={estadoPasarela ?? null}
                    isExpired={isExpired}
                    isApproved={isApproved}
                    isDeclined={isDeclined}
                    referenciaExterna={statusData?.referenciaExterna ?? null}
                    codigoReserva={codigoReserva ?? null}
                    hasWidgetConfig={!!widgetConfig}
                    manualVerifying={manualVerifying}
                    onStartPayment={handleStartPayment}
                    onOpenWidget={() => widgetRef.current?.open()}
                    onRetry={handleRetry}
                    onVerificar={handleVerificar}
                    onNavigateReservar={() => navigate("/reservar")}
                    onNavigateInicio={() => navigate("/")}
                    onVerDetalles={() => navigate("/mis-reservas")}
                    onDescargar={() => window.print()}
                    formatPrice={formatPrice}
                    gatewayDisplayName={gatewayDisplayName}
                    gatewayName={gatewayName}
                    gatewayLogoUrl={gateway?.logoUrl ?? null}
                    securePaymentText={gateway?.securePaymentText ?? `Pago seguro con ${gatewayDisplayName}`}
                    widgetConfig={widgetConfig}
                    onWidgetError={setWidgetError}
                    gatewayReady={gatewayReady}
                  />
                )}
              </div>

              {/* COL DERECHA */}
              <div className="order-2 lg:sticky lg:top-28 self-start">
                <BookingSummaryCard
                  selections={selections}
                  checkIn={checkIn}
                  checkOut={checkOut}
                  nights={nights}
                  totalPrice={totalPrice}
                  extraPersonCharge={state?.extraPersonCharge}
                  totalRooms={state?.totalRooms}
                  checkoutMonto={checkout?.monto != null ? Number(checkout.monto) : null}
                />
              </div>
            </div>

            {/* Beneficios */}
            <div className="mt-8">
              <TrustBenefitsBar />
            </div>
          </div>
        </div>

        {widgetConfig && !isBoldGateway ? (
          <PaymentWidget
            key={widgetConfig.reference}
            ref={widgetRef}
            gateway={gatewayName}
            publicKey={widgetConfig.publicKey}
            amountInCents={widgetConfig.amountInCents}
            currency={widgetConfig.currency}
            reference={widgetConfig.reference}
            integritySignature={widgetConfig.integritySignature}
            redirectUrl={widgetConfig.redirectUrl}
            onTransaction={(transaction) => {
              console.log("[PagoPage] Transacción recibida del Widget:", transaction);
              toast.info(`Transacción recibida. Validando con ${gatewayDisplayName}… La confirmación puede tardar unos segundos.`);
              setTimeout(() => refetchStatus(), 1500);
            }}
            onOpenError={(message) => {
              setWidgetError(message);
              toast.error(message);
            }}
          />
        ) : null}
      </PublicLayout>
    </>
  );
}
