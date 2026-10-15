import { useState, useEffect } from "react";

interface Props {
  sections: { id: string; title: string }[];
}

export default function PageTOC({ sections }: Props) {
  const [activeId, setActiveId] = useState(sections[0]?.id);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((e) => e.isIntersecting)
          .sort(
            (a, b) => a.boundingClientRect.top - b.boundingClientRect.top,
          );

        if (visible.length > 0) {
          setActiveId(visible[0].target.id);
        }
      },
      { rootMargin: "-80px 0px -65% 0px" },
    );

    const elements = sections
      .map((s) => document.getElementById(s.id))
      .filter((el): el is HTMLElement => el !== null);

    for (const el of elements) observer.observe(el);
    return () => observer.disconnect();
  }, [sections]);

  return (
    <nav className="hidden lg:block w-56 shrink-0 sticky top-32 self-start max-h-[calc(100vh-10rem)] overflow-y-auto">
      <p className="font-sans text-[11px] tracking-[0.2em] uppercase text-stone-400 font-medium mb-4">
        En esta página
      </p>
      <div className="space-y-0.5">
        {sections.map((s) => {
          const isActive = s.id === activeId;
          return (
            <a
              key={s.id}
              href={`#${s.id}`}
              onClick={(e) => {
                e.preventDefault();
                setActiveId(s.id);
                document
                  .getElementById(s.id)
                  ?.scrollIntoView({ behavior: "smooth" });
              }}
              className={`block text-sm leading-snug py-1.5 pl-3 border-l-2 transition-all duration-200 ${
                isActive
                  ? "border-l-rose-500 text-rose-700 font-semibold"
                  : "border-l-transparent text-stone-400 hover:text-stone-600 hover:border-l-stone-300"
              }`}
            >
              {s.id}. {s.title}
            </a>
          );
        })}
      </div>
    </nav>
  );
}
