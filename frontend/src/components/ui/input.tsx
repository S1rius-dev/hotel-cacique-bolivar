import * as React from "react";
import { cn } from "@/lib/utils";

function Input({ className, type, ...props }: React.ComponentProps<"input">) {
  return (
    <input
      type={type}
      data-slot="input"
      className={cn(
        /* =========================
           BASE MODERNO
        ========================== */
        "flex h-10 w-full min-w-0 rounded-xl",
        "border border-zinc-200 bg-white/70 backdrop-blur-sm",
        "px-3 py-2 text-sm text-zinc-900",

        /* =========================
           TRANSICIONES
        ========================== */
        "transition-all duration-200 ease-out",

        /* =========================
           PLACEHOLDER
        ========================== */
        "placeholder:text-zinc-400",

        /* =========================
           HOVER
        ========================== */
        "hover:border-rose-300",

        /* =========================
           FOCUS (TU IDENTIDAD)
        ========================== */
        "focus-visible:outline-none",
        "focus-visible:border-rose-800/90",
        "focus-visible:ring-4",
        "focus-visible:ring-rose-800/20",

        /* =========================
           DISABLED
        ========================== */
        "disabled:cursor-not-allowed disabled:opacity-50",

        /* =========================
           ERROR STATE
        ========================== */
        "aria-invalid:border-red-500",
        "aria-invalid:ring-4",
        "aria-invalid:ring-red-500/20",

        /* =========================
           DARK MODE (opcional pero pro)
        ========================== */
        "dark:bg-zinc-900/60 dark:border-zinc-700",
        "dark:text-white",
        "dark:placeholder:text-zinc-500",
        "dark:focus-visible:border-rose-800/90",

        className,
      )}
      {...props}
    />
  );
}

export { Input };
