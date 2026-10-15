import * as React from "react";
import { cn } from "@/lib/utils";
import {
  Popover,
  PopoverAnchor,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";

function PublicPopoverContent({
  className,
  ...props
}: React.ComponentProps<typeof PopoverContent>) {
  return (
    <PopoverContent
      className={cn(
        "force-light bg-white text-stone-900 border-stone-200",
        className
      )}
      {...props}
    />
  );
}

export { Popover, PopoverAnchor, PublicPopoverContent, PopoverTrigger };
