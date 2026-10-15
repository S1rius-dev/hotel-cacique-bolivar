import type { ReactNode } from "react";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";

interface BackHeaderLayoutProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
  contentClassName?: string;
}

const BackHeaderLayout = ({
  title,
  subtitle,
  children,
  contentClassName = "",
}: BackHeaderLayoutProps) => {
  const navigate = useNavigate();

  return (
    <div className="h-full flex flex-col min-h-0 min-w-0">
      <div className="flex items-center justify-between px-4 sm:px-6 py-3 sm:py-4 border-b shrink-0 min-w-0">
        <div className="flex items-center gap-3 sm:gap-4 min-w-0">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => navigate(-1)}
            className="rounded-lg hover:bg-rose-800/90! shrink-0">
            <ArrowLeft size={18} />
          </Button>

          <div className="min-w-0">
            <h1 className="text-lg sm:text-xl font-semibold truncate">{title}</h1>
            {subtitle && (
              <p className="text-xs sm:text-sm text-muted-foreground mt-0.5 truncate">{subtitle}</p>
            )}
          </div>
        </div>
      </div>

      <div className={`p-4 sm:p-6 min-h-0 flex-1 min-w-0 ${contentClassName}`}>{children}</div>
    </div>
  );
};

export default BackHeaderLayout;
