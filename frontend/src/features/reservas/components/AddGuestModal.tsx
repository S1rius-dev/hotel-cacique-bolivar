import { useState } from "react";
import { X, User, Plus, Loader2, Minus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { useDebounce } from "@/lib/hooks/useDebounce";
import { useBuscarCliente } from "../hooks/useBuscarCliente";
import { differenceInCalendarDays, addDays, format } from "date-fns";
import { es } from "date-fns/locale";
import type { DetalleReservaResponse, ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  open: boolean;
  detalle: DetalleReservaResponse;
  onClose: () => void;
  onConfirm: (detalleId: number, huespedesIds: number[]) => void;
  isPending: boolean;
  checkIn?: string;
  checkOut?: string;
}

function AddGuestModal({ open, detalle, onClose, onConfirm, isPending, checkIn: reservaCheckIn, checkOut: reservaCheckOut }: Props) {
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<ClienteResponse[]>([]);
  const [useCustomNoches, setUseCustomNoches] = useState(false);
  const [customNoches, setCustomNoches] = useState(1);
  const debouncedSearch = useDebounce(search, 300);
  const { data: clientesData, isLoading: loadingClientes } = useBuscarCliente(debouncedSearch);
  const clientes = clientesData?.content ?? [];

  if (!open) return null;

  const reservaNoches = reservaCheckIn && reservaCheckOut
    ? differenceInCalendarDays(new Date(reservaCheckOut), new Date(reservaCheckIn))
    : 0;

  const existingIds = (detalle.huespedes ?? []).map((h) => h.huespedId).filter((id): id is number => id != null);

  const disponibles = clientes.filter(
    (c) =>
      !selected.some((s) => s.id === c.id) &&
      !(c.id != null && existingIds.includes(c.id)),
  );

  const toggleCliente = (c: ClienteResponse) => {
    setSelected((prev) =>
      prev.some((s) => s.id === c.id)
        ? prev.filter((s) => s.id !== c.id)
        : [...prev, c],
    );
  };

  const handleConfirm = () => {
    const huespedesIds = selected
      .map((c) => c.id!)
      .filter((id): id is number => id != null);
    onConfirm(detalle.id!, huespedesIds);
    setSelected([]);
    setSearch("");
    setUseCustomNoches(false);
    setCustomNoches(1);
  };

  const handleClose = () => {
    setSelected([]);
    setSearch("");
    setUseCustomNoches(false);
    setCustomNoches(1);
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <User className="w-5 h-5 text-emerald-600" />
            Agregar huéspedes
            <span className="text-sm font-normal text-muted-foreground">
              — Habitación {detalle.habitacionNumero}
            </span>
          </DialogTitle>
          <DialogDescription>
            Seleccione los clientes que desea agregar como huéspedes.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {selected.length > 0 && (
            <div className="flex flex-wrap gap-1.5">
              {selected.map((c) => (
                <span
                  key={c.id}
                  className="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400"
                >
                  {c.nombreCompleto}
                  <button onClick={() => toggleCliente(c)} className="hover:text-emerald-900">
                    <X className="w-3 h-3" />
                  </button>
                </span>
              ))}
            </div>
          )}

          <div className="space-y-1.5">
            <label className="text-xs font-medium text-muted-foreground">Buscar cliente</label>
            <div className="relative">
              <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <input
                type="text"
                placeholder="Nombre o cédula..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full h-10 pl-9 pr-3 rounded-lg border bg-background text-sm outline-none focus:border-ring focus:ring-[3px] focus:ring-ring/50"
              />
            </div>
          </div>

          <div className="max-h-48 overflow-y-auto space-y-1.5">
            {loadingClientes && search.length >= 2 && (
              <div className="flex items-center justify-center py-4">
                <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
              </div>
            )}
            {disponibles.slice(0, 8).map((cliente) => (
              <div
                key={cliente.id}
                className="flex items-center justify-between p-2.5 rounded-lg border bg-white dark:bg-zinc-950/40 hover:bg-zinc-50 dark:hover:bg-zinc-900/60 cursor-pointer"
                onClick={() => toggleCliente(cliente)}
              >
                <div className="flex items-center gap-2.5 min-w-0">
                  <div className="p-1 rounded-full bg-zinc-100 dark:bg-zinc-800 shrink-0">
                    <User className="w-3.5 h-3.5 text-zinc-500" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-medium truncate">{cliente.nombreCompleto} {cliente.apellidoCompleto}</p>
                    <p className="text-[11px] text-muted-foreground">CC {cliente.cedula}</p>
                  </div>
                </div>
                <Plus className="w-4 h-4 text-emerald-600 shrink-0 ml-2" />
              </div>
            ))}
            {clientes && search.length >= 2 && disponibles.length === 0 && (
              <p className="text-xs text-muted-foreground text-center py-2">No se encontraron clientes</p>
            )}
          </div>

          {selected.length > 0 && reservaCheckIn && reservaNoches > 0 && (
            <div className="space-y-3 rounded-lg border bg-zinc-50 dark:bg-zinc-900/40 p-3">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={!useCustomNoches}
                  onChange={() => setUseCustomNoches(!useCustomNoches)}
                  className="rounded border-zinc-300 text-emerald-600 focus:ring-emerald-500"
                />
                <span className="text-xs font-medium text-muted-foreground">
                  Todas las noches de la reserva ({reservaNoches} {reservaNoches === 1 ? "noche" : "noches"})
                </span>
              </label>

              {useCustomNoches && (
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-medium text-muted-foreground">Noches de estadía</span>
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => setCustomNoches(Math.max(1, customNoches - 1))}
                        disabled={customNoches <= 1}
                        className="flex items-center justify-center w-8 h-8 rounded-lg border bg-background text-muted-foreground hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:pointer-events-none transition-colors"
                      >
                        <Minus className="w-4 h-4" />
                      </button>
                      <span className="w-8 text-center text-sm font-semibold tabular-nums">{customNoches}</span>
                      <button
                        type="button"
                        onClick={() => setCustomNoches(Math.min(reservaNoches, customNoches + 1))}
                        disabled={customNoches >= reservaNoches}
                        className="flex items-center justify-center w-8 h-8 rounded-lg border bg-background text-muted-foreground hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:pointer-events-none transition-colors"
                      >
                        <Plus className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                  <p className="text-xs text-muted-foreground text-center">
                    {format(new Date(reservaCheckIn), "d MMM", { locale: es })} →{" "}
                    {format(addDays(new Date(reservaCheckIn), customNoches), "d MMM", { locale: es })}{" "}
                    ({customNoches} {customNoches === 1 ? "noche" : "noches"})
                  </p>
                </div>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="ghost" onClick={handleClose}>Cancelar</Button>
          <Button
            onClick={handleConfirm}
            disabled={selected.length === 0 || isPending}
            className="bg-emerald-700 hover:bg-emerald-800"
          >
            {isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            Agregar ({selected.length})
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default AddGuestModal;
