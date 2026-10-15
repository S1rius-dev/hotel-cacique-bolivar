import { useState } from "react";
import { DatabaseZap, Loader2, ShieldAlert, X } from "lucide-react";
import { Dialog } from "@/components/ui/dialog";
import {
  PublicDialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/public-dialog";

/** Mismo criterio que el backend: letras, digitos y guion bajo. */
const NOMBRE_VALIDO = /^[A-Za-z0-9_]{1,64}$/;

interface Props {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  archivoNombre?: string;
  baseEnUso?: string;
  onConfirm: (baseDestino: string) => void;
  isPending?: boolean;
}

/**
 * El formulario vive en un componente aparte que solo se monta cuando el
 * dialogo esta abierto. Asi el nombre propuesto arranca vacio en cada apertura
 * (reutilizar el del intento anterior apuntaria a una base ya clonada) sin
 * needing un efecto que resetee el estado.
 */
function CloneBackupForm({
  onOpenChange,
  archivoNombre,
  baseEnUso,
  onConfirm,
  isPending,
}: Omit<Props, "open">) {
  const [base, setBase] = useState("");

  const esBaseEnUso =
    Boolean(baseEnUso) && base.trim().toLowerCase() === baseEnUso?.toLowerCase();
  const valido = NOMBRE_VALIDO.test(base.trim()) && !esBaseEnUso;

  const confirmar = () => {
    if (!valido) return;
    onConfirm(base.trim());
  };

  return (
    <PublicDialogContent className="w-[95vw] sm:max-w-lg rounded-2xl p-6">
      <button
        type="button"
        onClick={() => onOpenChange(false)}
        disabled={isPending}
        className="absolute right-4 top-4 cursor-pointer text-stone-400 hover:text-stone-600">
        <X className="size-4" />
      </button>

      <div className="flex flex-col items-center gap-4">
        <div className="flex size-12 items-center justify-center rounded-full bg-amber-50">
          <DatabaseZap className="size-6 text-amber-600" />
        </div>

        <DialogHeader>
          <DialogTitle>Restaurar en una base nueva</DialogTitle>
          <DialogDescription>
            Se crea una base de datos nueva a partir de{" "}
            <strong className="font-mono text-xs">{archivoNombre}</strong>
          </DialogDescription>
        </DialogHeader>

        <div className="w-full rounded-xl border border-amber-200 bg-amber-50 p-3 text-left">
          <div className="flex gap-2">
            <ShieldAlert className="mt-0.5 size-4 shrink-0 text-amber-600" />
            <p className="text-xs leading-relaxed text-amber-900">
              La base en uso <strong>{baseEnUso}</strong> no se toca en ningun
              momento. El clon es util para inspeccionar o comparar datos antes
              de decidir, y la base original se conserva intacta.
            </p>
          </div>
        </div>

        <div className="w-full text-left">
          <label className="mb-1.5 block text-xs font-medium text-stone-500 uppercase tracking-wider">
            Nombre de la base destino
          </label>
          <input
            type="text"
            value={base}
            onChange={(e) => setBase(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") confirmar();
            }}
            disabled={isPending}
            placeholder="hotel_restaurada_2026_09_26"
            autoFocus
            className="w-full rounded-xl border border-stone-200 px-4 py-2.5 font-mono text-sm text-stone-700 transition placeholder:text-stone-300 focus:border-amber-300 focus:ring-2 focus:ring-amber-100 disabled:opacity-50"
          />

          {base.trim() !== "" && !NOMBRE_VALIDO.test(base.trim()) && (
            <p className="mt-1.5 text-xs text-rose-600">
              Solo letras, digitos y guion bajo, hasta 64 caracteres.
            </p>
          )}

          {esBaseEnUso && (
            <p className="mt-1.5 text-xs text-rose-600">
              Ese es el nombre de la base en uso. Elige otro: restaurar sobre
              ella no esta permitido.
            </p>
          )}
        </div>

        {isPending && (
          <p className="w-full rounded-xl bg-stone-50 p-3 text-left text-xs leading-relaxed text-stone-600">
            Verificando el checksum e importando el archivo. Puede tardar
            varios minutos segun el tamano de la base; no cierres esta pestana.
          </p>
        )}

        <div className="flex w-full gap-3 pt-2">
          <button
            type="button"
            onClick={() => onOpenChange(false)}
            disabled={isPending}
            className="flex-1 cursor-pointer rounded-xl border border-stone-200 py-2.5 text-sm font-medium text-stone-600 transition hover:bg-stone-50">
            Cancelar
          </button>
          <button
            type="button"
            onClick={confirmar}
            disabled={!valido || isPending}
            className="flex-1 inline-flex cursor-pointer items-center justify-center gap-2 rounded-xl bg-amber-700 py-2.5 text-sm font-medium text-white transition hover:bg-amber-800 disabled:opacity-50">
            {isPending ? (
              <>
                <Loader2 className="size-4 animate-spin" />
                Restaurando...
              </>
            ) : (
              "Restaurar"
            )}
          </button>
        </div>
      </div>
    </PublicDialogContent>
  );
}

export default function CloneBackupDialog({ open, ...rest }: Props) {
  return (
    <Dialog open={open} onOpenChange={rest.onOpenChange}>
      {open && <CloneBackupForm {...rest} />}
    </Dialog>
  );
}
