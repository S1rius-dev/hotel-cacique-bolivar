import { useState } from "react";
import { CalendarIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import { formatDateShort } from "@/utils/date";

export interface DateRange {
  from: Date | undefined;
  to: Date | undefined;
}

interface DateRangePickerProps {
  value?: DateRange;
  onChange: (range: DateRange | undefined) => void;
  className?: string;
}

export function DateRangePicker({ value, onChange, className }: DateRangePickerProps) {
  const [open, setOpen] = useState(false);

  const label = !value?.from
    ? "Todas las fechas"
    : !value.to
      ? formatDateShort(value.from)
      : `${formatDateShort(value.from)} - ${formatDateShort(value.to)}`;

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          size="sm"
          className={cn(
            "h-8 min-w-[175px] text-xs font-normal gap-1.5 justify-start",
            !value?.from && "text-muted-foreground",
            className,
          )}
        >
          <CalendarIcon className="w-4 h-4 shrink-0" />
          <span className="truncate">{label}</span>
        </Button>
      </PopoverTrigger>
      <PopoverContent
        className="w-auto p-0"
        align="start"
        style={{ width: "auto", maxWidth: "none", overflow: "visible" }}
      >
        <Calendar
          mode="range"
          selected={value}
          onSelect={(range) => {
            onChange(range as DateRange | undefined);
            if (range?.from && range?.to) {
              setTimeout(() => setOpen(false), 200);
            }
          }}
        />
      </PopoverContent>
    </Popover>
  );
}
