import { forwardRef, useCallback, useEffect, useImperativeHandle, useRef } from "react";

export interface BoldWidgetConfig {
  apiKey: string; // LLAVE_DE_IDENTIDAD (BOLD_IDENTITY_KEY)
  amountInCents: number;
  currency: string;
  reference: string; // orderId / Identificador
  integrityHash: string; // SHA256({Identificador}{Monto}{Divisa}{LlaveSecreta})
  redirectUrl?: string;
  description?: string;
  renderMode?: "embedded" | "redirect";
}

export interface BoldWidgetHandle {
  open: () => void;
}

export type BoldWidgetProps = BoldWidgetConfig & {
  onTransaction?: (data: unknown) => void;
  onOpenError?: (message: string) => void;
};

const BOLD_SCRIPT_SRC = "https://checkout.bold.co/library/boldPaymentButton.js";

let boldScriptPromise: Promise<void> | null = null;

function loadBoldScript(): Promise<void> {
  if (boldScriptPromise) return boldScriptPromise;
  const existing = document.querySelector<HTMLScriptElement>(`script[src="${BOLD_SCRIPT_SRC}"]`);
  boldScriptPromise = new Promise((resolve, reject) => {
    const handleResolve = () => resolve();
    const handleReject = () => {
      boldScriptPromise = null;
      reject(new Error("No se pudo cargar el script de Bold"));
    };
    if (existing) {
      if ((existing as unknown as { dataset?: unknown }) != null) {
        // script ya cargado, verificar que esté listo
        if (document.querySelector("[data-bold-button]")) {
          resolve();
          return;
        }
      }
      existing.addEventListener("load", handleResolve, { once: true });
      existing.addEventListener("error", handleReject, { once: true });
      if (existing.getAttribute("data-loaded") === "true") resolve();
      return;
    }
    const script = document.createElement("script");
    script.src = BOLD_SCRIPT_SRC;
    script.async = true;
    script.onload = () => {
      script.setAttribute("data-loaded", "true");
      handleResolve();
    };
    script.onerror = handleReject;
    document.body.appendChild(script);
  });
  boldScriptPromise.catch(() => {
    boldScriptPromise = null;
  });
  return boldScriptPromise;
}

/**
 * Bold Embedded Checkout Widget.
 * Renderiza el botón data-bold-button según docs Bold.
 * El hash de integridad SHA-256({Identificador}{Monto}{Divisa}{LlaveSecreta}) se genera en backend
 * y se pasa como data-integrity-signature / integrityHash para validar que el monto no fue manipulado.
 */
export const BoldWidget = forwardRef<BoldWidgetHandle, BoldWidgetProps>(
  function BoldWidget(
    {
      apiKey,
      amountInCents,
      currency,
      reference,
      integrityHash,
      redirectUrl,
      description = "Pago a traves de Embedded Checkout",
      renderMode = "embedded",
      onOpenError,
    },
    ref,
  ) {
    const containerRef = useRef<HTMLDivElement>(null);
    const configRef = useRef<BoldWidgetConfig | null>(null);
    configRef.current = { apiKey, amountInCents, currency, reference, integrityHash, redirectUrl, description, renderMode };

    const renderButton = useCallback(() => {
      const cfg = configRef.current;
      if (!cfg) return;
      const missing: string[] = [];
      if (!cfg.apiKey) missing.push("apiKey (LLAVE_DE_IDENTIDAD)");
      if (!cfg.amountInCents) missing.push("amount");
      if (!cfg.currency) missing.push("currency");
      if (!cfg.reference) missing.push("reference/orderId");
      if (!cfg.integrityHash) missing.push("integrityHash");
      if (missing.length > 0) {
        const msg = `Faltan parámetros Bold: ${missing.join(", ")}.`;
        console.error("[BoldWidget] " + msg, cfg);
        onOpenError?.(msg + " Verifica que el checkout retornara identityKey e integrityHash.");
        return;
      }
      console.debug("[BoldWidget] Renderizando botón con:", {
        apiKey: cfg.apiKey?.substring(0, 8) + "…",
        amount: cfg.amountInCents,
        currency: cfg.currency,
        reference: cfg.reference,
        integrityHash: cfg.integrityHash?.substring(0, 8) + "…",
        redirectUrl: cfg.redirectUrl,
        renderMode: cfg.renderMode,
      });

      if (!containerRef.current) return;
      // Limpiar render previo
      containerRef.current.innerHTML = "";

      // Bold requiere que el script con data-bold-button TENGA src al mismo tiempo
      // https://checkout.bold.co/library/boldPaymentButton.js + data attributes
      const script = document.createElement("script");
      script.src = BOLD_SCRIPT_SRC;
      script.async = true;
      script.setAttribute("data-bold-button", "");
      script.setAttribute("data-api-key", cfg.apiKey);
      script.setAttribute("data-amount", String(cfg.amountInCents));
      script.setAttribute("data-currency", cfg.currency);
      script.setAttribute("data-order-id", cfg.reference);
      script.setAttribute("data-reference", cfg.reference);
      script.setAttribute("data-description", cfg.description ?? "Pago a traves de Embedded Checkout");
      // data-redirection-url solo si es https:// válida (Bold rechaza http://localhost)
      // Para pruebas sin redirección, omitir el atributo (embedded no lo requiere)
      if (cfg.redirectUrl && cfg.redirectUrl.startsWith("https://")) {
        script.setAttribute("data-redirection-url", cfg.redirectUrl);
      } else if (cfg.redirectUrl && cfg.redirectUrl.startsWith("https://employment-shaft-humans-whatever.trycloudflare.com")) {
        script.setAttribute("data-redirection-url", cfg.redirectUrl);
      }
      // Si no hay redirectUrl https, NO setear data-redirection-url (evita BTN-001 por http://localhost)
      script.setAttribute("data-render-mode", cfg.renderMode ?? "embedded");
      script.setAttribute("data-integrity-signature", cfg.integrityHash);
      script.setAttribute("data-integrity-hash", cfg.integrityHash);

      script.onload = () => {
        console.debug("[BoldWidget] Script Bold cargado y botón inyectado");
        // Verificar que Bold haya renderizado botón (reemplaza script por iframe/botón)
        setTimeout(() => {
          const hasButton = containerRef.current?.querySelector("button, iframe, [class*='bold']");
          if (!hasButton) {
            console.warn("[BoldWidget] Script cargado pero no se detectó botón Bold. Verifica BOLD_IDENTITY_KEY y que el dominio esté allowlisted en Bold dashboard.");
          }
        }, 1500);
      };
      script.onerror = () => {
        console.error("[BoldWidget] Error cargando Bold script:", BOLD_SCRIPT_SRC);
        onOpenError?.("No se pudo cargar el script de Bold. Verifica conexión.");
      };

      containerRef.current.appendChild(script);
    }, [onOpenError]);

    useImperativeHandle(ref, () => ({ open: renderButton }), [renderButton]);

    useEffect(() => {
      // Precarga script en montaje
      loadBoldScript().catch(() => {});
    }, []);

    useEffect(() => {
      // Auto-render cuando props estén listas y el contenedor exista
      if (apiKey && reference && integrityHash) {
        const t = setTimeout(renderButton, 300);
        return () => clearTimeout(t);
      }
    }, [apiKey, reference, integrityHash, renderButton]);

    return <div ref={containerRef} className="w-full flex justify-center py-2" />;
  },
);

export default BoldWidget;
