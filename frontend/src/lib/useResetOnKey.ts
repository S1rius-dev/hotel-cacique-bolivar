import { useState } from "react";

/**
 * Ejecuta `reset` cuando `key` cambia, durante el render.
 *
 * Es el patron oficial de React para "ajustar estado cuando cambia una prop"
 * (ver <https://react.dev/learn/you-might-not-need-an-effect>). Frente a un
 * `useEffect` evita el render extra con el valor viejo, que es justo lo que
 * dispara renders en cascada.
 *
 * @param key    Identidad contra la que se compara (id, `open`, una firma...).
 * @param reset  Callback que aplica el nuevo estado inicial.
 */
export function useResetOnKey(key: string, reset: () => void): void {
  const [lastKey, setLastKey] = useState(key);

  if (key !== lastKey) {
    setLastKey(key);
    reset();
  }
}
