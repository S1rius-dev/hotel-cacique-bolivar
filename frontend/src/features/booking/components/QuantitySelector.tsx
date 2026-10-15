import { Minus, Plus } from "lucide-react";

interface Props {
  value: number;
  onChange: (value: number) => void;
  min?: number;
  max?: number;
  disabled?: boolean;
}

export default function QuantitySelector({
  value,
  onChange,
  min = 0,
  max = 10,
  disabled = false,
}: Props) {
  return (
    <div className="inline-flex items-center gap-3">
      <button
        type="button"
        onClick={() => onChange(Math.max(min, value - 1))}
        disabled={disabled || value <= min}
        className="
          size-9 rounded-full
          flex items-center justify-center
          border border-stone-200
          text-stone-500
          transition-all duration-200
          hover:border-rose-300 hover:text-rose-600 hover:bg-rose-50
          disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:border-stone-200
          disabled:hover:bg-transparent disabled:hover:text-stone-500
          cursor-pointer
        "
      >
        <Minus className="size-3.5" />
      </button>

      <span
        className="
          min-w-[2ch] text-center
          text-lg font-semibold tabular-nums
          text-stone-800
          leading-none
        "
      >
        {value}
      </span>

      <button
        type="button"
        onClick={() => onChange(Math.min(max, value + 1))}
        disabled={disabled || value >= max}
        className="
          size-9 rounded-full
          flex items-center justify-center
          border border-stone-200
          text-stone-500
          transition-all duration-200
          hover:border-rose-300 hover:text-rose-600 hover:bg-rose-50
          disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:border-stone-200
          disabled:hover:bg-transparent disabled:hover:text-stone-500
          cursor-pointer
        "
      >
        <Plus className="size-3.5" />
      </button>
    </div>
  );
}
