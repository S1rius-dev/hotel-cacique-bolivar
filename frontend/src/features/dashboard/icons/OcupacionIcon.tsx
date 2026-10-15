import type { IconProps } from "./Icon";

const OcupacionIcon: React.FC<IconProps> = ({ className }) => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="1em"
      height="1em"
      viewBox="0 0 2048 2048"
      className={className}>
      <path
        fill="currentColor"
        d="M896 128v768H128V128h768zm-128 640V256H256v512h512zM1920 128v768h-768V128h768zm-128 640V256h-512v512h512zM896 1152v768H128v-768h768zm-128 640v-512H256v512h512zm1152-640v768h-768v-768h768zm-128 640v-512h-512v512h512z"
      />
    </svg>
  );
};

export default OcupacionIcon;
