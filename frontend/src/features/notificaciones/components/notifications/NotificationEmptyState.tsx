import { Bell } from "lucide-react";

export default function NotificationEmptyState() {
  return (
    <div className="flex flex-col items-center py-10 text-muted-foreground">
      <Bell className="size-8 mb-2 opacity-40" />
      <p className="text-sm">No tienes notificaciones</p>
    </div>
  );
}
