import { useCallback, useEffect, useRef } from "react";

/**
 * Hook reutilizable para lectores USB/Bluetooth que emulan teclado.
 * Detecta ráfaga de teclas con <50ms entre cada una terminando en Enter.
 * Se puede usar en cualquier input con onKeyDown o global window.
 */
export function useBarcodeScanner(onScan: (code: string) => void) {
  const bufferRef = useRef("");
  const lastRef = useRef(0);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent | KeyboardEvent) => {
      const now = Date.now();
      const delta = now - lastRef.current;
      lastRef.current = now;

      if (delta > 50) {
        bufferRef.current = "";
      }

      if (e.key === "Enter") {
        if (bufferRef.current.length >= 3) {
          const code = bufferRef.current;
          bufferRef.current = "";
          onScan(code);
        } else {
          bufferRef.current = "";
        }
        return;
      }

      if (e.key.length === 1) {
        bufferRef.current += e.key;
      }
    },
    [onScan],
  );

  // Opcional: escucha global para uso sin input enfocado
  useEffect(() => {
    const globalHandler = (e: KeyboardEvent) => handleKeyDown(e);
    window.addEventListener("keydown", globalHandler);
    return () => window.removeEventListener("keydown", globalHandler);
  }, [handleKeyDown]);

  return {
    onKeyDown: handleKeyDown,
    attach: { onKeyDown: handleKeyDown },
  };
}
