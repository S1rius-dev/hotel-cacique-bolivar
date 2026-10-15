import React from "react";
import { NavLink } from "react-router-dom";
import { Sheet, SheetContent } from "@/components/ui/sheet";
import { Separator } from "@/components/ui/separator";

export interface SidebarItems {
  label: string;
  href: string;
  icon?: React.ReactNode;
  roles?: string[];
  disabled?: boolean;
}

export interface SidebarSection {
  title: string;
  items: SidebarItems[];
}

export interface SidebarLogo {
  src: string;
  alt: string;
}

export interface SidebarProps {
  sections: SidebarSection[];
  logo?: SidebarLogo;
  isMobileOpen?: boolean;
  onMobileClose?: () => void;
}

function Sidebar({
  sections,
  logo,
  isMobileOpen,
  onMobileClose,
}: SidebarProps) {
  const navContent = (
    <>
      {/* LOGO */}
      <header className="h-20 flex items-center px-8">
        {logo && (
          <img
            src={logo.src}
            alt={logo.alt}
            className="w-35 h-20 object-contain"
          />
        )}
      </header>

      {/* NAV */}
      <nav className="flex-1 px-4 py-8 overflow-y-auto">
        {sections.map((section, idx) => (
          <div key={section.title}>
            {idx > 0 && <Separator className="my-4" />}

            <p className="px-3 pb-1 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              {section.title}
            </p>

            <ul className="space-y-1">
              {section.items.map((item) => (
                <li key={item.href}>
                  <NavLink to={item.href} onClick={onMobileClose}>
                    {({ isActive }) => (
                      <div
                        className={`
                          group flex items-center gap-3 px-3 py-2 rounded-md
                          text-sm transition-colors
                          ${
                            isActive
                              ? "bg-rose-800/90 text-white"
                              : "text-foreground hover:bg-muted hover:text-rose-800"
                          }
                        `}>
                        {item.icon && <span>{item.icon}</span>}
                        <span>{item.label}</span>
                      </div>
                    )}
                  </NavLink>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </nav>
    </>
  );

  return (
    <>
      {/* Desktop */}
      <aside className="hidden md:flex w-55 bg-card flex-col border-r border-border">
        {navContent}
      </aside>

      {/* Mobile Sheet */}
      <Sheet open={isMobileOpen} onOpenChange={onMobileClose}>
        <SheetContent side="left" className="w-55! p-0" showCloseButton={false}>
          {navContent}
        </SheetContent>
      </Sheet>
    </>
  );
}

export default Sidebar;
