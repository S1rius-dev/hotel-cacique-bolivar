import { motion } from "motion/react";
import { useState } from "react";
import { X, User, Check, Loader2, LogIn, UserPlus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { useClientesSearch } from "../hooks/useClientesSearch";
import { useCheckInPorDetalle } from "../hooks/useCheckInOut";
import QuickReservaClientSearch from "./QuickReservaClientSearch";
import type {
  HabitacionEstadoDiario,
  ClienteResponse,
} from "@/api/generated/openAPIDefinition.schemas";
import QuickReservaCreateClientDialog from "./QuickReservaCreateClientDialog";

interface Props {
  open: boolean;
  habitacion: HabitacionEstadoDiario | null;
  onClose: () => void;
}

function CheckInModal({ open, habitacion, onClose }: Props) {
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<ClienteResponse[]>([]);
  const debouncedSearch = useDebounce(search, 300);
  const [showCreateClient, setShowCreateClient] = useState(false);

  const { data: clientes, isLoading: loadingClientes } =
    useClientesSearch(debouncedSearch);

  const mutation = useCheckInPorDetalle();

  if (!open || !habitacion || !habitacion.reservaActiva) return null;

  const esAgregarHuesped = habitacion.estado === "OCUPADA";
  const huespedesActuales = habitacion.reservaActiva?.huespedes ?? [];
  const maxOcupantes =
    habitacion.reservaActiva.cantidadOcupantes ??
    habitacion.capacidadMaxima ??
    0;
  const limiteHuespedes = maxOcupantes - huespedesActuales.length;

  const handleClose = () => {
    setSearch("");
    setSelected([]);
    onClose();
  };

  const toggleCliente = (c: ClienteResponse) => {
    setSelected((prev) =>
      prev.some((x) => x.id === c.id)
        ? prev.filter((x) => x.id !== c.id)
        : prev.length >= limiteHuespedes
          ? prev
          : [...prev, c],
    );
  };

  const handleSubmit = async () => {
    if (selected.length === 0 || !habitacion.reservaActiva) return;

    await mutation.mutateAsync({
      reservaId: habitacion.reservaActiva.reservaId!,
      detalleId: habitacion.reservaActiva.detalleId!,
      huespedesIds: selected.map((c) => c.id!),
    });

    handleClose();
  };

  const handleClientCreated = (cliente: ClienteResponse) => {
    setSelected((prev) =>
      prev.some((x) => x.id === cliente.id) ? prev : [...prev, cliente],
    );
    setSearch("");
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={handleClose}
      />

      <div className="relative w-full max-w-lg mx-4 max-h-[85vh] flex flex-col">
        <div className="bg-card rounded-2xl shadow-2xl border overflow-hidden flex flex-col max-h-full">
          {/* Header */}
          <div className="flex items-center justify-between px-5 py-4 border-b border-zinc-100 dark:border-zinc-800 shrink-0">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-amber-100 dark:bg-amber-900/30">
                {esAgregarHuesped ? (
                  <UserPlus className="w-5 h-5 text-amber-600 dark:text-amber-400" />
                ) : (
                  <LogIn className="w-5 h-5 text-amber-600 dark:text-amber-400" />
                )}
              </div>

              <div>
                <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                  {esAgregarHuesped
                    ? `Agregar huésped: Hab. ${habitacion.numero}`
                    : `Check-in: Hab. ${habitacion.numero}`}
                </h2>

                <p className="text-xs text-muted-foreground">
                  Capacidad: {maxOcupantes} ocupante(s)
                  {huespedesActuales.length > 0 && (
                    <> · {huespedesActuales.length} registrado(s)</>
                  )}
                </p>
                {limiteHuespedes > 0 && (
                  <p className="text-[10px] text-muted-foreground">
                    Puede agregar hasta {limiteHuespedes} más
                  </p>
                )}
              </div>
            </div>

            <button
              onClick={handleClose}
              className="p-1 rounded-full hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors">
              <X className="w-4 h-4 text-muted-foreground" />
            </button>
          </div>

          {/* Body */}
          <motion.div
            initial="hidden"
            animate="visible"
            variants={{
              hidden: {},
              visible: { transition: { staggerChildren: 0.07 } },
            }}
            className="p-5 overflow-y-auto space-y-5">
            {/* Huéspedes actuales */}
            {huespedesActuales.length > 0 && (
              <motion.div
                variants={{
                  hidden: { opacity: 0, y: 10 },
                  visible: { opacity: 1, y: 0 },
                }}
                className="space-y-2">
                <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Huéspedes registrados ({huespedesActuales.length})
                </p>

                <div className="space-y-1.5">
                  {huespedesActuales.map((h, i) => (
                    <div
                      key={h.huespedId ?? i}
                      className="flex items-center gap-3 px-3 py-2 rounded-lg border bg-zinc-50 dark:bg-zinc-900/40">
                      <User className="w-4 h-4 text-muted-foreground shrink-0" />

                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
                          {h.nombreCompleto}
                        </p>

                        {h.cedula && (
                          <p className="text-[11px] text-muted-foreground">
                            {h.cedula}
                          </p>
                        )}
                      </div>

                      <Check className="w-4 h-4 text-green-600 shrink-0" />
                    </div>
                  ))}
                </div>
              </motion.div>
            )}

            {/* Buscar y agregar nuevos huéspedes */}
            <motion.div
              variants={{
                hidden: { opacity: 0, y: 10 },
                visible: { opacity: 1, y: 0 },
              }}
              className="space-y-2">
              <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                {esAgregarHuesped
                  ? "Agregar otro huésped"
                  : "Seleccionar huéspedes"}
              </p>

              <QuickReservaClientSearch
                search={search}
                onSearchChange={setSearch}
                selected={selected}
                onToggle={toggleCliente}
                clientes={clientes}
                loadingClientes={loadingClientes}
                excludeIds={huespedesActuales
                  .map((h) => h.huespedId)
                  .filter((id): id is number => id != null)}
                onCreateNewClient={() => setShowCreateClient(true)}
              />
            </motion.div>
          </motion.div>

          {/* Footer */}
          <div className="flex items-center justify-end gap-3 px-5 py-4 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50 shrink-0">
            <Button
              variant="ghost"
              onClick={handleClose}
              className="text-muted-foreground hover:text-foreground">
              Cancelar
            </Button>

            <Button
              onClick={handleSubmit}
              disabled={
                selected.length === 0 ||
                selected.length > limiteHuespedes ||
                mutation.isPending
              }
              className="gap-2 bg-amber-600 hover:bg-amber-700 text-white">
              {mutation.isPending && (
                <Loader2 className="w-4 h-4 animate-spin" />
              )}

              {selected.length === 0
                ? "Seleccione un huésped"
                : selected.length > limiteHuespedes
                  ? `Máximo ${limiteHuespedes}`
                  : esAgregarHuesped
                    ? `Agregar (${selected.length})`
                    : `Check-in (${selected.length})`}
            </Button>

            <QuickReservaCreateClientDialog
              open={showCreateClient}
              onOpenChange={setShowCreateClient}
              onClientCreated={handleClientCreated}
            />
          </div>
        </div>
      </div>
    </div>
  );
}

export default CheckInModal;
