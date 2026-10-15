import type { ReactNode } from "react";

interface Props {
  title: string;
  description?: string;
  icon?: ReactNode;
  children: ReactNode;
}
export function FormCard({ title, description, icon, children }: Props) {
  return (
    <section className="rounded-xl border bg-card shadow-sm">
      {" "}
      <header className="flex items-center gap-3 border-b px-6 py-4">
        {" "}
        {icon && (
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-rose-100">
            {" "}
            {icon}{" "}
          </div>
        )}{" "}
        <div>
          {" "}
          <h2 className="text-lg font-semibold">{title}</h2>{" "}
          {description && (
            <p className="text-sm text-muted-foreground">{description}</p>
          )}{" "}
        </div>{" "}
      </header>{" "}
      <div className="p-6">{children}</div>{" "}
    </section>
  );
}
