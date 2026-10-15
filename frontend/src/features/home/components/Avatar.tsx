interface Props {
  name: string;
  className?: string;
}

export default function Avatar({ name, className }: Props) {
  return (
    <div
      className={`size-10 rounded-full flex items-center justify-center bg-linear-to-br from-rose-500/30 to-rose-700/20 border border-rose-300/20 text-rose-200 font-semibold text-sm ${className ?? ""}`}>
      {name.charAt(0)}
    </div>
  );
}
