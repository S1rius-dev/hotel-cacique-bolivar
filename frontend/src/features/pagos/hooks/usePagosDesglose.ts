import { useQuery } from "@tanstack/react-query";
import { getPagoDesgloseController } from "@/api/generated/pago-desglose-controller/pago-desglose-controller";

interface Params {
  reservaId: number | undefined;
  monto: number;
}

/**
 * Muestra cómo se repartiría un pago entre las cajas de recepción y tienda,
 * sin registrar nada. El backend solo persiste desglose para pagos en efectivo,
 * así que el componente que lo consume decide cuándo pedirlo.
 *
 * El monto se debouncea en el componente porque cambia en cada tecla.
 */
export const usePreviewDesglosePago = ({ reservaId, monto }: Params) =>
  useQuery({
    queryKey: ["pago-desglose-preview", reservaId, monto],
    queryFn: () =>
      getPagoDesgloseController().previewDesglosePago(reservaId!, { monto }),
    enabled: !!reservaId && monto > 0,
    staleTime: 30_000,
  });
