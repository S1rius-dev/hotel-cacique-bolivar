import { ScanBarcode, Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useBarcodeScanner } from "../hooks/useBarcodeScanner";

interface Props {
  value: string;
  onChange: (v: string) => void;
  onScan?: (code: string) => void;
  placeholder?: string;
}

export default function ProductSearchBar({
  value,
  onChange,
  onScan,
  placeholder = "Buscar por nombre o SKU...",
}: Props) {
  const { onKeyDown } = useBarcodeScanner((code) => {
    onChange(code);
    onScan?.(code);
  });

  return (
    <div className="relative flex-1">
      <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground pointer-events-none z-10" />
      <Input
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={onKeyDown}
        className="pl-9 pr-9 h-9 bg-background border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-[#c81e3a]/40"
      />
      <ScanBarcode className="absolute right-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground pointer-events-none" />
    </div>
  );
}
