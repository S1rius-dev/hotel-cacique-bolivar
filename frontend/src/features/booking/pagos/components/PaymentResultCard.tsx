import { Check, X, Download, ArrowLeft, Home, ShieldCheck, RefreshCw } from "lucide-react";
import wompiPrincipal from "@/assets/wompi/Wompi_LogoPrincipal.svg";
import { resolveGatewayAssetUrl } from "@/features/payments/constants/gateway";

type Variant = "success" | "error";
type ErrorSubtype = "declined" | "expired" | "generic";

interface Props {
  variant: Variant;
  /** Código real de reserva (referenciaExterna / codigoReserva) */
  codigoReserva?: string | null;
  /** Fecha real formateada, ej "27 ago 2026 · 10:45" */
  fecha?: string | null;
  /** Mensaje real de la pasarela / error */
  motivo?: string | null;
  errorSubtype?: ErrorSubtype;
  onVerDetalles?: () => void;
  onDescargar?: () => void;
  onReintentar?: () => void;
  onVolverPago?: () => void;
  onVolverInicio?: () => void;
  gatewayDisplayName?: string;
  gatewayName?: string;
  gatewayLogoUrl?: string | null;
  securePaymentText?: string;
}

function formatFechaFallback(): string {
  const d = new Date();
  const fecha = d.toLocaleDateString("es-CO", { day: "2-digit", month: "short", year: "numeric" });
  const hora = d.toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit", hour12: false });
  return `${fecha} · ${hora}`;
}

