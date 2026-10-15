import { useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import { getPagoController } from "@/api/generated/pago-controller/pago-controller";

const pagoController = getPagoController();

export default function ReservaPagosRedirect() {
  const { id } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    if (!id) {
      navigate("/dashboard/pagos", { replace: true });
      return;
    }
    const numId = Number(id);
    if (Number.isNaN(numId)) {
      navigate("/dashboard/reservas", { replace: true });
      return;
    }

    // Intentar resolver como pagoId -> reservaId
    // Si falla, asumir que id ya es reservaId y redirigir a #pagos
    pagoController
      .obtenerPago(numId)
      .then((pago) => {
        const reservaId = (pago as unknown as { reservaId?: number }).reservaId;
        if (reservaId) {
          navigate(`/dashboard/reservas/${reservaId}?focus=pagos#pagos`, { replace: true });
        } else {
          navigate(`/dashboard/reservas/${id}?focus=pagos#pagos`, { replace: true });
        }
      })
      .catch(() => {
        // No es un pagoId válido o no se pudo resolver -> tratar como reservaId
        navigate(`/dashboard/reservas/${id}?focus=pagos#pagos`, { replace: true });
      });
  }, [id, navigate]);

  return (
    <BackHeaderLayout title="Reservas" subtitle="Redirigiendo a pagos">
      <DetailSkeleton cards={1} />
    </BackHeaderLayout>
  );
}
