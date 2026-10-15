import { format } from "date-fns";
import { es } from "date-fns/locale";
import { CalendarIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";

interface Props {
  checkIn: Date;
  checkOut: Date;
  onCheckInChange: (d: Date) => void;
  onCheckOutChange: (d: Date) => void;
}

function QuickReservaDatePicker({ checkIn, checkOut, onCheckInChange, onCheckOutChange }: Props) {
  return (
    <div className="grid grid-cols-1 gap-1.5">
      <div className="space-y-1.5">
        <label className="text-xs font-medium text-muted-foreground">
          Check-in
        </label>
        <Popover>
          <PopoverTrigger asChild>
            <Button
              variant="outline"
              className="w-full justify-start text-left font-normal"
            >
              <CalendarIcon className="mr-2 h-4 w-4" />
              {format(checkIn, "dd/MM/yyyy", { locale: es })}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="!w-auto p-0" align="start">
            <Calendar
              mode="single"
              selected={checkIn}
              onSelect={(d) => d && onCheckInChange(d)}
              locale={es}
            />
          </PopoverContent>
        </Popover>
      </div>
      <div className="space-y-1.5">
        <label className="text-xs font-medium text-muted-foreground">
          Check-out
        </label>
        <Popover>
          <PopoverTrigger asChild>
            <Button
              variant="outline"
              className="w-full justify-start text-left font-normal"
            >
              <CalendarIcon className="mr-2 h-4 w-4" />
              {format(checkOut, "dd/MM/yyyy", { locale: es })}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="!w-auto p-0" align="start">
            <Calendar
              mode="single"
              selected={checkOut}
              onSelect={(d) => d && onCheckOutChange(d)}
              disabled={{ before: checkIn }}
              locale={es}
            />
          </PopoverContent>
        </Popover>
      </div>
    </div>
  );
}

export default QuickReservaDatePicker;
