import { AlertCircle } from "lucide-react";

interface Props {
  message?: string;
}

function FormGlobalError({ message }: Props) {
  if (!message) return null;

  return (
    <div className="flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm shadow-sm dark:border-red-900/50 dark:bg-red-950/40">
      <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-red-600 dark:text-red-400" />

      <div>
        <p className="font-medium text-red-700 dark:text-red-300">
          Ocurrió un problema
        </p>

        <p className="mt-0.5 text-red-600 dark:text-red-400">{message}</p>
      </div>
    </div>
  );
}

export default FormGlobalError;
