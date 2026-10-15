import { useState, type ChangeEvent, type FormEvent } from "react";
import { toast } from "sonner";
import { Check } from "lucide-react";
import { PasswordField, Switch } from "./ui";
import { extractErrorMessage } from "@/lib/extract-error";

interface SecurityTabProps {
  /** Debe lanzar un error si el cambio falla. */
  onChangePassword: (current: string, next: string) => Promise<void>;
  /** Si no se pasa, la verificación en dos pasos se muestra como "Próximamente". */
  twoFactorEnabled?: boolean;
  onTwoFactorChange?: () => void;
}

type PasswordForm = { current: string; next: string; confirm: string };
const EMPTY: PasswordForm = { current: "", next: "", confirm: "" };

export default function SecurityTab({
  onChangePassword,
  twoFactorEnabled = false,
  onTwoFactorChange,
}: SecurityTabProps) {
  const [form, setForm] = useState<PasswordForm>(EMPTY);
  const [errors, setErrors] = useState<Partial<PasswordForm>>({});
  const [saving, setSaving] = useState(false);

  const setField =
    (key: keyof PasswordForm) => (e: ChangeEvent<HTMLInputElement>) => {
      setForm((f) => ({ ...f, [key]: e.target.value }));
      setErrors((prev) => ({ ...prev, [key]: undefined }));
    };

  const validate = () => {
    const next: Partial<PasswordForm> = {};
    if (!form.current) next.current = "Escribe tu contraseña actual";
    if (form.next.length < 8) next.next = "Usa al menos 8 caracteres";
    else if (form.next === form.current)
      next.next = "Elige una contraseña distinta a la actual";
    if (form.confirm !== form.next)
      next.confirm = "Las contraseñas no coinciden";
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (saving || !validate()) return;
    setSaving(true);
    try {
      await onChangePassword(form.current, form.next);
      toast.success("Contraseña actualizada");
      setForm(EMPTY);
    } catch (error) {
      toast.error(
        extractErrorMessage(
          error,
          "No pudimos actualizar tu contraseña. Revisa la actual e inténtalo de nuevo.",
        ),
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="px-6 pb-6 pt-5">
      <form onSubmit={handleSubmit} noValidate className="grid gap-3.5">
        <PasswordField
          label="Contraseña actual"
          autoComplete="current-password"
          placeholder="Tu contraseña actual"
          value={form.current}
          onChange={setField("current")}
          error={errors.current}
        />
        <PasswordField
          label="Nueva contraseña"
          autoComplete="new-password"
          placeholder="Mínimo 8 caracteres"
          value={form.next}
          onChange={setField("next")}
          error={errors.next}
        />
        <PasswordField
          label="Confirmar contraseña"
          autoComplete="new-password"
          placeholder="Repite la nueva contraseña"
          value={form.confirm}
          onChange={setField("confirm")}
          error={errors.confirm}
        />
        <button
          type="submit"
          disabled={saving}
          className="mt-1 w-fit cursor-pointer rounded-full border border-[#D9D0C5] bg-white px-5 py-2 text-sm font-medium text-[#241F1D] transition hover:bg-[#FAF7F2] disabled:cursor-wait disabled:opacity-60">
          {saving ? "Guardando…" : "Guardar contraseña"}
        </button>
      </form>

      <div className="mt-5 flex items-center justify-between gap-4 border-t border-[#EFEAE3] py-4">
        <div>
          <h3 className="text-sm font-medium text-[#241F1D]">
            Verificación en dos pasos
          </h3>
          <p className="mt-0.5 text-xs text-[#8A7F76]">
            Pide un código al iniciar sesión.
          </p>
        </div>
        {onTwoFactorChange ? (
          <Switch
            checked={twoFactorEnabled}
            onChange={onTwoFactorChange}
            label="Verificación en dos pasos"
          />
        ) : (
          <span className="rounded-full bg-[#F5F0EA] px-2.5 py-1 text-xs text-[#6B6058]">
            Próximamente
          </span>
        )}
      </div>

      <div className="flex items-center justify-between gap-4 border-t border-[#EFEAE3] pt-4">
        <div>
          <h3 className="text-sm font-medium text-[#241F1D]">
            Este dispositivo
          </h3>
          <p className="mt-0.5 text-xs text-[#8A7F76]">
            Navegador actual, activo ahora.
          </p>
        </div>
        <span className="flex items-center gap-1 rounded-full bg-[#EEF1EA] px-2.5 py-1 text-xs text-[#4A5A3F]">
          <Check className="size-3" aria-hidden="true" /> Actual
        </span>
      </div>
    </div>
  );
}
