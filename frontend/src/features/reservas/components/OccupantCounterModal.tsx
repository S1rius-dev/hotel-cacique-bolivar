"use client";

import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { MinusIcon, PlusIcon, BedDoubleIcon, Info } from "lucide-react";
import { formatPrice } from "../utils/room.utils";

interface OccupantCounterModalProps {
  open: boolean;
  roomNumber: string;
  roomType: string;
  capacidadMax: number;
  capacidadBase: number;
  precioPersonaExtra: number;
  defaultAdults: number;
  onConfirm: (adultos: number, ninos: number) => void;
  onCancel: () => void;
}

export const OccupantCounterModal: React.FC<OccupantCounterModalProps> = ({
  open,
  roomNumber,
  roomType,
  capacidadMax,
  capacidadBase,
  precioPersonaExtra,
  defaultAdults,
  onConfirm,
  onCancel,
}) => {
  const [adultos, setAdultos] = useState(Math.max(defaultAdults, 1));
  const [ninos, setNinos] = useState(0);

  const total = adultos + ninos;
  const personasExtras = Math.max(0, total - capacidadBase);
  const exceedsCapacity = total > capacidadMax;
  const canConfirm = total > 0 && !exceedsCapacity;

  return (
    <Dialog open={open} onOpenChange={(open) => !open && onCancel()}>
      <DialogContent className="sm:max-w-xs">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <BedDoubleIcon className="size-4 text-muted-foreground" />
            <DialogTitle>Habitación #{roomNumber}</DialogTitle>
          </div>
          <DialogDescription>{roomType}</DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2">
          <CounterRow
            label="Adultos"
            value={adultos}
            min={1}
            onChange={setAdultos}
          />
          <CounterRow
            label="Niños"
            value={ninos}
            min={0}
            onChange={setNinos}
          />

          <div className="flex items-center justify-between border-t pt-3">
            <span className="text-sm text-muted-foreground">
              Total ocupantes
            </span>
            <span className="font-semibold">{total}</span>
          </div>

          {personasExtras > 0 && (
            <div className="flex items-center gap-2 text-xs text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/20 rounded-lg px-3 py-2">
              <Info className="size-3.5 shrink-0" />
              <span>
                {personasExtras} persona(s) extra × {formatPrice(precioPersonaExtra)}
              </span>
            </div>
          )}

          {exceedsCapacity && (
            <p className="text-xs text-rose-600">
              Supera la capacidad máxima ({capacidadMax} personas)
            </p>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onCancel}>
            Cancelar
          </Button>
          <Button
            onClick={() => onConfirm(adultos, ninos)}
            disabled={!canConfirm}>
            Agregar
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

interface CounterRowProps {
  label: string;
  value: number;
  min: number;
  onChange: (val: number) => void;
}

const CounterRow: React.FC<CounterRowProps> = ({
  label,
  value,
  min,
  onChange,
}) => (
  <div className="flex items-center justify-between">
    <span className="text-sm">{label}</span>
    <div className="flex items-center gap-3">
      <button
        onClick={() => onChange(Math.max(min, value - 1))}
        disabled={value <= min}
        className="size-7 rounded-full border flex items-center justify-center disabled:opacity-30 transition-colors hover:bg-muted">
        <MinusIcon className="size-3.5" />
      </button>
      <span className="w-6 text-center font-medium">{value}</span>
      <button
        onClick={() => onChange(value + 1)}
        className="size-7 rounded-full border flex items-center justify-center transition-colors hover:bg-muted">
        <PlusIcon className="size-3.5" />
      </button>
    </div>
  </div>
);
