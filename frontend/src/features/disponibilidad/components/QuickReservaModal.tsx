import { motion } from "motion/react";
import { useState } from "react";
import { format, addDays, differenceInCalendarDays } from "date-fns";
import { BedDouble, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { useClientesSearch } from "../hooks/useClientesSearch";
import { useCrearReservaRapida } from "../hooks/useCrearReservaRapida";
import { RegistrarPagoModal } from "@/features/pagos";
import { formatCurrency } from "@/utils/format";
import QuickReservaDatePicker from "./QuickReservaDatePicker";
import QuickReservaOccupantsSelector from "./QuickReservaOccupantsSelector";
import QuickReservaClientSearch from "./QuickReservaClientSearch";
import QuickReservaCreateClientDialog from "./QuickReservaCreateClientDialog";
import type { HabitacionEstadoDiario, ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  open: boolean;
  habitacion: HabitacionEstadoDiario | null;
  onClose: () => void;
  selectedDate?: Date;
}

function getDefaultCheckOut(date: Date): Date {
  return date.getHours() < 6 ? date : addDays(date, 1);
}

type BodyProps = Omit<Props, "open"> & { habitacion: HabitacionEstadoDiario };

/**
 * Cuerpo del modal. Se monta solo mientras el dialogo esta abierto, asi que las
 * fechas, el cliente y los ocupantes arrancan desde los props de la habitacion
 * seleccionada sin un efecto que los resetee en cada apertura.
 */
function QuickReservaBody({ habitacion, onClose, selectedDate }: BodyProps) {
  const date = selectedDate ?? new Date();

  const [checkIn, setCheckIn] = useState<Date>(date);
  const [checkOut, setCheckOut] = useState(getDefaultCheckOut(date));
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<ClienteResponse[]>([]);
  const [showCreateClient, setShowCreateClient] = useState(false);
  const [touched, setTouched] = useState(false);

  const [createdReserva, setCreatedReserva] = useState<{
    id: number;
    total: number;
    totalPagado: number;
    saldoPendiente: number;
  } | null>(null);

  const capacidadBase = habitacion.capacidadBase ?? 1;
  const capacidadMaxima = habitacion.capacidadMaxima ?? 1;
  const [ocupantes, setOcupantes] = useState(capacidadBase);

  const debouncedSearch = useDebounce(search, 300);

  const {
    data: clientes,
    isLoading: loadingClientes,
    isFetching,
  } = useClientesSearch(debouncedSearch);

  const mutation = useCrearReservaRapida();

  const handleClose = () => {
    setSearch("");
    setSelected([]);
    setOcupantes(capacidadBase);
    setTouched(false);
    setCreatedReserva(null);
    onClose();
  };

  const handleClientCreated = (cliente: ClienteResponse) => {
    setSelected((prev) =>
      prev.some((x) => x.id === cliente.id)
        ? prev
        : [...prev, cliente],
    );
    setSearch("");
  };

  const toggleCliente = (c: ClienteResponse) => {
    setSelected((prev) =>
      prev.some((x) => x.id === c.id)
        ? prev.filter((x) => x.id !== c.id)
        : prev.length >= ocupantes
          ? prev
          : [...prev, c],
    );
  };

  const isOcupantesValido =
    ocupantes >= capacidadBase && ocupantes <= capacidadMaxima;

  const noches = differenceInCalendarDays(checkOut, checkIn);
  const precioBase =
    (habitacion as unknown as { precioNocheEfectivo?: number })
      ?.precioNocheEfectivo ?? habitacion?.precioBase ?? 0;
  const precioPersonaExtra =
    (habitacion as unknown as { precioPersonaExtraEfectivo?: number })
      ?.precioPersonaExtraEfectivo ?? habitacion?.precioPersonaExtra ?? 0;
  const esTarifaTemporada = precioBase !== (habitacion?.precioBase ?? 0);
  const personasExtras = Math.max(0, ocupantes - capacidadBase);
  const subtotalPreview =
    noches > 0
      ? precioBase * noches + personasExtras * precioPersonaExtra * noches
      : precioBase;

  const handleSubmit = async () => {
    setTouched(true);

    if (
      selected.length === 0 ||
      selected.length > ocupantes ||
      !isOcupantesValido
    ) {
      return;
    }

    const reservaCreada = await mutation.mutateAsync({
      habitacionId: habitacion.id!,
      fechaCheckIn: format(checkIn, "yyyy-MM-dd"),
      fechaCheckOut: format(checkOut, "yyyy-MM-dd"),
      cantidadOcupantes: ocupantes,
      huespedesIds: selected.map((c) => c.id!),
    });

    if (reservaCreada.id) {
      setCreatedReserva({
        id: reservaCreada.id,
        total: reservaCreada.total ?? 0,
        totalPagado: reservaCreada.totalPagado ?? 0,
        saldoPendiente:
          reservaCreada.saldoPendiente ?? reservaCreada.total ?? 0,
      });
    }

    toast.success("Reserva creada exitosamente");
  };

  return (
    <>
      <Dialog open onOpenChange={(v) => !v && handleClose()}>
        <DialogContent className="sm:max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <BedDouble className="w-5 h-5 text-emerald-600" />
              Nueva reserva
              <span className="ml-1 text-sm font-normal text-muted-foreground">
                — Habitación {habitacion.numero} · {habitacion.tipoNombre} ·{" "}
                {capacidadBase}-{capacidadMaxima} personas
              </span>
            </DialogTitle>
          </DialogHeader>

          <motion.div
            initial="hidden"
            animate="visible"
            variants={{
              hidden: {},
              visible: { transition: { staggerChildren: 0.07 } },
            }}
            className="flex gap-6 py-2 min-h-0 flex-1">
            <motion.div
              variants={{
                hidden: { opacity: 0, x: -15 },
                visible: { opacity: 1, x: 0 },
              }}
              className="w-75 shrink-0 space-y-6 overflow-y-auto min-h-0">
              <QuickReservaDatePicker
                checkIn={checkIn}
                checkOut={checkOut}
                onCheckInChange={setCheckIn}
                onCheckOutChange={setCheckOut}
              />

              <QuickReservaOccupantsSelector
                capacidadBase={capacidadBase}
                capacidadMaxima={capacidadMaxima}
                ocupantes={ocupantes}
                onChange={setOcupantes}
                touched={touched}
                onTouch={() => setTouched(true)}
              />
            </motion.div>

            <motion.div
              variants={{
                hidden: { opacity: 0, x: 15 },
                visible: { opacity: 1, x: 0 },
              }}
              className="flex-1 flex flex-col min-h-0 overflow-hidden">
              <QuickReservaClientSearch
                search={search}
                onSearchChange={setSearch}
                selected={selected}
                onToggle={toggleCliente}
                clientes={clientes}
                loadingClientes={loadingClientes || isFetching}
                onCreateNewClient={() => setShowCreateClient(true)}
              />
            </motion.div>
          </motion.div>

          {precioBase > 0 && (
            <div className="rounded-lg bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-800 px-4 py-3 mx-0">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">Subtotal preview</span>
                <span className="font-bold text-emerald-700 dark:text-emerald-400">
                  {formatCurrency(subtotalPreview)}
                </span>
              </div>
              <p className="text-[10px] text-muted-foreground mt-1">
                {noches > 0 ? (
                  <>
                    {formatCurrency(precioBase)} x {noches} noche
                    {noches !== 1 ? "s" : ""}
                    {personasExtras > 0 && (
                      <>
                        {" "}
                        + {personasExtras} extra ×{" "}
                        {formatCurrency(precioPersonaExtra)}
                      </>
                    )}
                    {esTarifaTemporada && (
                      <span className="ml-1 text-emerald-600">
                        · tarifa temporada
                      </span>
                    )}
                  </>
                ) : (
                  "Tarifa plana (mismo día)"
                )}
              </p>
            </div>
          )}

          <DialogFooter className="border-t pt-4 mt-2">
            <Button variant="ghost" onClick={handleClose}>
              Cancelar
            </Button>

            <Button
              onClick={handleSubmit}
              disabled={
                selected.length === 0 ||
                selected.length > ocupantes ||
                mutation.isPending ||
                !isOcupantesValido
              }
              className="bg-emerald-700 hover:bg-emerald-800">
              {mutation.isPending && (
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
              )}

              {selected.length === 0
                ? "Seleccione un huésped"
                : selected.length > ocupantes
                  ? `Máximo ${ocupantes} huésped(es)`
                  : !isOcupantesValido
                    ? "Verifique ocupantes"
                    : `Reservar (${selected.length})`}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {createdReserva && (
        <RegistrarPagoModal
          open={true}
          reservaId={createdReserva.id}
          total={createdReserva.total}
          totalPagado={createdReserva.totalPagado}
          saldoPendiente={createdReserva.saldoPendiente}
          onClose={() => {
            setCreatedReserva(null);
            handleClose();
          }}
        />
      )}

      <QuickReservaCreateClientDialog
        open={showCreateClient}
        onOpenChange={setShowCreateClient}
        onClientCreated={handleClientCreated}
      />
    </>
  );
}

function QuickReservaModal({ open, habitacion, ...rest }: Props) {
  return (
    <>
      {open && habitacion && (
        <QuickReservaBody habitacion={habitacion} {...rest} />
      )}
    </>
  );
}

export default QuickReservaModal;
