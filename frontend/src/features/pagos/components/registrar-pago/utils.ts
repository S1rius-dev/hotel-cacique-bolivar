export const parseMonto = (raw: string): number => {
  return Number(raw.replace(/[^0-9]/g, "")) || 0;
};
