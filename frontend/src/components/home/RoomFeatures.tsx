import { cn } from "@/lib/utils";

import type { RoomFeature } from "./rooms.data";

type RoomFeaturesProps = {
  features: RoomFeature[];
  className?: string;
};

export default function RoomFeatures({
  features,
  className,
}: RoomFeaturesProps) {
  return (
    <div
      className={cn("grid grid-cols-2 gap-x-5 gap-y-4", className)}>
      {features.map((feature) => {
        const Icon = feature.icon;
        return (
          <div
            key={feature.label}
            className="group/feature flex items-center gap-3">
            <div
              className="
                flex items-center justify-center
                size-10 rounded-xl
                bg-rose-50
                ring-1 ring-rose-200/50
                transition-all duration-300
                group-hover/feature:bg-rose-100
                group-hover/feature:ring-rose-300/60
                group-hover/feature:scale-105
              ">
              <Icon
                className="
                  size-4 text-rose-500
                  transition-colors duration-300
                  group-hover/feature:text-rose-600
                "
              />
            </div>
            <span
              className="
                font-sans text-sm text-stone-600 font-light
                transition-colors duration-300
                group-hover/feature:text-stone-900
              ">
              {feature.label}
            </span>
          </div>
        );
      })}
    </div>
  );
}
