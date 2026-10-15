import { useEffect } from "react";
import {
  CreditCard,
  Lock,
  Clock,
  AlertTriangle,
  ShieldCheck,
} from "lucide-react";
import wompiPrincipal from "@/assets/wompi/Wompi_LogoPrincipal.svg";
import wompiContraccion from "@/assets/wompi/Wompi_ContraccionPrincipal.svg";
import { resolveGatewayAssetUrl } from "@/features/payments/constants/gateway";
import PaymentResultCard from "./PaymentResultCard";
import BoldWidget from "./BoldWidget";
import BoldPaymentMethodsCarousel from "./BoldPaymentMethodsCarousel";

interface Props {
  checkoutMonto?: number | string | null;
  ttlLabel?: string;
  showTtl?: boolean;
  isPending?: boolean;
  error?: unknown;
  widgetError?: string | null;
  estadoPasarela?: string | null;
  isExpired?: boolean;
  isApproved?: boolean;
  isDeclined?: boolean;
  referenciaExterna?: string | null;
  codigoReserva?: string | null;
  hasWidgetConfig?: boolean;
  manualVerifying?: boolean;
  onStartPayment?: () => void;
  onOpenWidget?: () => void;
  onRetry?: () => void;
  onVerificar?: () => void;
  onNavigateReservar?: () => void;
  onNavigateInicio?: () => void;
  onVerDetalles?: () => void;
  onDescargar?: () => void;
  formatPrice: (v: number) => string;
  gatewayDisplayName?: string;
  gatewayName?: string;
  gatewayLogoUrl?: string | null;
  securePaymentText?: string;
  widgetConfig?: {
    publicKey: string;
    amountInCents: number;
    currency: string;
    reference: string;
    integritySignature: string;
    redirectUrl?: string;
  } | null;
  onWidgetError?: (message: string) => void;
  gatewayReady?: boolean;
}

