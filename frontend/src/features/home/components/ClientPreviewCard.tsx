import { User, FileText, Phone, CheckCircle, Loader2 } from "lucide-react";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";
import { Button } from "@/components/ui/button";

interface Props {
  client: ClienteResponse;
  onLink: () => void;
  isLinking: boolean;
  onBack: () => void;
}

export default function ClientPreviewCard({
  client,
  onLink,
  isLinking,
  onBack,
}: Props) {
  return (
    <div className="space-y-5">
      <div className="text-center">
        <div
          className="
            mx-auto mb-3 size-12 rounded-full
            bg-emerald-100 dark:bg-emerald-900/30
            flex items-center justify-center
          "
        >
          <CheckCircle className="size-5 text-emerald-600" />
        </div>
        <h3 className="text-lg font-semibold text-stone-800 dark:text-stone-200">
          Cliente encontrado
        </h3>
        <p className="text-sm text-stone-500 dark:text-stone-400 mt-1">
          Ya existe un registro con tus datos. ¿Deseas vincularlo a tu cuenta?
        </p>
      </div>

      <div
        className="
          rounded-xl border border-stone-200 dark:border-stone-800
          bg-stone-50 dark:bg-stone-900/50
          p-4 space-y-3
        "
      >
        <div className="flex items-center gap-3 pb-3 border-b border-stone-200 dark:border-stone-800">
          <div
            className="
              size-11 rounded-full
              bg-rose-100 dark:bg-rose-900/30
              flex items-center justify-center
              text-rose-600 dark:text-rose-400
              font-semibold text-sm
            "
          >
            {client.nombreCompleto?.charAt(0) ?? "?"}
            {client.apellidoCompleto?.charAt(0) ?? ""}
          </div>
          <div>
            <p className="font-semibold text-stone-800 dark:text-stone-200">
              {client.nombreCompleto} {client.apellidoCompleto}
            </p>
            <p className="text-xs text-stone-500 dark:text-stone-400">
              ID: {client.id}
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div className="flex items-center gap-2">
            <FileText className="size-3.5 text-stone-400 shrink-0" />
            <span className="text-xs text-stone-600 dark:text-stone-400">
              {client.cedula}
            </span>
          </div>
          {client.telefono && (
            <div className="flex items-center gap-2">
              <Phone className="size-3.5 text-stone-400 shrink-0" />
              <span className="text-xs text-stone-600 dark:text-stone-400">
                {client.telefono}
              </span>
            </div>
          )}
          <div className="flex items-center gap-2">
            <User className="size-3.5 text-stone-400 shrink-0" />
            <span className="text-xs text-stone-600 dark:text-stone-400">
              {client.activo ? "Activo" : "Inactivo"}
            </span>
          </div>
        </div>
      </div>

      <div className="flex gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={onBack}
          disabled={isLinking}
          className="flex-1 h-11 cursor-pointer"
        >
          Volver
        </Button>
        <Button
          type="button"
          onClick={onLink}
          disabled={isLinking}
          className="
            flex-1 h-11
            bg-rose-700 hover:bg-rose-800
            text-white font-semibold
            cursor-pointer
            transition-colors
          "
        >
          {isLinking ? (
            <>
              <Loader2 className="size-4 mr-2 animate-spin" />
              Vinculando...
            </>
          ) : (
            <>
              <CheckCircle className="size-4 mr-2" />
              Vincular cuenta
            </>
          )}
        </Button>
      </div>
    </div>
  );
}
