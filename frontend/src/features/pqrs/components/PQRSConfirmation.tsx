import { CheckCircle2, FileText, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";

interface PQRSConfirmationProps {
  radicado: string;
  tipoLabel: string;
  onCreateAnother: () => void;
  onClose: () => void;
}

export default function PQRSConfirmation({
  radicado,
  tipoLabel,
  onCreateAnother,
  onClose,
}: PQRSConfirmationProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <div className="mb-6 flex size-16 items-center justify-center rounded-full bg-emerald-100">
        <CheckCircle2 className="size-8 text-emerald-600" />
      </div>

      <h2 className="font-heading text-xl font-semibold text-stone-800">
        ¡Solicitud recibida!
      </h2>

      <div className="mt-3 rounded-xl border border-rose-100 bg-rose-50/50 px-4 py-2">
        <p className="text-xs text-stone-500">N° de radicado</p>
        <p className="font-mono text-sm font-semibold text-rose-700">
          {radicado}
        </p>
      </div>

      <div className="mt-4 space-y-2 text-sm text-stone-500">
        <p>Hemos registrado tu {tipoLabel.toLowerCase()} correctamente.</p>
        <p>
          Te responderemos en un máximo de{" "}
          <strong className="text-stone-700">48 horas hábiles</strong>.
        </p>
        <p className="text-xs text-stone-400">
          Hemos enviado una copia al correo asociado a tu cuenta.
        </p>
      </div>

      <div className="mt-8 flex w-full flex-row flex-wrap gap-3 justify-center">
        <Button variant="hotel" onClick={onCreateAnother}>
          <FileText className="size-4" />
          Crear otra solicitud
        </Button>
        <Button variant="outline" onClick={onClose}>
          <ArrowLeft className="size-4" />
          Volver
        </Button>
      </div>
    </div>
  );
}
