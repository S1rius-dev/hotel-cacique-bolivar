import { useEffect } from "react";
import { useForm, useWatch, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { CalendarIcon } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { DatePicker } from "@/components/ui/date-picker";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import {
  reservationBlockSchema,
  resolveBlockMotivo,
  type ReservationBlockFormValues,
  type BlockFormValues,
} from "../schemas/block.schema";
import type { BookingBlock } from "../types";
import { BLOCK_MOTIVOS } from "./constants";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSave: (data: BlockFormValues, id?: number) => void;
  editBlock?: BookingBlock | null;
  initialDates?: { fechaInicio: Date; fechaFin: Date } | null;
  isPending?: boolean;
}

function resolveInitialValues(
  editBlock?: BookingBlock | null,
  initialDates?: Props["initialDates"],
): ReservationBlockFormValues {
  if (editBlock) {
    const isPreset = BLOCK_MOTIVOS.includes(
      editBlock.motivo as (typeof BLOCK_MOTIVOS)[number],
    );
    return {
      fechaInicio: new Date(editBlock.fechaInicio),
      fechaFin: new Date(editBlock.fechaFin),
      tipoMotivo: isPreset ? editBlock.motivo : "Otro",
      customMotivo: isPreset ? "" : editBlock.motivo,
      activo: editBlock.activo,
    };
  }
  const defaults = {
    fechaInicio: initialDates?.fechaInicio ?? new Date(),
    fechaFin: initialDates?.fechaFin ?? new Date(),
    tipoMotivo: "",
    customMotivo: "",
    activo: true,
  };
  return defaults;
}

export function ReservationBlockModal({
  open,
  onOpenChange,
  onSave,
  editBlock,
  initialDates,
  isPending,
}: Props) {
  const isEditing = !!editBlock;

  const form = useForm<ReservationBlockFormValues>({
    resolver: zodResolver(reservationBlockSchema),
    defaultValues: resolveInitialValues(editBlock, initialDates),
  });

  useEffect(() => {
    if (open) {
      form.reset(resolveInitialValues(editBlock, initialDates));
    }
  }, [open, editBlock, initialDates, form]);

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const tipoMotivo = useWatch({ control: form.control, name: "tipoMotivo" });
  const fechaInicioValue = useWatch({ control: form.control, name: "fechaInicio" });
  const fechaFinValue = useWatch({ control: form.control, name: "fechaFin" });
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  const hasPastDates =
    !!fechaFinValue && fechaFinValue.getTime() < hoy.getTime();

  const handleSubmit = (values: ReservationBlockFormValues) => {
    onSave(
      {
        fechaInicio: values.fechaInicio,
        fechaFin: values.fechaFin,
        motivo: resolveBlockMotivo(values),
        activo: values.activo,
      },
      editBlock?.id,
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            {isEditing ? "Editar bloqueo" : "Nuevo bloqueo"}
          </DialogTitle>
          <DialogDescription>
            {isEditing
              ? "Modifica los datos del bloqueo de reservas web."
              : "Crea un nuevo bloqueo para impedir reservas desde la página web."}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(handleSubmit)}
            className="space-y-5">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Controller
                control={form.control}
                name="fechaInicio"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Fecha inicio *</FormLabel>
                    <FormControl>
                      <DatePicker
                        value={field.value}
                        onChange={field.onChange}
                        placeholder="Seleccionar fecha"
                        className="w-full"
                        minDate={today}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <Controller
                control={form.control}
                name="fechaFin"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Fecha fin *</FormLabel>
                    <FormControl>
                      <DatePicker
                        value={field.value}
                        onChange={field.onChange}
                        placeholder="Seleccionar fecha"
                        className="w-full"
                        minDate={fechaInicioValue ?? today}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <Controller
              control={form.control}
              name="tipoMotivo"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Motivo *</FormLabel>
                  <FormControl>
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger className="w-full">
                        <SelectValue placeholder="Selecciona un motivo" />
                      </SelectTrigger>
                      <SelectContent>
                        {BLOCK_MOTIVOS.map((motivo) => (
                          <SelectItem key={motivo} value={motivo}>
                            {motivo}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {tipoMotivo === "Otro" && (
              <Controller
                control={form.control}
                name="customMotivo"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Especifica el motivo</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Describe el motivo del bloqueo"
                        className="min-h-[80px] resize-none"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}

            <Controller
              control={form.control}
              name="activo"
              render={({ field }) => (
                <FormItem>
                  <div className="flex items-center gap-3 rounded-lg border p-3">
                    <FormControl>
                      <Switch
                        checked={field.value}
                        onCheckedChange={field.onChange}
                        disabled={hasPastDates && !field.value}
                        className="cursor-pointer"
                      />
                    </FormControl>
                    <div className="space-y-0.5">
                      <FormLabel className="text-sm font-medium leading-none">
                        Estado
                      </FormLabel>
                      <p className="text-xs text-muted-foreground">
                        {field.value
                          ? "El bloqueo está vigente y afecta las reservas web"
                          : hasPastDates
                            ? "No se puede activar un bloqueo con fechas ya finalizadas"
                            : "El bloqueo está desactivado y no afecta las reservas"}
                      </p>
                    </div>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <DialogClose asChild>
                <Button
                  type="button"
                  variant="outline"
                  className="cursor-pointer"
                  disabled={isPending}>
                  Cancelar
                </Button>
              </DialogClose>
              <Button
                type="submit"
                variant="hotel"
                className="cursor-pointer"
                disabled={isPending}>
                <CalendarIcon className="size-4" />
                {isPending
                  ? "Guardando..."
                  : isEditing
                    ? "Guardar cambios"
                    : "Crear bloqueo"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
