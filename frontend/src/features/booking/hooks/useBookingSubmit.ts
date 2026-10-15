import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import type { BookingRoomType, OccupantConfig } from "../types";
import { useAuth } from "@/app/auth/AuthContext";
import { usePublicLayout } from "@/layouts/public/PublicLayoutContext";
import { getCustomerReservationController } from "@/api/generated/customer-reservation-controller/customer-reservation-controller";
import type { CustomerReservationRequest } from "@/api/generated/openAPIDefinition.schemas";
import { extractErrorMessage } from "@/lib/extract-error";

interface Props {
  tipos: BookingRoomType[];
  selections: Record<number, number>;
  occupantConfig: OccupantConfig;
  checkIn: string;
  checkOut: string;
  checkInISO: string;
  checkOutISO: string;
  nights: number;
  totalPrice: number;
  totalRooms: number;
  totalGuests: number;
  extraPersonCharge: number;
}

export function useBookingSubmit({
  tipos,
  selections,
  occupantConfig,
  checkIn,
  checkOut,
  checkInISO,
  checkOutISO,
  nights,
  totalPrice,
  totalRooms,
  totalGuests,
  extraPersonCharge,
}: Props) {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { openLogin, openCompleteProfile } = usePublicLayout();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleBook = useCallback(async () => {
    if (!user) {
      toast.info("Debes iniciar sesión para reservar");
      openLogin();
      return;
    }

    if (!user.clienteId) {
      toast.error("Debes completar tu perfil antes de reservar");
      openCompleteProfile();
      return;
    }

    const selectedTipos = tipos
      .filter((t) => (selections[t.id] ?? 0) > 0)
      .flatMap((t) => {
        const config = occupantConfig[t.id];
        if (config && config.length === (selections[t.id] ?? 0)) {
          return config.map((ocupantes) => ({
            tipoHabitacionId: t.id,
            cantidad: 1,
            ocupantes,
          }));
        }
        return [{
          tipoHabitacionId: t.id,
          cantidad: selections[t.id] ?? 1,
          ocupantes: t.capacidadBase,
        }];
      });

    if (selectedTipos.length === 0) {
      toast.error("Selecciona al menos una habitación");
      return;
    }

    setIsSubmitting(true);
    try {
      const request: CustomerReservationRequest = {
        fechaCheckIn: checkInISO,
        fechaCheckOut: checkOutISO,
        cantidadHuespedes: totalGuests,
        tiposHabitacion: selectedTipos,
        titularId: user.clienteId,
      };

      const response = await getCustomerReservationController().crearReservaPublica(request);

      if (!response.id || !response.codigoReserva) {
        toast.error("No se pudo generar la reserva");
        return;
      }

      const paymentData = {
        reservaId: response.id,
        codigoReserva: response.codigoReserva,
        checkIn,
        checkOut,
        nights,
        selections: tipos
          .filter((t) => (selections[t.id] ?? 0) > 0)
          .map((t) => ({ tipo: t, quantity: selections[t.id] ?? 0 })),
        totalRooms,
        totalGuests,
        totalPrice: response.total ?? totalPrice,
        extraPersonCharge,
      };

      navigate("/pago", { state: paymentData });
    } catch (error) {
      toast.error(
        extractErrorMessage(error, "No se pudo crear la reserva. Intenta de nuevo."),
      );
    } finally {
      setIsSubmitting(false);
    }
  }, [
    user,
    tipos,
    selections,
    occupantConfig,
    checkIn,
    checkOut,
    checkInISO,
    checkOutISO,
    nights,
    totalPrice,
    totalRooms,
    totalGuests,
    extraPersonCharge,
    navigate,
    openLogin,
    openCompleteProfile,
  ]);

  return {
    handleBook,
    isSubmitting,
  };
}
