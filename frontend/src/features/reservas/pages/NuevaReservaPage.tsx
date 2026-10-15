"use client";

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  CheckCircle2,
  ArrowRight,
  Wallet,
  MoonIcon,
  CreditCard,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import StepIndicator from "@/features/reservas/components/StepIndicator";
import Step1Reserva from "@/features/reservas/pages/Step1Reserva";
import { useCrearReserva } from "@/features/reservas/hooks/useCrearReserva";
import { RegistrarPagoModal } from "@/features/pagos";
import { toast } from "sonner";
import { FORMAT_CURRENCY } from "@/features/pagos";
import type {
  ClienteResponse,
  ReservaRequest,
  ReservaResponse,
} from "@/api/generated/openAPIDefinition.schemas";
import type {
  SelectedRoom,
  HabitacionHuespedForm,
} from "@/features/reservas/types/reservas.types";
import { Step2Reserva } from "./Step2Reserva";
import { formatDateShort } from "@/utils/date";

const NuevaReservaPage: React.FC = () => {
  const navigate = useNavigate();
  const { mutate, isPending } = useCrearReserva();

  const [currentStep, setCurrentStep] = useState(1);
  const [filtros, setFiltros] = useState({ checkIn: "", checkOut: "" });
  const [selectedRooms, setSelectedRooms] = useState<SelectedRoom[]>([]);
  const [titular, setTitular] = useState<ClienteResponse | null>(null);
  const [habitacionesConHuespedes, setHabitacionesConHuespedes] = useState<
    HabitacionHuespedForm[]
  >([]);
  const [createdReserva, setCreatedReserva] =
    useState<ReservaResponse | null>(null);
  const [pagoModalOpen, setPagoModalOpen] = useState(false);

  const handleNextStep = () => {
    const habitaciones: HabitacionHuespedForm[] = selectedRooms.map((room) => ({
      habitacion: {
        numero: room.numero,
        tipo: room.tipo,
        capacidad: room.capacidad,
        id: room.id,
        precio: room.precio,
        adultos: room.adultos,
        ninos: room.ninos,
        precioIncluyeIva: room.precioIncluyeIva,
      },
      huespedes: [],
    }));
    setHabitacionesConHuespedes(habitaciones);
    setCurrentStep(currentStep + 1);
  };

  const handlePrevStep = () => {
    if (currentStep > 1) setCurrentStep(currentStep - 1);
  };

  const calculateNights = () => {
    if (filtros.checkIn && filtros.checkOut) {
      const start = new Date(filtros.checkIn);
      const end = new Date(filtros.checkOut);
      const diffTime = Math.abs(end.getTime() - start.getTime());
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) || 1;
    }
    return 1;
  };

  const handleReset = () => {
    setCreatedReserva(null);
    setCurrentStep(1);
    setFiltros({ checkIn: "", checkOut: "" });
    setSelectedRooms([]);
    setTitular(null);
    setHabitacionesConHuespedes([]);
  };

  const noches = calculateNights();

  const handleConfirmarReserva = () => {
    if (!titular) {
      toast.error("Selecciona un titular para la reserva");
      return;
    }

    if (habitacionesConHuespedes.length === 0) {
      toast.error("No hay habitaciones seleccionadas");
      return;
    }

    const detallesValidos = habitacionesConHuespedes.filter(
      (h) => h.habitacion.id,
    );
    if (detallesValidos.length === 0) {
      toast.error("Ninguna habitación tiene un ID válido");
      return;
    }

    const detalles = detallesValidos.map((h) => ({
      habitacionId: h.habitacion.id!,
      precioPorNoche: h.habitacion.precio || 0,
      cantidadOcupantes:
        (h.habitacion.adultos || 0) + (h.habitacion.ninos || 0),
      observaciones: "",
      // ausente = modo sin IVA (default del backend)
      precioIncluyeIva: h.habitacion.precioIncluyeIva,
    }));

    const reservaData: ReservaRequest = {
      titularReservaId: titular.id!,
      fechaCheckIn: filtros.checkIn,
      fechaCheckOut: filtros.checkOut,
      origen: "RECEPCION",
      observaciones: "",
      detalles,
    };

    mutate(reservaData, {
      onSuccess: (data) => {
        setCreatedReserva(data);
      },
    });
  };

  const renderSuccess = () => {
    if (!createdReserva) return null;

    const saldoPendiente = createdReserva.saldoPendiente ?? 0;
    const total = createdReserva.total ?? 0;

    return (
      <div className="max-w-2xl mx-auto mt-8 pb-8">
        <div className="border-2 border-zinc-200 dark:border-zinc-700 rounded-2xl bg-white dark:bg-zinc-900 shadow-lg overflow-hidden">
          {/* Header */}
          <div className="text-center pt-8 pb-6 px-6 bg-gradient-to-b from-zinc-50 to-white dark:from-zinc-800/50 dark:to-zinc-900">
            <div className="inline-flex size-14 rounded-full bg-emerald-100 dark:bg-emerald-900/30 items-center justify-center ring-[6px] ring-emerald-100/60 dark:ring-emerald-900/20 mb-4">
              <CheckCircle2 className="size-7 text-emerald-600" />
            </div>
            <h2 className="text-lg font-bold text-zinc-900 dark:text-white">
              Reserva creada exitosamente
            </h2>
            <p className="text-sm text-muted-foreground mt-1">
              Código:{" "}
              <span className="font-mono font-semibold text-zinc-900 dark:text-zinc-100 tracking-wider">
                {createdReserva.codigoReserva}
              </span>
            </p>
          </div>

          {/* Dashed separator */}
          <div className="border-t-2 border-dashed border-zinc-200 dark:border-zinc-700" />

          {/* Body — 3-column dates + nights */}
          <div className="px-6 py-5">
            <div className="grid grid-cols-3 gap-4">
              <div className="p-3 rounded-xl bg-zinc-50 dark:bg-zinc-800/50">
                <p className="text-[10px] uppercase tracking-widest text-muted-foreground mb-0.5">
                  Check-in
                </p>
                <p className="font-semibold text-sm text-zinc-900 dark:text-white">
                  {formatDateShort(createdReserva.fechaCheckIn ?? "")}
                </p>
              </div>
              <div className="p-3 rounded-xl bg-zinc-50 dark:bg-zinc-800/50 text-center">
                <p className="text-[10px] uppercase tracking-widest text-muted-foreground mb-0.5">
                  Check-out
                </p>
                <p className="font-semibold text-sm text-zinc-900 dark:text-white">
                  {formatDateShort(createdReserva.fechaCheckOut ?? "")}
                </p>
              </div>
              <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/20 text-right">
                <p className="text-[10px] uppercase tracking-widest text-muted-foreground mb-0.5 flex items-center justify-end gap-1">
                  <MoonIcon className="size-3 text-rose-500" />
                  Noches
                </p>
                <p className="text-lg font-bold text-rose-700 dark:text-rose-400">
                  {createdReserva.noches ?? noches}
                </p>
              </div>
            </div>
          </div>

          {/* Dashed separator */}
          <div className="border-t-2 border-dashed border-zinc-200 dark:border-zinc-700" />

          {/* Total + Payment side by side */}
          <div className="grid grid-cols-1 sm:grid-cols-2 divide-y sm:divide-y-0 sm:divide-x-2 divide-dashed divide-zinc-200 dark:divide-zinc-700">
            <div className="px-6 py-5 flex flex-col justify-center">
              <span className="text-xs uppercase tracking-widest text-muted-foreground mb-1">
                Total
              </span>
              <span className="text-2xl font-black text-rose-700 dark:text-rose-400">
                {FORMAT_CURRENCY.format(total)}
              </span>
            </div>

            {saldoPendiente > 0 ? (
              <div className="px-6 py-5 space-y-3">
                <div className="flex items-start gap-3">
                  <div className="size-9 rounded-lg bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center shrink-0">
                    <CreditCard className="size-4.5 text-amber-600" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-zinc-900 dark:text-white">
                      Pago pendiente
                    </p>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      Puedes pagar ahora o después desde la reserva.
                    </p>
                  </div>
                </div>
                <div className="flex items-center justify-between rounded-lg bg-amber-50 dark:bg-amber-950/20 border border-amber-200/60 dark:border-amber-800/30 px-4 py-2.5">
                  <span className="text-sm text-muted-foreground">Saldo</span>
                  <span className="text-sm font-bold text-amber-700 dark:text-amber-400">
                    {FORMAT_CURRENCY.format(saldoPendiente)}
                  </span>
                </div>
                <Button
                  variant="default"
                  onClick={() => setPagoModalOpen(true)}
                  className="w-full gap-2 bg-amber-700 hover:bg-amber-800 text-white shadow-sm"
                >
                  <Wallet className="size-4" />
                  Pagar ahora
                </Button>
              </div>
            ) : (
              <div className="px-6 py-5 flex items-center gap-3">
                <div className="size-9 rounded-lg bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center shrink-0">
                  <CheckCircle2 className="size-4.5 text-emerald-600" />
                </div>
                <p className="text-sm font-semibold text-emerald-700 dark:text-emerald-400">
                  Totalmente pagado
                </p>
              </div>
            )}
          </div>

          {/* Footer actions */}
          <div className="border-t-2 border-dashed border-zinc-200 dark:border-zinc-700" />
          <div className="px-6 py-4 flex gap-3">
            <Button
              variant="outline"
              onClick={handleReset}
              className="flex-1 border-zinc-300 dark:border-zinc-700 hover:bg-zinc-100 dark:hover:bg-zinc-800"
            >
              Nueva reserva
            </Button>
            <Button
              onClick={() => navigate("/dashboard/reservas")}
              className="flex-1 gap-2 bg-rose-800 hover:bg-rose-900 text-white shadow-sm"
            >
              Ir a reservas
              <ArrowRight className="size-4" />
            </Button>
          </div>
        </div>
      </div>
    );
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <Step1Reserva
            filtros={filtros}
            selectedRooms={selectedRooms}
            titular={titular}
            onFiltrosChange={setFiltros}
            onSelectedRoomsChange={setSelectedRooms}
            onTitularChange={setTitular}
            onNextStep={handleNextStep}
          />
        );

      case 2:
        return (
          <Step2Reserva
            checkIn={filtros.checkIn}
            checkOut={filtros.checkOut}
            noches={noches}
            titular={titular}
            habitacionesConHuespedes={habitacionesConHuespedes}
            selectedRooms={selectedRooms.map((r) => ({
              numero: r.numero,
              tipo: r.tipo,
              precio: r.precio,
              adultos: r.adultos,
              ninos: r.ninos,
            }))}
            onConfirm={handleConfirmarReserva}
            onPrevStep={handlePrevStep}
            isSubmitting={isPending}
          />
        );
      default:
        return null;
    }
  };

  if (createdReserva) {
    return (
      <>
        {renderSuccess()}
        {createdReserva.id && (
          <RegistrarPagoModal
            open={pagoModalOpen}
            reservaId={createdReserva.id}
            total={createdReserva.total ?? 0}
            totalPagado={createdReserva.totalPagado ?? 0}
            saldoPendiente={createdReserva.saldoPendiente ?? 0}
            onClose={() => setPagoModalOpen(false)}
          />
        )}
      </>
    );
  }

  return (
    <BackHeaderLayout
      title="Nueva Reserva"
      subtitle="Gestión interna de reservas"
      contentClassName="flex flex-col pb-10">
      <div className="mb-4 shrink-0">
        <StepIndicator currentStep={currentStep} />
      </div>

      <div className="flex-1 min-h-0 pb-2">{renderStep()}</div>
    </BackHeaderLayout>
  );
};

export default NuevaReservaPage;
