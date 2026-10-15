import { useParams, useNavigate } from "react-router-dom";
import { useState } from "react";
import { motion } from "motion/react";
import { useResetOnKey } from "@/lib/useResetOnKey";
import {
  ArrowLeft,
  Save,
  Loader2,
  Plus,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { differenceInCalendarDays, addDays, format } from "date-fns";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";
import {
  useReservaDetalle,
  useActualizarReserva,
  useActualizarDetalle,
  useAgregarHuespedes,
  useRemoverHuesped,
  useRemoverDetalle,
  useAgregarDetalle,
} from "../hooks/useReservaDetalle";
import type {
  ReservaUpdateRequest,
  DetalleReservaResponse,
  DetalleReservaRequest,
} from "@/api/generated/openAPIDefinition.schemas";
import type {
  ReservaUpdateRequestOrigen,

} from "@/api/generated/openAPIDefinition.schemas";
import { toast } from "sonner";
import EditarInformacionGeneral from "../components/EditarInformacionGeneral";
import EditarHabitacionCard from "../components/EditarHabitacionCard";
import TitularEditField from "../components/TitularEditField";
import AddGuestModal from "../components/AddGuestModal";
import AgregarDetalleModal from "../components/AgregarDetalleModal";

const ESTADO_BADGES: Record<string, { label: string; className: string }> = {
  PENDIENTE: { label: "Pendiente", className: "bg-rose-100 text-rose-800 border-rose-200 dark:bg-rose-900/30 dark:text-rose-400 dark:border-rose-800" },
  CONFIRMADA: { label: "Confirmada", className: "bg-emerald-100 text-emerald-800 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-400 dark:border-emerald-800" },
  EN_CURSO: { label: "En curso", className: "bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-800" },
  FINALIZADA: { label: "Finalizada", className: "bg-stone-100 text-stone-700 border-stone-200 dark:bg-stone-900/30 dark:text-stone-400 dark:border-stone-800" },
  CANCELADA: { label: "Cancelada", className: "bg-red-100 text-red-800 border-red-200 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800" },
  NO_SHOW: { label: "No Llegó", className: "bg-stone-100 text-stone-700 border-stone-200 dark:bg-stone-900/30 dark:text-stone-400 dark:border-stone-800" },
};

const ESTADO_HEADER_BG: Record<string, string> = {
  PENDIENTE: "bg-rose-50/80 border-l-rose-400 dark:bg-rose-950/20 dark:border-l-rose-600",
  CONFIRMADA: "bg-emerald-50/80 border-l-emerald-400 dark:bg-emerald-950/20 dark:border-l-emerald-600",
  EN_CURSO: "bg-blue-50/80 border-l-blue-400 dark:bg-blue-950/20 dark:border-l-blue-600",
  FINALIZADA: "bg-stone-50 border-l-stone-300 dark:bg-stone-900/30 dark:border-l-stone-600",
  CANCELADA: "bg-red-50/80 border-l-red-400 dark:bg-red-950/20 dark:border-l-red-600",
  NO_SHOW: "bg-stone-50 border-l-stone-300 dark:bg-stone-900/30 dark:border-l-stone-600",
};

function EditarReservaPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const numId = id ? Number(id) : undefined;

  const { data: reserva, isLoading } = useReservaDetalle(numId);
  const actualizar = useActualizarReserva();
  const actualizarDetalle = useActualizarDetalle();
  const agregarHuespedes = useAgregarHuespedes();
  const removerHuesped = useRemoverHuesped();
  const removerDetalle = useRemoverDetalle();
  const agregarDetalle = useAgregarDetalle();

  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [origen, setOrigen] = useState("RECEPCION");
  const [observaciones, setObservaciones] = useState("");
  const [titularId, setTitularId] = useState<number | undefined>(undefined);
  const [detallesEdit, setDetallesEdit] = useState<
    Map<
      number,
      {
        precioPorNoche?: number;
        cantidadOcupantes?: number;
        observaciones?: string;
        useCustomNoches?: boolean;
        customNoches?: number;
      }
    >
  >(new Map());

  const [saving, setSaving] = useState(false);
  const [checkInOpen, setCheckInOpen] = useState(false);
  const [checkOutOpen, setCheckOutOpen] = useState(false);

  const [addRoomOpen, setAddRoomOpen] = useState(false);
  const [addGuestDetalle, setAddGuestDetalle] =
    useState<DetalleReservaResponse | null>(null);
  const [removeGuestTarget, setRemoveGuestTarget] = useState<{
    detalleId: number;
    detalleHuespedId: number;
    guestName: string;
  } | null>(null);
  const [removeDetalleTarget, setRemoveDetalleTarget] = useState<{
    detalleId: number;
    roomNumber: string;
  } | null>(null);

  const puedeGestionarHuespedes =
    reserva?.estado === "PENDIENTE" ||
    reserva?.estado === "CONFIRMADA" ||
    reserva?.estado === "EN_CURSO";
  const puedeRemoverDetalle =
    (reserva?.estado === "PENDIENTE" || reserva?.estado === "CONFIRMADA") &&
    (reserva?.detalles?.length ?? 0) > 1;

  // El formulario arranca desde la reserva cargada. Se resincroniza en render
  // cuando llega una version distinta, en vez de en un efecto que dejaria el
  // formulario mostrando los valores viejos durante un render.
  useResetOnKey(JSON.stringify(reserva ?? null), () => {
    if (!reserva) return;
    setCheckIn(reserva.fechaCheckIn ?? "");
    setCheckOut(reserva.fechaCheckOut ?? "");
    setOrigen(reserva.origen ?? "RECEPCION");
    setObservaciones(reserva.observaciones ?? "");
    setTitularId(reserva.titular?.id);
    setDetallesEdit(
      new Map(
        (reserva.detalles ?? []).map((d) => [
          d.id!,
          {
            precioPorNoche: d.precioPorNoche,
            cantidadOcupantes: d.cantidadOcupantes,
            observaciones: d.observaciones,
          },
        ]),
      ),
    );
  });

  const checkInDate = checkIn ? new Date(checkIn + "T00:00:00") : undefined;
  const checkOutDate = checkOut ? new Date(checkOut + "T00:00:00") : undefined;
  const noches =
    checkInDate && checkOutDate
      ? differenceInCalendarDays(checkOutDate, checkInDate)
      : 0;

  const hasChanges =
    checkIn !== (reserva?.fechaCheckIn ?? "") ||
    checkOut !== (reserva?.fechaCheckOut ?? "") ||
    origen !== (reserva?.origen ?? "RECEPCION") ||
    observaciones !== (reserva?.observaciones ?? "") ||
    titularId !== reserva?.titular?.id ||
    [...detallesEdit.entries()].some(([id, edit]) => {
      const orig = reserva?.detalles?.find((d) => d.id === id);
      if (!orig) return true;
      return (
        edit.cantidadOcupantes !== orig.cantidadOcupantes ||
        edit.precioPorNoche !== orig.precioPorNoche ||
        edit.observaciones !== orig.observaciones
      );
    });

  const handleDetalleChange = (
    detalleId: number,
    field: string,
    value: unknown,
  ) => {
    setDetallesEdit((prev) => {
      const next = new Map(prev);
      const current = next.get(detalleId) ?? {};
      next.set(detalleId, { ...current, [field]: value });
      return next;
    });
  };

  const handleSave = async () => {
    if (!numId) return;

    setSaving(true);

    const headerPayload: Record<string, unknown> = {};
    if (checkIn !== reserva?.fechaCheckIn) headerPayload.fechaCheckIn = checkIn || undefined;
    if (checkOut !== reserva?.fechaCheckOut) headerPayload.fechaCheckOut = checkOut || undefined;
    if (titularId !== reserva?.titular?.id) headerPayload.titularReservaId = titularId;
    if (origen !== (reserva?.origen ?? "RECEPCION")) headerPayload.origen = origen as ReservaUpdateRequestOrigen;
    if (observaciones !== (reserva?.observaciones ?? "")) headerPayload.observaciones = observaciones || undefined;

    const hasHeaderChanges = Object.keys(headerPayload).length > 0;

    const detallesConCambios = [...detallesEdit.entries()].filter(([id, edit]) => {
      const orig = reserva?.detalles?.find((d) => d.id === id);
      if (!orig) return true;
      return (
        edit.cantidadOcupantes !== orig.cantidadOcupantes ||
        edit.precioPorNoche !== orig.precioPorNoche ||
        edit.observaciones !== orig.observaciones
      );
    });

    try {
      if (hasHeaderChanges) {
        await actualizar.mutateAsync({ id: numId, data: headerPayload as ReservaUpdateRequest });
      }

      for (const [detalleId, edit] of detallesConCambios) {
        await actualizarDetalle.mutateAsync({
          reservaId: numId,
          detalleId,
          data: {
            cantidadOcupantes: edit.cantidadOcupantes,
            observaciones: edit.observaciones,
          },
        });
      }

      toast.success("Reserva actualizada correctamente");
      navigate("/dashboard/reservas");
    } catch {
      setSaving(false);
    }
  };

  const handleAddGuest = (detalleId: number, huespedesIds: number[]) => {
    if (!numId) return;
    agregarHuespedes.mutate(
      { reservaId: numId, detalleId, huespedesIds },
      { onSuccess: () => setAddGuestDetalle(null) },
    );
  };

  const handleRemoveGuest = () => {
    if (!numId || !removeGuestTarget) return;
    removerHuesped.mutate(
      {
        reservaId: numId,
        detalleId: removeGuestTarget.detalleId,
        detalleHuespedId: removeGuestTarget.detalleHuespedId,
      },
      { onSuccess: () => setRemoveGuestTarget(null) },
    );
  };

  const handleAddRoom = (data: DetalleReservaRequest) => {
    if (!numId) return;
    agregarDetalle.mutate(
      { reservaId: numId, data },
      { onSuccess: () => setAddRoomOpen(false) },
    );
  };

  const handleRemoveDetalle = () => {
    if (!numId || !removeDetalleTarget) return;
    removerDetalle.mutate(
      { reservaId: numId, detalleId: removeDetalleTarget.detalleId },
      { onSuccess: () => setRemoveDetalleTarget(null) },
    );
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-card py-6">
        <div className="max-w-6xl mx-auto px-4">
          <div className="rounded-2xl border border-border bg-card p-6 shadow-sm">
            <DetailSkeleton cards={2} />
          </div>
        </div>
      </div>
    );
  }

  if (!reserva) {
    return (
      <div className="min-h-screen bg-card py-6">
        <div className="max-w-6xl mx-auto px-4">
          <div className="rounded-2xl border border-border bg-card p-6 shadow-sm">
            <NotFoundState
              title="Reserva no encontrada"
              description="La reserva que buscas no existe o fue eliminada."
            />
          </div>
        </div>
      </div>
    );
  }

  const statusInfo = ESTADO_BADGES[reserva.estado ?? ""] ?? { label: reserva.estado ?? "—", className: "" };
  const headerBgClass = ESTADO_HEADER_BG[reserva.estado ?? ""] ?? "bg-muted/50";

  return (
    <div className="min-h-screen bg-card">
      <div className="max-w-6xl mx-auto py-6 px-4">
        <motion.div
          initial="hidden"
          animate="visible"
          variants={{
            hidden: {},
            visible: { transition: { staggerChildren: 0.06 } },
          }}
          className="space-y-6"
        >
          {/* ── Header: back + identity + save ── */}
          <motion.div
            variants={{
              hidden: { opacity: 0, y: -8 },
              visible: { opacity: 1, y: 0 },
            }}
          >
            <div className={`rounded-2xl border border-border bg-card shadow-sm border-l-4 ${headerBgClass}`}>
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 px-5 py-4">
                <div className="flex items-center gap-4 min-w-0">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => navigate(-1)}
                    className="rounded-lg text-muted-foreground hover:text-foreground hover:bg-accent shrink-0"
                  >
                    <ArrowLeft className="w-4 h-4" />
                  </Button>
                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <h1 className="font-heading text-xl font-semibold text-foreground leading-tight">
                        {reserva.codigoReserva ?? "Reserva"}
                      </h1>
                      <span className={`inline-flex items-center text-[11px] px-2.5 py-0.5 rounded-full border font-medium ${statusInfo.className}`}>
                        {statusInfo.label}
                      </span>
                    </div>
                    <p className="text-sm text-muted-foreground mt-0.5">
                      {reserva.detalles?.length ?? 0} habitación{(reserva.detalles?.length ?? 0) !== 1 ? "es" : ""}
                    </p>
                  </div>
                </div>

                <Button
                  onClick={handleSave}
                  disabled={!hasChanges || saving}
                  className="bg-rose-800 hover:bg-rose-900 text-white shadow-sm disabled:opacity-50 disabled:cursor-not-allowed shrink-0"
                >
                  {saving ? (
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  ) : (
                    <Save className="w-4 h-4 mr-1.5" />
                  )}
                  Guardar cambios
                </Button>
              </div>
            </div>
          </motion.div>

          {/* ── Timeline + Titular ── */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <motion.div
              variants={{
                hidden: { opacity: 0, y: 12 },
                visible: { opacity: 1, y: 0 },
              }}
              className="lg:col-span-2"
            >
              <EditarInformacionGeneral
                checkIn={checkIn}
                checkOut={checkOut}
                origen={origen}
                observaciones={observaciones}
                checkInOpen={checkInOpen}
                checkOutOpen={checkOutOpen}
                onCheckInChange={setCheckIn}
                onCheckOutChange={setCheckOut}
                onOrigenChange={setOrigen}
                onObservacionesChange={setObservaciones}
                onCheckInOpenChange={setCheckInOpen}
                onCheckOutOpenChange={setCheckOutOpen}
              />
            </motion.div>

            <motion.div
              variants={{
                hidden: { opacity: 0, y: 12 },
                visible: { opacity: 1, y: 0 },
              }}
              className="rounded-2xl border border-border bg-card p-5 shadow-sm"
            >
              <h3 className="font-heading text-base font-medium text-foreground mb-4">
                Titular
              </h3>
              <TitularEditField
                value={titularId}
                currentName={
                  reserva.titular
                    ? `${reserva.titular.nombreCompleto ?? ""} ${reserva.titular.apellidoCompleto ?? ""}`
                    : undefined
                }
                onChange={setTitularId}
              />
            </motion.div>
          </div>

          {/* ── Rooms section ── */}
          <motion.div
            variants={{
              hidden: { opacity: 0, y: 12 },
              visible: { opacity: 1, y: 0 },
            }}
            className="rounded-2xl border border-border bg-card p-5 shadow-sm"
          >
            <div className="flex items-center justify-between mb-5">
              <h2 className="font-heading text-base font-medium text-foreground">
                Habitaciones y Huéspedes
              </h2>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              {(reserva.detalles ?? []).map((detalle) => (
                <EditarHabitacionCard
                  key={detalle.id}
                  detalle={detalle}
                  detalleEdit={detallesEdit.get(detalle.id!)}
                  noches={noches}
                  isLast={(reserva.detalles?.length ?? 0) <= 1}
                  puedeRemover={puedeRemoverDetalle}
                  puedeGestionarHuespedes={puedeGestionarHuespedes}
                  onDetalleChange={handleDetalleChange}
                  onRemoveDetalle={(detalleId, roomNumber) =>
                    setRemoveDetalleTarget({ detalleId, roomNumber })
                  }
                  onAddGuest={setAddGuestDetalle}
                  onRemoveGuest={(detalleId, detalleHuespedId, guestName) =>
                    setRemoveGuestTarget({
                      detalleId,
                      detalleHuespedId,
                      guestName,
                    })
                  }
                  checkIn={checkIn}
                  checkOut={checkOut}
                />
              ))}
            </div>

            {(reserva.estado === "PENDIENTE" ||
              reserva.estado === "CONFIRMADA") && (
              <button
                type="button"
                onClick={() => setAddRoomOpen(true)}
                disabled={agregarDetalle.isPending}
                className="w-full mt-4 flex items-center justify-center gap-3 py-5 rounded-xl border-2 border-dashed border-border text-muted-foreground hover:border-rose-400 hover:text-rose-600 dark:hover:border-rose-500 dark:hover:text-rose-400 transition-all cursor-pointer bg-transparent hover:bg-rose-50/30 dark:hover:bg-rose-900/5 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {agregarDetalle.isPending ? (
                  <Loader2 className="w-5 h-5 animate-spin" />
                ) : (
                  <>
                    <Plus className="w-5 h-5" />
                    <span className="text-sm font-medium">Agregar habitación</span>
                  </>
                )}
              </button>
            )}
          </motion.div>
        </motion.div>

        <AgregarDetalleModal
          open={addRoomOpen}
          onClose={() => setAddRoomOpen(false)}
          onConfirm={handleAddRoom}
          isPending={agregarDetalle.isPending}
          existingRoomIds={(reserva.detalles ?? [])
            .map((d) => d.habitacionId!)
            .filter(Boolean)}
          checkIn={checkIn}
          checkOut={checkOut}
          noches={noches}
        />

        <AddGuestModal
          open={addGuestDetalle != null}
          detalle={addGuestDetalle!}
          onClose={() => setAddGuestDetalle(null)}
          onConfirm={handleAddGuest}
          isPending={agregarHuespedes.isPending}
          checkIn={checkIn}
          checkOut={
            addGuestDetalle && detallesEdit.get(addGuestDetalle.id!)?.useCustomNoches
              ? format(
                  addDays(new Date(checkIn), detallesEdit.get(addGuestDetalle.id!)?.customNoches ?? 0),
                  "yyyy-MM-dd",
                )
              : checkOut
          }
        />

        <ConfirmDeleteDialog
          open={removeGuestTarget != null}
          onClose={() => setRemoveGuestTarget(null)}
          onConfirm={handleRemoveGuest}
          isPending={removerHuesped.isPending}
          title="Remover huésped"
          description={`¿Estás seguro de remover a "${removeGuestTarget?.guestName}" de esta habitación?`}
        />

        <ConfirmDeleteDialog
          open={removeDetalleTarget != null}
          onClose={() => setRemoveDetalleTarget(null)}
          onConfirm={handleRemoveDetalle}
          isPending={removerDetalle.isPending}
          title="Remover habitación"
          description={`¿Estás seguro de remover la habitación ${removeDetalleTarget?.roomNumber} de la reserva? El detalle se cancelará y el total se recalculará.`}
        />
      </div>
    </div>
  );
}

export default EditarReservaPage;
