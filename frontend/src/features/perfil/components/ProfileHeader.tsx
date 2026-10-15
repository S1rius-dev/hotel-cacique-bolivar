import { useState } from "react";
import { Mail, X } from "lucide-react";

interface ProfileHeaderProps {
  name: string;
  email?: string;
  avatarUrl?: string;
  onClose: () => void;
}

function getInitials(name: string) {
  const parts = name
    .trim()
    .split(/[\s._-]+/)
    .filter(Boolean);
  const letters =
    parts.length > 1 ? parts[0][0] + parts[1][0] : name.slice(0, 2);
  return letters.toUpperCase();
}

export default function ProfileHeader({
  name,
  email,
  avatarUrl,
  onClose,
}: ProfileHeaderProps) {
  const [imgFailed, setImgFailed] = useState(false);
  const showPhoto = !!avatarUrl && !imgFailed;

  return (
    <div className="relative flex items-center gap-3.5 px-6 pb-4 pt-6">
      {showPhoto ? (
        <img
          src={avatarUrl}
          alt=""
          onError={() => setImgFailed(true)}
          className="size-[52px] shrink-0 rounded-full object-cover ring-2 ring-[#E6C3D0] ring-offset-2 ring-offset-[#FFFDFB]"
        />
      ) : (
        <div
          aria-hidden="true"
          className="flex size-[52px] shrink-0 items-center justify-center rounded-full bg-[#F3E4EA] font-heading text-lg text-[#7A1440] ring-2 ring-[#E6C3D0] ring-offset-2 ring-offset-[#FFFDFB]">
          {getInitials(name)}
        </div>
      )}

      <div className="min-w-0 pr-8">
        <p className="truncate font-heading text-[19px] font-semibold leading-tight text-[#241F1D]">
          {name}
        </p>
        {email && (
          <p className="mt-1 flex items-center gap-1.5 text-[12.5px] text-[#8A7F76]">
            <Mail className="size-3.5 shrink-0" aria-hidden="true" />
            <span className="truncate">{email}</span>
          </p>
        )}
      </div>

      <button
        type="button"
        onClick={onClose}
        aria-label="Cerrar perfil"
        className="absolute right-3.5 top-3.5 flex size-8 cursor-pointer items-center justify-center rounded-full text-[#8A7F76] transition hover:bg-[#F5F0EA] hover:text-[#241F1D] focus-visible:outline-2 focus-visible:outline-[#7A1440]">
        <X className="size-[18px]" />
      </button>
    </div>
  );
}
