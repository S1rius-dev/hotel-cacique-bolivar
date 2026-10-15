import { useId, type InputHTMLAttributes } from "react";

interface PasswordFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  hint?: string;
}

export function PasswordField({
  label,
  error,
  hint,
  ...props
}: PasswordFieldProps) {
  const id = useId();
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined;

  return (
    <div className="min-w-0">
      <label
        htmlFor={id}
        className="mb-1.5 block text-xs font-medium text-[#6B6058]">
        {label}
      </label>
      <input
        id={id}
        type="password"
        aria-invalid={!!error}
        aria-describedby={describedBy}
        className={`h-10 w-full min-w-0 rounded-[10px] border bg-white px-3 text-sm text-[#241F1D] outline-none transition placeholder:text-[#B5ABA1] focus:border-[#7A1440] focus:ring-3 focus:ring-[#7A1440]/12 ${
          error ? "border-[#B42318]" : "border-[#D9D0C5]"
        }`}
        {...props}
      />
      {error ? (
        <p id={`${id}-error`} className="mt-1 text-xs text-[#B42318]">
          {error}
        </p>
      ) : hint ? (
        <p id={`${id}-hint`} className="mt-1 text-xs text-[#8A7F76]">
          {hint}
        </p>
      ) : null}
    </div>
  );
}

interface SwitchProps {
  checked: boolean;
  onChange: () => void;
  label: string;
}

export function Switch({ checked, onChange, label }: SwitchProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      onClick={onChange}
      className={`relative inline-flex h-6 w-[42px] shrink-0 cursor-pointer items-center rounded-full transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#7A1440] ${
        checked ? "bg-[#7A1440]" : "bg-[#D9D0C5]"
      }`}>
      <span
        className={`block size-5 rounded-full bg-white shadow-sm transition-transform ${
          checked ? "translate-x-[20px]" : "translate-x-0.5"
        }`}
      />
    </button>
  );
}
