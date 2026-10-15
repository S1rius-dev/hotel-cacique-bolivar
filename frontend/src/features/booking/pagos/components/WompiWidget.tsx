import {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useRef,
} from "react";

export interface WompiCustomerData {
  email?: string;
  fullName?: string;
  phoneNumber?: string;
  phoneNumberPrefix?: string;
  legalId?: string;
  legalIdType?: string;
}

export interface WompiWidgetConfig {
  publicKey: string;
  amountInCents: number;
  currency: string;
  reference: string;
  integritySignature: string;
  redirectUrl?: string;
  customer?: WompiCustomerData;
}

export interface WompiWidgetHandle {
  open: () => void;
}

export interface WompiTransaction {
  id?: string;
  status?: string;
  reference?: string;
  amountInCents?: number;
  amount_in_cents?: number;
  currency?: string;
  [key: string]: unknown;
}

export interface WompiResult {
  transaction: WompiTransaction;
}

interface WompiSignature {
  integrity: string;
}

interface WompiCheckoutOptions {
  currency: string;
  amountInCents: number;
  reference: string;
  publicKey: string;
  signature: WompiSignature;
  redirectUrl?: string;
  customerData?: WompiCustomerData;
}

interface WidgetCheckoutInstance {
  open: (callback: (result: WompiResult) => void) => void;
}

interface WidgetCheckoutConstructor {
  new (options: WompiCheckoutOptions): WidgetCheckoutInstance;
}

declare global {
  interface Window {
    WidgetCheckout?: WidgetCheckoutConstructor;
  }
}

export type WompiWidgetProps = WompiWidgetConfig & {
  onTransaction?: (transaction: WompiTransaction) => void;
  onOpenError?: (message: string) => void;
};

const SCRIPT_SRC = "https://checkout.wompi.co/widget.js";

let wompiScriptPromise: Promise<void> | null = null;

function loadScript(): Promise<void> {
  if (wompiScriptPromise) return wompiScriptPromise;

  const existing = document.querySelector<HTMLScriptElement>(
    `script[src="${SCRIPT_SRC}"]`,
  );

  wompiScriptPromise = new Promise((resolve, reject) => {
    const handleResolve = () => {
      if (window.WidgetCheckout) {
        resolve();
      } else {
        wompiScriptPromise = null;
        reject(new Error("WidgetCheckout no disponible tras cargar el script"));
      }
    };

    const handleReject = () => {
      wompiScriptPromise = null;
      reject(new Error("No se pudo cargar el widget de Wompi"));
    };

    if (existing) {
      if (window.WidgetCheckout) {
        resolve();
        return;
      }
      existing.addEventListener("load", handleResolve, { once: true });
      existing.addEventListener("error", handleReject, { once: true });
      return;
    }

    const script = document.createElement("script");
    script.src = SCRIPT_SRC;
    script.async = true;
    script.onload = handleResolve;
    script.onerror = handleReject;
    document.body.appendChild(script);
  });

  // Permite reintento si la carga falla: resetea el singleton en rechazo
  wompiScriptPromise.catch(() => {
    wompiScriptPromise = null;
  });

  return wompiScriptPromise;
}

