import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";

import { Button } from "@/components/ui/button";
import { MoreHorizontal } from "lucide-react";

export type ActionItem<T> = {
  label: string;
  onClick: (row: T) => void;
  variant?: "default" | "destructive";
  icon?: React.ElementType;
};

interface Props<T> {
  row: T;
  actions: ActionItem<T>[];
}

export function DataTableActions<T>({ row, actions }: Props<T>) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon">
          <MoreHorizontal />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end">
        {actions.map((action, index) => {
          const Icon = action.icon;

          return (
            <DropdownMenuItem
              key={index}
              onClick={() => action.onClick(row)}
              className={
                action.variant === "destructive" ? "text-red-600" : ""
              }>
              {Icon && <Icon className="mr-2 h-4 w-4" />}
              {action.label}
            </DropdownMenuItem>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
