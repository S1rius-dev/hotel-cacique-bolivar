import { useState, useEffect } from "react";

export const useScroll = (threshold?: number) => {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const t = threshold ?? window.innerHeight * 0.65;

    const handleScroll = () => {
      setScrolled(window.scrollY > t);
    };

    handleScroll();
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [threshold]);

  return scrolled;
};
