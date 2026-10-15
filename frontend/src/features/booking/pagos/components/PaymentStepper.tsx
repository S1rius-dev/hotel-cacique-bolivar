import { Check } from "lucide-react";

type Step = {
  number: number;
  label: string;
};

const STEPS: Step[] = [
  { number: 1, label: "Seleccionar habitación" },
  { number: 2, label: "Huéspedes y extras" },
  { number: 3, label: "Pago" },
  { number: 4, label: "Confirmación" },
];

interface Props {
  currentStep?: number;
}

export default function PaymentStepper({ currentStep = 3 }: Props) {
  return (
    <div className="w-full flex flex-col items-center">
      {/* Línea superior decorativa */}
      <div aria-hidden className="hidden sm:block h-px w-24 bg-stone-200 mb-6" />

      {/* Desktop */}
      <nav
        aria-label="Progreso de reserva"
        className="hidden sm:flex items-center justify-center w-full max-w-3xl"
      >
        {STEPS.map((step, idx) => {
          const isCompleted = step.number < currentStep;
          const isCurrent = step.number === currentStep;
          const isLast = idx === STEPS.length - 1;

          return (
            <div key={step.number} className="flex items-center">
              <div className="flex items-center gap-2.5">
                <span
                  className={`size-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0 transition-colors
                    ${isCompleted ? "bg-rose-700 text-white" : ""}
                    ${isCurrent ? "bg-rose-700 text-white shadow-md shadow-rose-900/20 ring-4 ring-rose-100" : ""}
                    ${!isCompleted && !isCurrent ? "bg-white border border-stone-200 text-stone-400" : ""}
                  `}
                  aria-current={isCurrent ? "step" : undefined}
                >
                  {isCompleted ? <Check className="size-3.5" strokeWidth={3} /> : step.number}
                </span>
                <span
                  className={`text-sm whitespace-nowrap
                    ${isCompleted ? "text-stone-500 font-medium" : ""}
                    ${isCurrent ? "text-stone-900 font-semibold" : ""}
                    ${!isCompleted && !isCurrent ? "text-stone-400 font-medium" : ""}
                  `}
                >
                  {step.label}
                </span>
              </div>
              {!isLast && (
                <div
                  aria-hidden
                  className={`mx-3 sm:mx-4 h-px w-10 lg:w-14 shrink-0
                    ${step.number < currentStep ? "bg-rose-300" : "bg-stone-200"}
                  `}
                />
              )}
            </div>
          );
        })}
      </nav>

      {/* Mobile */}
      <nav aria-label="Progreso de reserva" className="flex sm:hidden items-center justify-center gap-1.5">
        {STEPS.map((step, idx) => {
          const isCompleted = step.number < currentStep;
          const isCurrent = step.number === currentStep;
          const isLast = idx === STEPS.length - 1;
          return (
            <div key={step.number} className="flex items-center">
              <div
                className={`size-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0
                  ${isCompleted ? "bg-rose-700 text-white" : ""}
                  ${isCurrent ? "bg-rose-700 text-white ring-4 ring-rose-100" : ""}
                  ${!isCompleted && !isCurrent ? "bg-white border border-stone-200 text-stone-400" : ""}
                `}
              >
                {isCompleted ? <Check className="size-3.5" strokeWidth={3} /> : step.number}
              </div>
              {!isLast && (
                <div
                  aria-hidden
                  className={`mx-1.5 h-px w-5 ${step.number < currentStep ? "bg-rose-300" : "bg-stone-200"}`}
                />
              )}
            </div>
          );
        })}
      </nav>
      {/* Mobile labels */}
      <div className="sm:hidden mt-2 flex items-center justify-center">
        <span className="text-xs font-semibold text-stone-900">
          {STEPS.find((s) => s.number === currentStep)?.label}
        </span>
        <span className="text-xs text-stone-400 ml-1">· Paso {currentStep} de 4</span>
      </div>
    </div>
  );
}
