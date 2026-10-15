export function GuestAvatar({ name }: { name: string }) {
  const initials = name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-rose-100 to-rose-200 text-xs font-semibold text-rose-600 ring-2 ring-rose-300/50 dark:from-rose-900/40 dark:to-rose-800/30 dark:text-rose-300">
      {initials}
    </div>
  );
}
