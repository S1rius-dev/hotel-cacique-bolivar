import { Tv, Wifi, Bath, Thermometer, Fan, Layers } from "lucide-react";
import type { LucideIcon } from "lucide-react";

export interface Amenity {
  icon: LucideIcon;
  label: string;
}

export const roomAmenities: Amenity[] = [
  { icon: Tv, label: "TV" },
  { icon: Wifi, label: "WiFi" },
  { icon: Bath, label: "Baño privado" },
  { icon: Thermometer, label: "Agua caliente" },
  { icon: Fan, label: "Ventilador" },
  { icon: Layers, label: "Toallas" },
];