export default function PaymentResultCard({
  variant,
  codigoReserva,
  fecha,
  motivo,
  errorSubtype = "generic",
  onVerDetalles,
  onDescargar,
  onReintentar,
  onVolverPago,
  onVolverInicio,
  gatewayDisplayName,
  gatewayName,
  gatewayLogoUrl,
  securePaymentText,
}: Props) {
  const isSuccess = variant === "success";
  const displayFecha = fecha ?? formatFechaFallback();
  const displayName = gatewayDisplayName || gatewayName || "Pasarela";
  const secureText = securePaymentText || `Pago seguro con ${displayName}`;
  const isBold = (gatewayName ?? "").toUpperCase() === "BOLD";
  const isWompi = (gatewayName ?? "").toUpperCase() === "WOMPI";
  // logoUrl llega como ruta relativa al backend (/assets/...), hay que resolverla
  // contra la API o el <img> pediría el archivo al origen del frontend y daría 404.
  const logoPrincipal =
    resolveGatewayAssetUrl(gatewayLogoUrl) ?? (isWompi ? wompiPrincipal : null);

  return (
    <div className="rounded-2xl border border-stone-200 bg-white shadow-sm overflow-hidden">
      <div className="px-6 sm:px-8 py-8 sm:py-10 text-center">
        {/* Icon */}
        <div
          className={`mx-auto flex size-16 items-center justify-center rounded-full border
            ${isSuccess ? "bg-green-50 border-green-100 text-green-600" : "bg-rose-50 border-rose-100 text-rose-600"}`}
        >
          {isSuccess ? <Check className="size-7" strokeWidth={2.5} /> : <X className="size-7" strokeWidth={2.5} />}
        </div>

        {/* Title */}
        <h2 className="mt-5 font-heading text-2xl sm:text-[26px] font-semibold tracking-tight text-stone-900 leading-tight">
          {isSuccess ? (
            "¡Pago exitoso!"
          ) : errorSubtype === "expired" ? (
            "Reserva expirada"
          ) : (
            <>
              No pudimos procesar
              <br />
              tu pago
            </>
          )}
        </h2>

        {/* Description */}
        <div className="mt-3 space-y-1">
          {isSuccess ? (
            <>
              <p className="text-sm text-stone-600">Tu reserva ha sido confirmada correctamente.</p>
              <p className="text-xs text-stone-500">Hemos enviado los detalles de tu reserva a tu correo electrónico.</p>
            </>
          ) : errorSubtype === "expired" ? (
            <>
              <p className="text-sm text-stone-600">No se completó el pago en 20 minutos.</p>
              <p className="text-xs text-stone-500">Las habitaciones fueron liberadas. Puedes hacer una nueva reserva.</p>
            </>
          ) : (
            <>
              <p className="text-sm text-stone-600">No se realizó ningún cobro. Puedes intentarlo</p>
              <p className="text-sm text-stone-600 -mt-1">nuevamente.</p>
            </>
          )}
        </div>

        {/* Info grid - success */}
        {isSuccess && (
          <div className="mt-6 grid grid-cols-2 gap-3 text-left rounded-xl border border-stone-200 bg-stone-50 p-3 sm:p-4">
            <div className="min-w-0">
              <p className="text-[11px] tracking-[0.12em] uppercase text-stone-400 font-medium">Código de reserva</p>
              <p className="mt-1 font-mono text-xs sm:text-sm font-semibold text-stone-800 truncate">
                {codigoReserva ?? "—"}
              </p>
            </div>
            <div className="min-w-0 text-right sm:text-left">
              <p className="text-[11px] tracking-[0.12em] uppercase text-stone-400 font-medium">Fecha</p>
              <p className="mt-1 text-xs sm:text-sm font-medium text-stone-700 truncate">{displayFecha}</p>
            </div>
          </div>
        )}

        {/* Motivo - error */}
        {!isSuccess && motivo && (
          <div className="mt-6 rounded-xl border border-rose-200 bg-rose-50/70 p-3 text-left">
            <p className="text-[11px] tracking-[0.12em] uppercase text-rose-700/70 font-medium">Motivo</p>
            <p className="mt-1 text-xs sm:text-sm text-rose-800 leading-relaxed break-words">{motivo}</p>
          </div>
        )}

        {/* Primary CTA */}
        <div className="mt-6 space-y-3">
          {isSuccess ? (
            <button
              type="button"
              onClick={onVerDetalles}
              className="w-full inline-flex items-center justify-center rounded-xl bg-rose-700 hover:bg-rose-800 text-white font-semibold text-sm px-6 py-3.5 shadow-lg shadow-rose-900/20 transition-colors cursor-pointer"
            >
              Ver detalles de mi reserva
            </button>
          ) : errorSubtype === "expired" ? (
            <button
              type="button"
              onClick={onVolverPago}
              className="w-full inline-flex items-center justify-center rounded-xl bg-rose-700 hover:bg-rose-800 text-white font-semibold text-sm px-6 py-3.5 shadow-lg shadow-rose-900/20 transition-colors cursor-pointer"
            >
              Hacer nueva reserva
            </button>
          ) : (
            <button
              type="button"
              onClick={onReintentar}
              className="w-full inline-flex items-center justify-center gap-2 rounded-xl bg-rose-700 hover:bg-rose-800 text-white font-semibold text-sm px-6 py-3.5 shadow-lg shadow-rose-900/20 transition-colors cursor-pointer"
            >
              <RefreshCw className="size-4" />
              Intentar de nuevo
            </button>
          )}

          {/* Secondary CTAs */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {isSuccess ? (
              <>
                <button
                  type="button"
                  onClick={onDescargar}
                  className="inline-flex items-center justify-center gap-2 rounded-xl border border-stone-200 bg-white hover:bg-stone-50 text-stone-700 font-medium text-sm px-4 py-3 transition-colors cursor-pointer"
                >
                  <Download className="size-4" />
                  Descargar comprobante
                </button>
                <button
                  type="button"
                  onClick={onVolverInicio}
                  className="inline-flex items-center justify-center gap-2 rounded-xl border border-stone-200 bg-white hover:bg-stone-50 text-stone-700 font-medium text-sm px-4 py-3 transition-colors cursor-pointer"
                >
                  <Home className="size-4" />
                  Volver al inicio
                </button>
              </>
            ) : (
              <>
                {onVolverPago && errorSubtype !== "expired" && (
                  <button
                    type="button"
                    onClick={onVolverPago}
                    className="inline-flex items-center justify-center gap-2 rounded-xl border border-stone-200 bg-white hover:bg-stone-50 text-stone-700 font-medium text-sm px-4 py-3 transition-colors cursor-pointer"
                  >
                    <ArrowLeft className="size-4" />
                    Volver al pago
                  </button>
                )}
                <button
                  type="button"
                  onClick={onVolverInicio}
                  className={`inline-flex items-center justify-center gap-2 rounded-xl border border-stone-200 bg-white hover:bg-stone-50 text-stone-700 font-medium text-sm px-4 py-3 transition-colors cursor-pointer ${onVolverPago && errorSubtype !== "expired" ? "" : "sm:col-span-2"}`}
                >
                  <Home className="size-4" />
                  Volver al inicio
                </button>
              </>
            )}
          </div>
        </div>

        {/* Pasarela footer */}
        <div className="mt-6 rounded-xl border border-stone-200 bg-stone-50 px-4 py-3 flex gap-3 text-left">
          <div className="size-8 rounded-lg bg-white border border-stone-200 flex items-center justify-center shrink-0">
            <ShieldCheck className={`size-4 ${isSuccess ? "text-green-600" : "text-stone-400"}`} />
          </div>
          <div className="min-w-0">
            <p className="flex items-center gap-1.5 text-xs font-medium text-stone-700">
              <span>{isSuccess ? secureText : "No se realizó ningún cobro"}</span>
              {isSuccess && isWompi && logoPrincipal && (
                <img src={logoPrincipal} alt={displayName} className="h-3.5 w-auto object-contain opacity-70 inline" />
              )}
              {isSuccess && isBold && (
                <span className="font-bold text-stone-700">{displayName}</span>
              )}
            </p>
            <p className="text-xs text-stone-500 leading-relaxed mt-0.5">
              {isSuccess
                ? "Si tienes alguna duda, nuestro equipo está listo para ayudarte."
                : "Tu reserva sigue vigente hasta que expire el tiempo. Puedes reintentar."}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
