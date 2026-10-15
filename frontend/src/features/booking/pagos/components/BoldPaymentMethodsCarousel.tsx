import { useEffect, useRef } from "react";

const BOLD_UI_KIT_SRC = "https://bold.co/library/ui-kit.js";
const CAROUSEL_TARGET_ID = "bold-payment-methods-carousel";

declare global {
  interface Window {
    boldUIKKitAlreadyProcessed?: boolean;
  }
}

interface Props {
  className?: string;
}

export default function BoldPaymentMethodsCarousel({ className }: Props) {
  const targetRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const target = targetRef.current;
    if (!target) return;

    const render = () => {
      if (target.querySelector("[id^='bold-ui-kit-']")) return;
      document.querySelector("script[data-bold-ui-kit='carousel']")?.remove();
      delete window.boldUIKKitAlreadyProcessed;

      const params = new URLSearchParams({ type: "slider", target: CAROUSEL_TARGET_ID });
      const script = document.createElement("script");
      script.src = `${BOLD_UI_KIT_SRC}?${params.toString()}`;
      script.async = true;
      script.dataset.boldUiKit = "carousel";
      script.onerror = () =>
        console.error("[BoldPaymentMethodsCarousel] No se pudo cargar el carrusel de métodos de pago de Bold");
      document.body.appendChild(script);
    };

    if (document.readyState === "complete") {
      render();
    } else {
      window.addEventListener("load", render, { once: true });
    }

    return () => {
      window.removeEventListener("load", render);
      document.querySelector("script[data-bold-ui-kit='carousel']")?.remove();
    };
  }, []);

  return <div id={CAROUSEL_TARGET_ID} ref={targetRef} className={className} />;
}
