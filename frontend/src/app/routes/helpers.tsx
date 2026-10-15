import { Suspense } from "react";
import type { ReactNode } from "react";

export function withSuspense(node: ReactNode, fallback: ReactNode) {
  return <Suspense fallback={fallback}>{node}</Suspense>;
}
