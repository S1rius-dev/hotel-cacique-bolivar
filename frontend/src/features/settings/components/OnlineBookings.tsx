import { useCallback, useState } from "react";
import { PlusCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";
import { OnlineReservationStatus } from "./OnlineReservationStatus";
import { OnlineReservationStats } from "./OnlineReservationStats";
import { ReservationBookingRules } from "./ReservationBookingRules";
import { ReservationBlockCalendar } from "./ReservationBlockCalendar";
import { ReservationBlockList } from "./ReservationBlockList";
import { ReservationBlockModal } from "./ReservationBlockModal";
import { BlockingInfoAlert } from "./BlockingInfoAlert";
import { useBloqueos } from "../hooks/useBloqueos";
import { useCreateBloqueo } from "../hooks/useCreateBloqueo";
import { useUpdateBloqueo } from "../hooks/useUpdateBloqueo";
import { useDeleteBloqueo } from "../hooks/useDeleteBloqueo";
import { useOnlineReservationSettings } from "../hooks/useOnlineReservationSettings";
import { useToggleWebReservations } from "../hooks/useToggleWebReservations";
import { computeBlockStats, findBlockForDate } from "../utils/blocks.utils";
import type { BookingBlock } from "../types";
import type { BlockFormValues } from "../schemas/block.schema";

interface CreatePrefill {
  fechaInicio: Date;
  fechaFin: Date;
}

export function OnlineBookings() {
  const [modalOpen, setModalOpen] = useState(false);
  const [editingBlock, setEditingBlock] = useState<BookingBlock | null>(null);
  const [createPrefill, setCreatePrefill] = useState<CreatePrefill | null>(null);
  const [blockToDeactivate, setBlockToDeactivate] =
    useState<BookingBlock | null>(null);
  const [confirmToggleOpen, setConfirmToggleOpen] = useState(false);
  const [pendingEnabled, setPendingEnabled] = useState(true);

  const { data: blocks = [], isLoading } = useBloqueos();
  const createBloqueo = useCreateBloqueo();
  const updateBloqueo = useUpdateBloqueo();
  const deleteBloqueo = useDeleteBloqueo();
  const { settings, dirty, updateRule, saveRules } =
    useOnlineReservationSettings();
  const { webEnabled, isPending: isTogglePending, toggle } =
    useToggleWebReservations();

  const stats = computeBlockStats(blocks);

  const handleCreate = useCallback((prefill?: CreatePrefill) => {
    setEditingBlock(null);
    setCreatePrefill(prefill ?? null);
    setModalOpen(true);
  }, []);

  const handleEdit = useCallback((block: BookingBlock) => {
    setEditingBlock(block);
    setCreatePrefill(null);
    setModalOpen(true);
  }, []);

  const handleDayClick = useCallback(
    (date: Date) => {
      const block = findBlockForDate(blocks, date);
      if (block) {
        handleEdit(block);
      } else {
        handleCreate({ fechaInicio: date, fechaFin: date });
      }
    },
    [blocks, handleCreate, handleEdit],
  );

  const handleSave = useCallback(
    (data: BlockFormValues, id?: number) => {
      const onSuccess = () => setModalOpen(false);
      if (id) {
        updateBloqueo.mutate({ id, data }, { onSuccess });
      } else {
        createBloqueo.mutate(data, { onSuccess });
      }
    },
    [createBloqueo, updateBloqueo],
  );

  const confirmDeactivate = useCallback(() => {
    if (!blockToDeactivate) return;
    deleteBloqueo.mutate(blockToDeactivate.id, {
      onSuccess: () => setBlockToDeactivate(null),
    });
  }, [blockToDeactivate, deleteBloqueo]);

  const handleReactivate = useCallback(
    (block: BookingBlock) => {
      updateBloqueo.mutate({
        id: block.id,
        data: {
          fechaInicio: new Date(block.fechaInicio),
          fechaFin: new Date(block.fechaFin),
          motivo: block.motivo,
          activo: true,
        },
      });
    },
    [updateBloqueo],
  );

  const isPending =
    createBloqueo.isPending ||
    updateBloqueo.isPending ||
    deleteBloqueo.isPending;

  const handleToggleWeb = useCallback(
    (enabled: boolean) => {
      setPendingEnabled(enabled);
      setConfirmToggleOpen(true);
    },
    [],
  );

  const confirmToggle = useCallback(() => {
    toggle(pendingEnabled);
    setConfirmToggleOpen(false);
  }, [toggle, pendingEnabled]);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
          Reservas Online
        </h2>
        <p className="mt-1 text-sm text-muted-foreground">
          Administra cuándo las reservas realizadas desde la página web estarán
          habilitadas o bloqueadas.
        </p>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="min-w-0 lg:col-span-2">
          <OnlineReservationStatus
            webEnabled={webEnabled}
            onToggle={handleToggleWeb}
          />
        </div>
        <div className="min-w-0">
          <OnlineReservationStats stats={stats} />
        </div>
      </div>

      <div className="grid items-start gap-4 lg:grid-cols-[340px_minmax(0,1fr)] xl:grid-cols-[380px_minmax(0,1fr)]">
        <ReservationBookingRules
          rules={settings.rules}
          dirty={dirty}
          onUpdateRule={updateRule}
          onSave={saveRules}
        />

        <div className="min-w-0 space-y-4">
          <section className="overflow-hidden rounded-xl border border-zinc-200 bg-card shadow-sm dark:border-zinc-800">
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-zinc-100 px-5 py-4 dark:border-zinc-800">
              <div className="min-w-0">
                <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
                  Bloqueos de reservas web
                </h3>
                <p className="mt-0.5 text-xs text-muted-foreground">
                  Gestiona los períodos en los que no se permiten reservas desde
                  la página web.
                </p>
              </div>
              <Button
                variant="hotel"
                onClick={() => handleCreate()}
                className="cursor-pointer"
                disabled={isPending}
              >
                <PlusCircle className="size-4" />
                Nuevo bloqueo
              </Button>
            </div>

            <div className="p-4 sm:p-5">
              <ReservationBlockCalendar
                blocks={blocks}
                loading={isLoading}
                onDayClick={handleDayClick}
              />
              <div className="my-4 h-px bg-zinc-100 dark:bg-zinc-800" />
              <ReservationBlockList
                blocks={blocks}
                loading={isLoading}
                onEdit={handleEdit}
                onDeactivate={setBlockToDeactivate}
                onReactivate={handleReactivate}
                onCreate={() => handleCreate()}
              />
            </div>
          </section>

          <BlockingInfoAlert />
        </div>
      </div>

      <ReservationBlockModal
        open={modalOpen}
        onOpenChange={setModalOpen}
        onSave={handleSave}
        editBlock={editingBlock}
        initialDates={createPrefill}
        isPending={isPending}
      />

      <ConfirmDeleteDialog
        open={!!blockToDeactivate}
        onClose={() => setBlockToDeactivate(null)}
        onConfirm={confirmDeactivate}
        title="¿Deseas desactivar este bloqueo?"
        description="Las reservas web volverán a estar disponibles durante este periodo."
        confirmLabel="Desactivar"
        isPending={deleteBloqueo.isPending}
      />

      <ConfirmDeleteDialog
        open={confirmToggleOpen}
        onClose={() => setConfirmToggleOpen(false)}
        onConfirm={confirmToggle}
        title={
          pendingEnabled
            ? "¿Deseas habilitar las reservas web?"
            : "¿Deseas deshabilitar las reservas web?"
        }
        description={
          pendingEnabled
            ? "Los huéspedes podrán realizar reservas desde la página web nuevamente."
            : "Los huéspedes no podrán realizar reservas desde la página web hasta que se vuelva a habilitar."
        }
        confirmLabel={pendingEnabled ? "Habilitar" : "Deshabilitar"}
        isPending={isTogglePending}
      />
    </div>
  );
}