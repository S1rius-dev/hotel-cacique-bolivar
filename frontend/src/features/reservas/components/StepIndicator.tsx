"use client";

import { CheckIcon } from "lucide-react";

interface StepIndicatorProps {
  currentStep: number;
}

const steps = [
  { number: 1, label: "Reserva" },
  { number: 2, label: "Confirmación" },
];

const StepIndicator: React.FC<StepIndicatorProps> = ({ currentStep }) => {
  return (
    <div className="flex justify-center py-2">
      <div className="inline-flex items-center rounded-xl bg-zinc-100 dark:bg-zinc-800/60 p-1 gap-1">
        {steps.map((step) => {
          const isCompleted = step.number < currentStep;
          const isCurrent = step.number === currentStep;

          return (
            <div
              key={step.number}
              className={`
                flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium
                transition-all duration-300
                ${
                  isCompleted
                    ? "bg-rose-600 text-white shadow-sm"
                    : isCurrent
                      ? "bg-white dark:bg-zinc-900 text-zinc-900 dark:text-white shadow-sm border border-zinc-200 dark:border-zinc-700"
                      : "text-zinc-400 dark:text-zinc-500"
                }
              `}
            >
              <span
                className={`
                  size-5 rounded-full flex items-center justify-center text-[11px] font-bold
                  ${
                    isCompleted
                      ? "bg-white/20 text-white"
                      : isCurrent
                        ? "bg-rose-800 text-white"
                        : "bg-zinc-200 dark:bg-zinc-700 text-zinc-500"
                  }
                `}
              >
                {isCompleted ? <CheckIcon className="size-3" /> : step.number}
              </span>
              <span>{step.label}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default StepIndicator;
