import * as React from "react";
import { cn } from "@/lib/utils";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

// Wrapper for public area: always light, isolated from .dark via .force-light
// Radix Portal renders outside the PublicLayout wrapper, so we must
// re-apply .force-light directly on the dialog content.

function PublicDialogContent({
  className,
  children,
  ...props
}: React.ComponentProps<typeof DialogContent>) {
  return (
    <DialogContent
      className={cn(
        // force-light redeclares all CSS vars to light; ensures bg-popover etc are white
        "force-light bg-white text-stone-900 border-stone-200",
        // override token-based defaults that would be dark inside .dark html
        "[&_[data-slot=dialog-title]]:text-stone-900",
        className
      )}
      {...props}
    >
      {children}
    </DialogContent>
  );
}

export {
  Dialog,
  PublicDialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
};
