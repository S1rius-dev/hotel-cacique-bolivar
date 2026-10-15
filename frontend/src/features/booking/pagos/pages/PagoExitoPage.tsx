import { useNavigate, useSearchParams } from "react-router-dom";
import { SEOHead } from "@/components/seo";
import PublicLayout from "@/layouts/public/PublicLayout";
import PaymentResultCard from "../components/PaymentResultCard";
import { usePaymentGatewayConfig } from "@/features/payments/hooks/usePaymentGatewayConfig";

export default function PagoExitoPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const { gateway } = usePaymentGatewayConfig();
  const codigoReserva = params.get("codigoReserva") ?? params.get("reference") ?? params.get("id");

  const fecha = new Date().toLocaleDateString("es-CO", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
  const hora = new Date().toLocaleTimeString("es-CO", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
  const fechaDisplay = `${fecha} · ${hora}`;

  return (
    <>
      <SEOHead
        title="Pago exitoso"
        description="Tu pago fue procesado correctamente"
        url="/pago/exito"
        noindex
      />
      <PublicLayout>
        <div className="mx-auto max-w-xl px-4 py-8 sm:py-10">
          <PaymentResultCard
            variant="success"
            codigoReserva={codigoReserva}
            fecha={fechaDisplay}
            onVerDetalles={() => navigate("/mis-reservas")}
            onDescargar={() => window.print()}
            onVolverInicio={() => navigate("/")}
            gatewayDisplayName={gateway?.displayName ?? "Bold"}
            gatewayName={gateway?.gateway ?? "BOLD"}
            gatewayLogoUrl={gateway?.logoUrl ?? null}
            securePaymentText={gateway?.securePaymentText ?? "Pago seguro con Bold"}
          />
        </div>
      </PublicLayout>
    </>
  );
}
