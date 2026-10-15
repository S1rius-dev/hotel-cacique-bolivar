import { forwardRef, useImperativeHandle, useRef } from "react";
import WompiWidget, { type WompiWidgetHandle, type WompiWidgetConfig } from "./WompiWidget";
import BoldWidget, { type BoldWidgetHandle } from "./BoldWidget";

export type PaymentWidgetConfig = WompiWidgetConfig;

export type PaymentWidgetHandle = WompiWidgetHandle | BoldWidgetHandle;

export interface PaymentWidgetProps extends PaymentWidgetConfig {
  gateway?: string;
  onTransaction?: (transaction: unknown) => void;
  onOpenError?: (message: string) => void;
}

/**
 * Widget genérico de pago.
 * Delega a WompiWidget o BoldWidget según la pasarela activa.
 *
 * Bold usa Embedded Checkout con botón data-bold-button:
 * <script data-bold-button data-api-key="LLAVE_DE_IDENTIDAD" data-amount data-currency
 *         data-order-id data-description data-redirection-url data-integrity-signature data-render-mode="embedded"></script>
 */
export const PaymentWidget = forwardRef<PaymentWidgetHandle, PaymentWidgetProps>(
  function PaymentWidget({ gateway, onTransaction, onOpenError, ...config }, ref) {
    const wompiRef = useRef<WompiWidgetHandle>(null);
    const boldRef = useRef<BoldWidgetHandle>(null);

    useImperativeHandle(ref, () => ({
      open: () => {
        if (!gateway) return;
        const normalized = gateway.toUpperCase();
        if (normalized === "BOLD") {
          boldRef.current?.open();
          return;
        }
        if (normalized === "WOMPI") wompiRef.current?.open();
      },
    }));

    if (!gateway) return null;
    const normalized = gateway.toUpperCase();
    if (normalized === "BOLD") {
      return (
        <BoldWidget
          ref={boldRef}
          apiKey={config.publicKey}
          amountInCents={config.amountInCents}
          currency={config.currency}
          reference={config.reference}
          integrityHash={config.integritySignature}
          redirectUrl={config.redirectUrl}
          description="Pago a traves de Embedded Checkout"
          renderMode="embedded"
          onOpenError={onOpenError}
        />
      );
    }

    return (
      <WompiWidget
        ref={wompiRef}
        publicKey={config.publicKey}
        amountInCents={config.amountInCents}
        currency={config.currency}
        reference={config.reference}
        integritySignature={config.integritySignature}
        redirectUrl={config.redirectUrl}
        customer={config.customer}
        onTransaction={onTransaction as never}
        onOpenError={onOpenError}
      />
    );
  },
);

export default PaymentWidget;
