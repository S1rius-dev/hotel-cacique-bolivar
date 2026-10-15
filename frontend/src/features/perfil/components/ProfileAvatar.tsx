import { useProfileImage } from "@/lib/useProfileImage";
import { User } from "lucide-react";

interface Props {
  profilePicture?: string;
  name?: string;
  size?: "sm" | "md" | "lg";
}

const sizeMap = {
  sm: "size-10 text-lg",
  md: "size-16 text-2xl",
  lg: "size-24 text-4xl",
};

export default function ProfileAvatar({ profilePicture, name, size = "md" }: Props) {
  const imgSrc = useProfileImage(profilePicture);
  const initials = name
    ? name.split(" ").map((n) => n[0]).join("").slice(0, 2).toUpperCase()
    : undefined;

  if (imgSrc) {
    return (
      <div className="shrink-0">
        <img
          src={imgSrc}
          alt=""
          className={`${sizeMap[size]} rounded-full object-cover ring-2 ring-rose-300/50 ring-offset-2 shadow-md`}
        />
      </div>
    );
  }

  return (
    <div
      className={`${sizeMap[size]} flex items-center justify-center rounded-full bg-gradient-to-br from-rose-100 to-rose-200 text-rose-600 ring-2 ring-rose-300/50 ring-offset-2 shadow-md`}
    >
      {initials ?? <User className="size-1/2" />}
    </div>
  );
}