export const WompiWidget = forwardRef<WompiWidgetHandle, WompiWidgetProps>(
  function WompiWidget(
    {
      publicKey,
      amountInCents,
      currency,
      reference,
      integritySignature,
      redirectUrl,
      customer,
      onTransaction,
      onOpenError,
    },
    ref,
  ) {
    const configRef = useRef<WompiWidgetConfig | null>(null);
    configRef.current = {
      publicKey,
      amountInCents,
      currency,
      reference,
      integritySignature,
      redirectUrl,
      customer,
    };

    const transactionRef = useRef<WompiWidgetProps["onTransaction"]>(undefined);
    transactionRef.current = onTransaction;

    const open = useCallback(() => {
      const cfg = configRef.current;
      if (!cfg) return;

      // Validación de parámetros obligatorios según doc Wompi
      // public-key, currency, amount-in-cents, reference, signature:integrity son obligatorios
      const missing: string[] = [];
      if (!cfg.publicKey) missing.push("public-key");
      if (!cfg.currency) missing.push("currency");
      if (!cfg.amountInCents) missing.push("amount-in-cents");
      if (!cfg.reference) missing.push("reference");
      if (!cfg.integritySignature) missing.push("signature:integrity");
      if (missing.length > 0) {
        const msg = `Faltan parámetros obligatorios Wompi: ${missing.join(", ")}.`;
        console.error("[WompiWidget] " + msg, cfg);
        onOpenError?.(
          msg + " Verifica que el checkout haya retornado todos los datos.",
        );
        return;
      }

      // Log de depuración — útil para cotejar con backend (reference/amount/currency/signature)
      console.debug("[WompiWidget] Abriendo checkout con:", {
        currency: cfg.currency,
        amountInCents: cfg.amountInCents,
        reference: cfg.reference,
        publicKey: cfg.publicKey,
        "signature:integrity": cfg.integritySignature?.substring(0, 8) + "…",
        redirectUrl: cfg.redirectUrl,
      });

      loadScript()
        .then(() => {
          const Constructor = window.WidgetCheckout;
          if (!Constructor) {
            throw new Error("WidgetCheckout no disponible");
          }

          // redirectUrl opcional: Wompi CloudFront bloquea http://localhost (WAF). Solo enviar si es https (prod) — en dev se omite y widget abre sin redirect
          const isHttpsRedirect = !!cfg.redirectUrl && cfg.redirectUrl.startsWith("https://");
          const options: WompiCheckoutOptions = {
            currency: cfg.currency,
            amountInCents: cfg.amountInCents,
            reference: cfg.reference,
            publicKey: cfg.publicKey,
            signature: {
              integrity: cfg.integritySignature,
            },
            ...(isHttpsRedirect ? { redirectUrl: cfg.redirectUrl } : {}),
            ...(cfg.customer ? { customerData: cfg.customer } : {}),
          };
          if (cfg.redirectUrl && !isHttpsRedirect) {
            console.warn("[WompiWidget] redirectUrl omitido por no ser https (evita 403 WAF):", cfg.redirectUrl);
          }

          console.debug("[WompiWidget] WidgetCheckout options:", options);

          const checkout = new Constructor(options);
          checkout.open((result: WompiResult) => {
            // No redirigir manualmente ni considerar el pago confirmado aquí.
            // La confirmación definitiva depende del webhook de Wompi.
            // Solo exponer la transacción al consumidor.
            const transaction = result?.transaction;
            if (transaction) {
              console.debug("[WompiWidget] Transacción recibida:", transaction);
              transactionRef.current?.(transaction);
            }
          });

          // Detector de iframe con error (currency/amount/reference no proveído por allowlist/firma)
          setTimeout(() => {
            try {
              const iframe = document.querySelector<HTMLIFrameElement>(
                'iframe[src*="checkout.wompi.co/p/"]',
              );
              if (!iframe) return;
              // No podemos leer cross-origin, pero detectamos si el iframe sigue en estado de error por tamaño/mensaje
              // Si el widget no reemplazó el modal en 2s, probablemente falló la validación
              const hasModal = !!document.querySelector(
                '[id*="widget-checkout"], [class*="widget-checkout"]',
              );
              if (!hasModal) {
                console.warn(
                  "[WompiWidget] Posible fallo allowlist: iframe sin modal. Verifica redirectUrl en Dashboard Wompi.",
                );
              }
            } catch {
              // ignore cross-origin
            }
          }, 2500);
        })
        .catch((err: unknown) => {
          console.error("[WompiWidget] Error al abrir el widget:", err);
          const message =
            typeof err === "string"
              ? err
              : err instanceof Error
                ? err.message
                : "No se pudo abrir el widget de Wompi";
          onOpenError?.(message);
        });
    }, [onOpenError]);

    useImperativeHandle(ref, () => ({ open }), [open]);

    useEffect(() => {
      // Precarga segura: una sola vez, sin bloquear render
      loadScript().catch(() => {
        /* el script se reintentará al abrir el widget */
      });
    }, []);

    return null;
  },
);

export default WompiWidget;