export default function PaymentMethodCard({
  checkoutMonto,
  ttlLabel,
  showTtl,
  isPending,
  error,
  widgetError,
  estadoPasarela,
  isExpired,
  isApproved,
  isDeclined,
  referenciaExterna,
  codigoReserva,
  hasWidgetConfig,
  onStartPayment,
  onOpenWidget,
  onRetry,
  onNavigateReservar,
  onNavigateInicio,
  onVerDetalles,
  onDescargar,
  formatPrice,
  gatewayDisplayName,
  gatewayName,
  gatewayLogoUrl,
  securePaymentText,
  widgetConfig,
  onWidgetError,
  gatewayReady = true,
}: Props) {
  const displayName =
    gatewayDisplayName ||
    gatewayName ||
    (gatewayReady ? "Pasarela" : "Cargando pasarela...");
  const secureText =
    securePaymentText ||
    (gatewayReady ? `Pago seguro con ${displayName}` : "Cargando pasarela...");
  const isBold = (gatewayName ?? "").toUpperCase() === "BOLD";
  const isWompi = (gatewayName ?? "").toUpperCase() === "WOMPI";
  // logoUrl llega como ruta relativa al backend (/assets/...), hay que resolverla
  // contra la API o el <img> pediría el archivo al origen del frontend y daría 404.
  const remoteLogo = resolveGatewayAssetUrl(gatewayLogoUrl);
  const logoPrincipal =
    remoteLogo ?? (gatewayReady && isWompi ? wompiPrincipal : null);
  const logoContraccion =
    remoteLogo ?? (gatewayReady && isWompi ? wompiContraccion : null);

  // Para Bold: auto-iniciar checkout para obtener reference/hash y mostrar botón Bold embebido sin requerir "Pagar ahora"
  useEffect(() => {
    if (
      isBold &&
      gatewayReady &&
      !hasWidgetConfig &&
      !isPending &&
      !isExpired &&
      !isApproved &&
      !isDeclined &&
      onStartPayment
    ) {
      console.debug("[PaymentMethodCard] Auto-iniciando checkout Bold");
      onStartPayment();
    }
  }, [
    isBold,
    gatewayReady,
    hasWidgetConfig,
    isPending,
    isExpired,
    isApproved,
    isDeclined,
    onStartPayment,
  ]);
  const fechaReal = new Date().toLocaleDateString("es-CO", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
  const horaReal = new Date().toLocaleTimeString("es-CO", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
  const fechaDisplay = `${fechaReal} · ${horaReal}`;
  const codigoDisplay = referenciaExterna ?? codigoReserva ?? null;
  const motivoDeclined =
    (widgetError as string | null) ??
    (typeof error === "object" && error !== null && "message" in error
      ? String((error as { message?: string }).message)
      : null) ??
    (estadoPasarela ? `Estado: ${estadoPasarela}` : null);
  return (
    <div className="rounded-2xl border border-stone-200 bg-white shadow-sm p-5 sm:p-6 space-y-5">
      {/* Header */}
      <div className="flex items-center gap-2.5">
        <div className="size-8 rounded-lg bg-rose-50 border border-rose-100 flex items-center justify-center shrink-0">
          <CreditCard className="size-4 text-rose-700" />
        </div>
        <h2 className="text-sm font-semibold text-stone-800 tracking-wide">
          Método de pago
        </h2>
      </div>
      <p className="text-sm text-stone-500 leading-relaxed">
        Serás redirigido a la pasarela segura de {displayName} para completar tu
        pago.
      </p>
      {/* Aviso seguridad */}
      <div className="rounded-xl border border-stone-200 bg-stone-50 px-4 py-3 flex gap-3">
        <div className="size-8 rounded-lg bg-white border border-stone-200 flex items-center justify-center shrink-0">
          <ShieldCheck className="size-4 text-emerald-600" />
        </div>
        <div>
          <p className="text-xs font-semibold text-stone-800 flex items-center gap-1.5">
            <span className="inline-flex size-1.5 rounded-full bg-emerald-500" />{" "}
            Tu pago está protegido
          </p>
          <p className="text-xs text-stone-500 leading-relaxed mt-1">
            Encriptación SSL de 256 bits. Tus datos están seguros y nunca se
            almacenan en nuestros servidores.
          </p>
        </div>
      </div>
      {/* Banners dinámicos */}
      {showTtl && !isExpired && !isApproved && ttlLabel ? (
        <div className="flex items-center gap-2 rounded-xl border border-amber-200 bg-amber-50 px-3 py-2.5">
          <Clock className="size-4 text-amber-600 shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-amber-900">
              Tienes {ttlLabel} para pagar
            </p>
            <p className="text-xs text-amber-700">
              Si no se confirma, la reserva se cancelará automáticamente.
            </p>
          </div>
          <span className="font-mono text-sm font-bold text-amber-900 tabular-nums shrink-0">
            {ttlLabel}
          </span>
        </div>
      ) : null}
      {estadoPasarela === "PENDING" && !isExpired && !isApproved ? (
        <div className="flex items-center gap-2 rounded-xl border border-blue-200 bg-blue-50 px-3 py-2.5 text-sm text-blue-800">
          <Clock className="size-4 animate-pulse shrink-0" />
          <span>Validando pago con</span>
          {isBold ? (
            <span className="font-semibold text-sm">{displayName}</span>
          ) : logoPrincipal ? (
            <img
              src={logoPrincipal}
              alt={displayName}
              className="h-3.5 w-auto object-contain"
            />
          ) : (
            <span className="font-semibold text-sm">{displayName}</span>
          )}
          <span>… se actualizará automáticamente</span>
        </div>
      ) : null}
      {error ? (
        <div className="rounded-xl border border-red-200 bg-red-50 p-3">
          <p className="text-sm text-red-700 flex items-center gap-2">
            <AlertTriangle className="size-4 shrink-0" />
            No se pudo iniciar el pago. Reintenta.
          </p>
          <p className="text-xs text-red-600 mt-1">
            Si viste el error con el widget, es probable que la firma expirara.
            Genera un nuevo intento.
          </p>
        </div>
      ) : null}
      {widgetError ? (
        <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          {widgetError}
        </div>
      ) : null}
      {/* Estados terminales — reemplazado por PaymentResultCard */}
      {isExpired ? (
        <PaymentResultCard
          variant="error"
          errorSubtype="expired"
          codigoReserva={codigoDisplay}
          fecha={fechaDisplay}
          motivo={referenciaExterna ? `Referencia: ${referenciaExterna}` : null}
          onVolverPago={onNavigateReservar}
          onVolverInicio={onNavigateInicio}
          gatewayDisplayName={displayName}
          gatewayName={gatewayName}
          gatewayLogoUrl={gatewayLogoUrl}
          securePaymentText={secureText}
        />
      ) : isApproved ? (
        <PaymentResultCard
          variant="success"
          codigoReserva={codigoDisplay}
          fecha={fechaDisplay}
          onVerDetalles={onVerDetalles}
          onDescargar={onDescargar}
          onVolverInicio={onNavigateInicio}
          gatewayDisplayName={displayName}
          gatewayName={gatewayName}
          gatewayLogoUrl={gatewayLogoUrl}
          securePaymentText={secureText}
        />
      ) : isDeclined ? (
        <PaymentResultCard
          variant="error"
          errorSubtype="declined"
          motivo={motivoDeclined}
          onReintentar={onRetry}
          onVolverPago={onNavigateReservar}
          onVolverInicio={onNavigateInicio}
          gatewayDisplayName={displayName}
          gatewayName={gatewayName}
          gatewayLogoUrl={gatewayLogoUrl}
          securePaymentText={secureText}
        />
      ) : (
        <>
          {/* Pasarela */}
          <div className="space-y-3">
            <p className="text-xs font-medium text-stone-500 tracking-[0.14em] uppercase">
              Pasarela de pago
            </p>
            <div className="rounded-xl border border-stone-200 bg-white px-4 py-4 flex flex-col gap-3">
              <div className="flex items-center justify-between gap-3">
                {isBold ? (
                  <span className="text-lg font-bold tracking-tight text-stone-800">
                    {displayName}
                  </span>
                ) : logoPrincipal ? (
                  <img
                    src={logoPrincipal}
                    alt={displayName}
                    className="h-7 w-auto object-contain"
                  />
                ) : (
                  <span className="text-sm font-semibold text-stone-700">
                    {displayName}
                  </span>
                )}
                <span className="text-[10px] font-medium tracking-wide text-stone-400 border border-stone-200 rounded-full px-2.5 py-1 bg-stone-50">
                  Pasarela oficial
                </span>
              </div>
              {isBold ? (
                <BoldPaymentMethodsCarousel className="w-full" />
              ) : (
                <div className="flex items-center gap-1.5 flex-wrap">
                  <span className="px-2 py-1 rounded-md bg-white border border-stone-200 text-[10px] font-bold tracking-wide text-stone-700">
                    VISA
                  </span>
                  <span className="px-2 py-1 rounded-md bg-white border border-stone-200 text-[10px] font-bold tracking-wide text-stone-700">
                    MC
                  </span>
                  <span className="px-2 py-1 rounded-md bg-white border border-stone-200 text-[10px] font-bold tracking-wide text-stone-700">
                    AMEX
                  </span>
                  <span className="px-2 py-1 rounded-md bg-white border border-stone-200 text-[10px] font-bold tracking-wide text-rose-700">
                    PSE
                  </span>
                  <span className="px-2 py-1 rounded-md bg-white border border-stone-200 text-[10px] font-bold tracking-wide text-yellow-700">
                    BANCOLOMBIA
                  </span>
                  <span className="px-2 py-1 rounded-md bg-white border border-stone-200 text-[10px] font-bold tracking-wide text-stone-700">
                    Nequi
                  </span>
                </div>
              )}
            </div>
          </div>

          {isBold ? (
            hasWidgetConfig && widgetConfig ? (
              <div className="space-y-3">
                <BoldWidget
                  apiKey={widgetConfig.publicKey}
                  amountInCents={widgetConfig.amountInCents}
                  currency={widgetConfig.currency}
                  reference={widgetConfig.reference}
                  integrityHash={widgetConfig.integritySignature}
                  redirectUrl={widgetConfig.redirectUrl}
                  description="Pago a traves de Embedded Checkout"
                  renderMode="embedded"
                  onOpenError={onWidgetError}
                />
              </div>
            ) : (
              <div className="w-full flex flex-col items-center justify-center gap-2 py-4 rounded-xl border border-stone-200 bg-stone-50">
                <div className="size-6 border-2 border-stone-300 border-t-rose-700 rounded-full animate-spin" />
                <span className="text-sm text-stone-600">
                  {isPending
                    ? "Preparando pago con Bold…"
                    : "Cargando botón de pago Bold..."}
                </span>
                <span className="text-xs text-stone-400">
                  {checkoutMonto != null
                    ? `Anticipo ${formatPrice(Number(checkoutMonto))}`
                    : ""}
                </span>
              </div>
            )
          ) : (
            <>
              <button
                type="button"
                onClick={hasWidgetConfig ? onOpenWidget : onStartPayment}
                disabled={isPending}
                className="w-full inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-rose-700 hover:bg-rose-800 disabled:bg-stone-300 disabled:cursor-not-allowed text-white font-semibold text-sm transition-all duration-200 cursor-pointer shadow-lg shadow-rose-900/20">
                <Lock className="size-4 shrink-0" />
                <span>
                  {isPending
                    ? "Preparando pago…"
                    : hasWidgetConfig
                      ? "Abrir pasarela de pago"
                      : `Pagar ahora${checkoutMonto != null ? ` · ${formatPrice(Number(checkoutMonto))}` : ""}`}
                </span>
                {logoContraccion && (
                  <span className="ml-1 inline-flex items-center justify-center bg-white rounded-md px-1.5 py-1 shrink-0">
                    <img
                      src={logoContraccion}
                      alt={displayName}
                      className="h-3.5 w-auto object-contain"
                      style={{ display: "block" }}
                    />
                  </span>
                )}
              </button>
              {hasWidgetConfig ? (
                <p className="text-xs text-center text-stone-400">
                  Si el widget no abrió, haz clic de nuevo en “Abrir pasarela de
                  pago”.
                </p>
              ) : null}
            </>
          )}
        </>
      )}
    </div>
  );
}
