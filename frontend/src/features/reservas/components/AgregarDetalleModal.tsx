import { useState } from "react";
import { differenceInCalendarDays, addDays, format } from "date-fns";
import { es } from "date-fns/locale";
import { useResetOnKey } from "@/lib/useResetOnKey";
import { BedDouble, Info, Loader2, Minus, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import { useRoomAvailability } from "../hooks/useRoomAvailability";
import { formatCurrency, formatNumber } from "@/utils/format";
import type { DetalleReservaRequest, DisponibilidadResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  open: boolean;
  onClose: () => void;
  onConfirm: (data: DetalleReservaRequest) => void;
  isPending: boolean;
  existingRoomIds: number[];
  checkIn: string;
  checkOut: string;
  noches: number;
}

function AgregarDetalleModal({ open, onClose, onConfirm, isPending, existingRoomIds, checkIn, checkOut, noches }: Props) {
  const { data: disponibles, isLoading: loadingRooms } = useRoomAvailability(checkIn, checkOut);
  const [selectedRoom, setSelectedRoom] = useState<DisponibilidadResponse | null>(null);
  const [ocupantes, setOcupantes] = useState(1);
  const [obs, setObs] = useState("");
  const [incluyeIva, setIncluyeIva] = useState(false);
  const [useCustomNoches, setUseCustomNoches] = useState(false);
  const [customNoches, setCustomNoches] = useState(1);
  const reservaNoches = checkIn && checkOut ? differenceInCalendarDays(new Date(checkOut), new Date(checkIn)) : 0;

  // Cada apertura arranca en blanco: reutilizar la habitacion del intento
  // anterior dejaria una seleccion fantasma en el formulario.
  useResetOnKey(String(open), () => {
    setSelectedRoom(null);
    setOcupantes(1);
    setObs("");
    setIncluyeIva(false);
    setUseCustomNoches(false);
    setCustomNoches(1);
  });

  const availableRooms = (disponibles ?? []).filter((r) => r.id != null && !existingRoomIds.includes(r.id!));

  const capacidadBase = selectedRoom?.capacidadBase ?? 0;
  const capacidadMaxima = selectedRoom?.capacidadMaxima ?? 99;
  const precioBase = selectedRoom?.precioBase ?? 0;
  const precioPersonaExtra = selectedRoom?.precioPersonaExtra ?? 0;
  const personasExtras = Math.max(0, ocupantes - capacidadBase);
  const subtotalPreview = (precioBase * noches) + (personasExtras * precioPersonaExtra * noches);

  const isOcupantesValido = ocupantes >= 1 && ocupantes <= capacidadMaxima;
  const canSubmit = selectedRoom != null && isOcupantesValido;

  const handleSelect = (room: DisponibilidadResponse) => {
    setSelectedRoom(room);
    const maxOcup = room.capacidadMaxima ?? 99;
    if (ocupantes > maxOcup) setOcupantes(maxOcup);
  };

  const handleConfirm = () => {
    if (!selectedRoom?.id) return;
    onConfirm({
      habitacionId: selectedRoom.id,
      precioPorNoche: selectedRoom.precioBase ?? 0,
      cantidadOcupantes: ocupantes,
      observaciones: obs || undefined,
      precioIncluyeIva: incluyeIva,
    });
  };

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <BedDouble className="w-5 h-5 text-emerald-600" />
            Agregar habitación
          </DialogTitle>
          <DialogDescription>
            Seleccione una habitación disponible para agregar a esta reserva.
          </DialogDescription>
        </DialogHeader>

        <div className="py-2 space-y-4">
          {loadingRooms ? (
            <div className="flex items-center justify-center py-6">
              <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
            </div>
          ) : availableRooms.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-4">
              No hay habitaciones disponibles en el rango de fechas seleccionado.
            </p>
          ) : (
            <div className="max-h-48 overflow-y-auto space-y-1.5">
              {availableRooms.map((room) => {
                const isSelected = selectedRoom?.id === room.id;
                return (
                  <div
                    key={room.id}
                    onClick={() => handleSelect(room)}
                    className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-colors ${
                      isSelected
                        ? "border-emerald-500 bg-emerald-50 dark:bg-emerald-950/20"
                        : "border-border bg-white dark:bg-zinc-950/40 hover:bg-zinc-50 dark:hover:bg-zinc-900/60"
                    }`}
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <BedDouble className="w-4 h-4 text-muted-foreground shrink-0" />
                      <div className="min-w-0">
                        <p className="text-sm font-medium">{room.numero}</p>
                        <p className="text-xs text-muted-foreground">{room.tipoNombre}</p>
                      </div>
                    </div>
                    <div className="text-right shrink-0 ml-2">
                      <p className="text-sm font-semibold">{formatCurrency(room.precioBase)}</p>
                      <p className="text-[10px] text-muted-foreground">
                        Base {room.capacidadBase} · Max {room.capacidadMaxima}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {selectedRoom && (
            <div className="space-y-4 pt-2 border-t border-border">
              <div className="flex items-start gap-3 p-3 rounded-lg bg-zinc-50 dark:bg-zinc-900/40 border text-sm">
                <Info className="w-4 h-4 text-emerald-500 mt-0.5 shrink-0" />
                <div className="text-xs text-muted-foreground space-y-1">
                  <p>
                    <span className="font-medium text-zinc-700 dark:text-zinc-300">Precio base:</span>{" "}
                    {formatCurrency(precioBase)} / noche
                  </p>
                  <p>
                    <span className="font-medium text-zinc-700 dark:text-zinc-300">Capacidad:</span>{" "}
                    {capacidadBase} incluidos + ${formatNumber(precioPersonaExtra)}/persona extra
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-between gap-3 p-3 rounded-lg bg-zinc-50 dark:bg-zinc-900/40 border">
                <div className="min-w-0">
                  <p className="text-sm font-medium">Precio incluye IVA</p>
                  <p className="text-xs text-muted-foreground">
                    Congela el modo de precio desde la creación del detalle.
                  </p>
                </div>
                <Switch
                  checked={incluyeIva}
                  onCheckedChange={setIncluyeIva}
                  aria-label="El precio incluye IVA"
                />
              </div>

              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">
                  Cant. ocupantes *
                </label>
                <input
                  type="number"
                  min={1}
                  max={capacidadMaxima}
                  value={ocupantes}
                  onChange={(e) => setOcupantes(Number(e.target.value))}
                  className={`w-full h-9 rounded-lg border bg-background px-3 text-sm outline-none focus:border-ring focus:ring-[3px] focus:ring-ring/50 ${
                    !isOcupantesValido ? "border-red-400" : ""
                  }`}
                />
                {!isOcupantesValido && (
                  <p className="text-[10px] text-red-500">
                    Máximo {capacidadMaxima} ocupantes
                  </p>
                )}
                {personasExtras > 0 && (
                  <p className="text-[10px] text-amber-600">
                    {personasExtras} persona(s) extra × {formatCurrency(precioPersonaExtra)}/noche
                  </p>
                )}
              </div>

              {checkIn && reservaNoches > 0 && (
                <div className="space-y-2">
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
                        {format(new Date(checkIn), "d MMM", { locale: es })} →{" "}
                        {format(addDays(new Date(checkIn), customNoches), "d MMM", { locale: es })}{" "}
                        ({customNoches} {customNoches === 1 ? "noche" : "noches"})
                      </p>
                    </div>
                  )}
                </div>
              )}

              <div className="space-y-1">
                <label className="text-xs font-medium text-muted-foreground">
                  Observaciones
                </label>
                <input
                  type="text"
                  value={obs}
                  onChange={(e) => setObs(e.target.value)}
                  className="w-full h-9 rounded-lg border bg-background px-3 text-sm outline-none focus:border-ring focus:ring-[3px] focus:ring-ring/50"
                  placeholder="Opcional"
                />
              </div>

              <div className="rounded-lg bg-emerald-50 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-800 px-4 py-3">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-muted-foreground">Subtotal preview</span>
                  <span className="font-bold text-emerald-700 dark:text-emerald-400">
                    {formatCurrency(subtotalPreview)}
                  </span>
                </div>
                <p className="text-[10px] text-muted-foreground mt-1">
                  {formatCurrency(precioBase)} x {noches} noche{noches !== 1 ? "s" : ""}
                  {personasExtras > 0 && (
                    <> + {personasExtras} extra × {formatCurrency(precioPersonaExtra)}</>
                  )}
                </p>
              </div>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="ghost" onClick={onClose}>Cancelar</Button>
          <Button
            onClick={handleConfirm}
            disabled={!canSubmit || isPending}
            className="bg-emerald-700 hover:bg-emerald-800"
          >
            {isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            Agregar habitación
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default AgregarDetalleModal;
