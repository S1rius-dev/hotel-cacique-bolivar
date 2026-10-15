import { useState } from "react";
import { X, User, BedDouble, Check, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { useBuscarCliente } from "../../hooks/useBuscarCliente";
import QuickReservaClientSearch from "@/features/disponibilidad/components/QuickReservaClientSearch";
import type { DetalleReservaResponse, HuespedResponse, ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  open: boolean;
  onClose: () => void;
  detalle: DetalleReservaResponse;
  huespedesActuales?: HuespedResponse[];
  onConfirm: (detalleId: number, clienteIds: number[]) => void;
  isPending: boolean;
}

function CheckInModal({
  open,
  onClose,
  detalle,
  huespedesActuales,
  onConfirm,
  isPending,
}: Props) {
  const [selectedClientes, setSelectedClientes] = useState<ClienteResponse[]>([]);
  const [search, setSearch] = useState("");
  const debouncedSearch = useDebounce(search, 300);
  const { data: clientesData, isLoading: loadingClientes } = useBuscarCliente(debouncedSearch);
  const clientes = clientesData?.content ?? [];

  if (!open) return null;

  const toggleCliente = (cliente: ClienteResponse) => {
    setSelectedClientes((prev) =>
      prev.some((s) => s.id === cliente.id)
        ? prev.filter((s) => s.id !== cliente.id)
        : [...prev, cliente],
    );
  };

  const handleConfirm = () => {
    const ids = selectedClientes
      .map((c) => c.id!)
      .filter((id): id is number => id != null);
    onConfirm(detalle.id!, ids);
    setSelectedClientes([]);
    setSearch("");
  };

  const handleClose = () => {
    setSelectedClientes([]);
    setSearch("");
    onClose();
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
              <div className="p-2 rounded-full bg-green-100 dark:bg-green-900/30">
                <BedDouble className="w-5 h-5 text-green-600 dark:text-green-400" />
              </div>
              <div>
                <h2 className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                  Check-in: Habitación {detalle.habitacionNumero ?? "—"}
                </h2>
                {detalle.cantidadOcupantes != null && (
                  <p className="text-xs text-muted-foreground">
                    Capacidad: {detalle.cantidadOcupantes} ocupante(s)
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
          <div className="p-5 overflow-y-auto space-y-5">
            {huespedesActuales && huespedesActuales.length > 0 && (
              <div className="space-y-2">
                <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Huéspedes registrados
                </p>

                <div className="space-y-1.5">
                  {huespedesActuales.map((h) => (
                    <div
                      key={h.detalleHuespedId ?? h.huespedId}
                      className="flex items-center gap-3 px-3 py-2 rounded-lg border bg-zinc-50 dark:bg-zinc-900/40"
                    >
                      <User className="w-4 h-4 text-muted-foreground shrink-0" />
                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
                          {`${h.nombreCompleto ?? ""} ${h.apellidoCompleto ?? ""}`.trim() || "—"}
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
              </div>
            )}

            {/* Selector de clientes */}
            <div className="space-y-2">
              <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                Agregar huéspedes
              </p>

              <QuickReservaClientSearch
                search={search}
                onSearchChange={setSearch}
                selected={selectedClientes}
                onToggle={toggleCliente}
                clientes={clientes}
                loadingClientes={loadingClientes}
                excludeIds={huespedesActuales?.map((h) => h.huespedId).filter((id): id is number => id != null)}
              />
            </div>
          </div>

          {/* Footer */}
          <div className="flex items-center justify-end gap-3 px-5 py-4 border-t border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/50 shrink-0">
            <Button
              variant="ghost"
              onClick={handleClose}
              disabled={isPending}
              className="text-muted-foreground hover:text-foreground"
            >
              Cancelar
            </Button>

            <Button
              variant="default"
              onClick={handleConfirm}
              disabled={isPending}
              className="gap-2 bg-green-700 hover:bg-green-800 text-white"
            >
              {isPending && <Loader2 className="w-4 h-4 animate-spin" />}
              <User className="w-4 h-4" />
              Confirmar Check-in
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CheckInModal;
