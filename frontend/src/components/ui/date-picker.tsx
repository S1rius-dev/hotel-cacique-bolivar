import { useState } from "react";
import { CalendarIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { cn } from "@/lib/utils";
import { formatDateShort } from "@/utils/date";

interface DatePickerProps {
  value?: Date;
  onChange: (date: Date | undefined) => void;
  placeholder?: string;
  className?: string;
  minDate?: Date;
}

export function DatePicker({ value, onChange, placeholder, className, minDate }: DatePickerProps) {
  const [open, setOpen] = useState(false);

  const label = value ? formatDateShort(value) : (placeholder ?? "Seleccionar fecha");

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          size="sm"
          className={cn(
            "h-8 min-w-[140px] text-xs font-normal gap-1.5 justify-start",
            !value && "text-muted-foreground",
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
          mode="single"
          selected={value}
          onSelect={(date) => {
            onChange(date);
            setTimeout(() => setOpen(false), 200);
          }}
          disabled={minDate ? (date) => date < minDate : undefined}
        />
      </PopoverContent>
    </Popover>
  );
}
