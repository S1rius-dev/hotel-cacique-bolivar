import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Dialog } from "@/components/ui/dialog";
import {
  PublicDialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/public-dialog";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { pqrsSchema, type PQRSFormValues } from "../schemas/pqrs.schema";
import { TIPOS_PQRS, type TipoPqrsConfig } from "../constants";
import PQRSConfirmation from "./PQRSConfirmation";
import { useCreatePqrs } from "../hooks/useCreatePqrs";
import { Loader2, FileUp } from "lucide-react";

interface PQRSDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

type Step = "select" | "form" | "done";

export default function PQRSDialog({ open, onOpenChange }: PQRSDialogProps) {
  const [step, setStep] = useState<Step>("select");
  const [selectedTipo, setSelectedTipo] = useState<TipoPqrsConfig | null>(null);
  const [radicado, setRadicado] = useState("");

  const form = useForm<PQRSFormValues>({
    resolver: zodResolver(pqrsSchema),
    defaultValues: {
      tipo: "PETICION",
      asunto: "",
      descripcion: "",
      reservaId: "",
      nombreContacto: "",
      emailContacto: "",
      telefonoContacto: "",
    },
  });

  const createMutation = useCreatePqrs(form);

  const handleReset = () => {
    form.reset();
    setStep("select");
    setSelectedTipo(null);
    setRadicado("");
  };

  const handleClose = () => {
    handleReset();
    onOpenChange(false);
  };

  const handleSelectTipo = (config: TipoPqrsConfig) => {
    setSelectedTipo(config);
    form.setValue("tipo", config.value);
    setStep("form");
  };

  const onSubmit = async (data: PQRSFormValues) => {
    try {
      const result = await createMutation.mutateAsync(data);
      setRadicado(result.codigo);
      setStep("done");
    } catch {
      // Error handled by mutation onError
    }
  };

  if (!open) return null;

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) handleClose(); }}>
      <PublicDialogContent className={`${step === "done" ? "sm:max-w-xs" : "sm:max-w-2xl"} max-h-[90vh] overflow-y-auto p-6`}>
        {step === "select" && (
          <>
            <DialogHeader className="mb-4">
              <DialogTitle className="font-heading text-lg">
                ¿Qué tipo de solicitud deseas crear?
              </DialogTitle>
              <DialogDescription>
                Selecciona una opción para comenzar
              </DialogDescription>
            </DialogHeader>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {TIPOS_PQRS.map((config) => {
                const Icon = config.icon;
                return (
                  <button
                    key={config.value}
                    type="button"
                    onClick={() => handleSelectTipo(config)}
                    className="group flex flex-col items-center gap-2 rounded-xl border border-stone-200 bg-white p-4 text-center transition-all duration-200 hover:border-rose-200 hover:shadow-sm active:scale-[0.98]"
                  >
                    <span
                      className={`flex size-9 items-center justify-center rounded-lg ${config.bgColor} ${config.color}`}
                    >
                      <Icon className="size-4" />
                    </span>
                    <div>
                      <p className="text-sm font-semibold text-stone-800">
                        {config.label}
                      </p>
                      <p className="text-[11px] text-stone-500 leading-tight mt-0.5">
                        {config.subtitle}
                      </p>
                    </div>
                  </button>
                );
              })}
            </div>
          </>
        )}

        {step === "form" && selectedTipo && (
          <>
            <DialogHeader className="mb-4">
              <span
                className={`flex size-9 items-center justify-center rounded-lg ${selectedTipo.bgColor} ${selectedTipo.color} mb-1`}
              >
                <selectedTipo.icon className="size-4" />
              </span>
              <DialogTitle className="font-heading text-lg">
                Crear {selectedTipo.label}
              </DialogTitle>
              <DialogDescription>
                Cuéntanos en detalle tu solicitud. Los campos marcados con * son obligatorios.
              </DialogDescription>
            </DialogHeader>

            <Form {...form}>
              <form
                onSubmit={form.handleSubmit(onSubmit)}
                className="space-y-4"
              >
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 min-w-0">
                  <FormField
                    name="asunto"
                    render={({ field }) => (
                      <FormItem className="sm:col-span-2 min-w-0">
                        <FormLabel>
                          Asunto <span className="text-red-500">*</span>
                        </FormLabel>
                        <FormControl>
                          <Input
                            placeholder="Ej: Solicito información sobre..."
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    name="descripcion"
                    render={({ field }) => (
                      <FormItem className="sm:col-span-2 min-w-0">
                        <FormLabel>
                          Descripción <span className="text-red-500">*</span>
                        </FormLabel>
                        <FormControl>
                          <Textarea
                            placeholder="Cuéntanos en detalle lo que sucedió..."
                            className="min-h-28 resize-y"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    name="reservaId"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Número de reserva (opcional)</FormLabel>
                        <FormControl>
                          <Input
                            placeholder="Ej: 12345"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    name="nombreContacto"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Nombre de contacto (opcional)</FormLabel>
                        <FormControl>
                          <Input placeholder="Tu nombre" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    name="emailContacto"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Email de contacto (opcional)</FormLabel>
                        <FormControl>
                          <Input
                            type="email"
                            placeholder="correo@ejemplo.com"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    name="telefonoContacto"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Teléfono de contacto (opcional)</FormLabel>
                        <FormControl>
                          <Input placeholder="+57 300 123 4567" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <div className="flex items-center justify-center rounded-xl border border-dashed border-stone-300 p-4 transition-colors hover:border-rose-300">
                    <div className="text-center">
                      <FileUp className="mx-auto size-5 text-stone-400" />
                      <p className="mt-1 text-[11px] text-stone-500">
                        Adjuntar archivo (opcional)
                      </p>
                      <p className="text-[10px] text-stone-400">
                        Próximamente
                      </p>
                    </div>
                  </div>
                </div>

                <p className="text-xs text-stone-400 leading-relaxed">
                  Al enviar esta solicitud, aceptas nuestros{" "}
                  <a
                    href="/terminos"
                    target="_blank"
                    className="text-rose-600 underline underline-offset-2 hover:text-rose-700"
                  >
                    Términos y Condiciones
                  </a>{" "}
                  y{" "}
                  <a
                    href="/privacidad"
                    target="_blank"
                    className="text-rose-600 underline underline-offset-2 hover:text-rose-700"
                  >
                    Política de Privacidad
                  </a>
                  .
                </p>

                <div className="flex justify-end gap-3 pt-2">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setStep("select")}
                  >
                    Volver
                  </Button>
                  <Button
                    type="submit"
                    variant="hotel"
                    disabled={createMutation.isPending}
                  >
                    {createMutation.isPending ? (
                      <>
                        <Loader2 className="size-4 animate-spin" />
                        Enviando...
                      </>
                    ) : (
                      "Enviar solicitud"
                    )}
                  </Button>
                </div>
              </form>
            </Form>
          </>
        )}

        {step === "done" && (
          <PQRSConfirmation
            radicado={radicado}
            tipoLabel={selectedTipo?.label ?? ""}
            onCreateAnother={handleReset}
            onClose={handleClose}
          />
        )}
      </PublicDialogContent>
    </Dialog>
  );
}
