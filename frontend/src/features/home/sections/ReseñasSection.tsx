import { useState, useCallback, useMemo } from "react";
import { useInView } from "@/lib/hooks/useInView";
import { useAuth } from "@/app/auth/AuthContext";
import { toast } from "sonner";
import { useResenas } from "../hooks/useResenas";
import FeaturedResenaCard from "../components/FeaturedResenaCard";
import ReputationPanel from "../components/ReputationPanel";
import InfiniteMarquee from "../components/InfiniteMarquee";
import ResenasCTA from "../components/ResenasCTA";
import ResenaFormDialog from "../components/ResenaFormDialog";

interface ResenasSectionProps {
  onLoginRequired?: () => void;
}

export default function ResenasSection({
  onLoginRequired,
}: ResenasSectionProps) {
  const { ref: headingRef, inView: headingInView } = useInView(0.1);
  const { user } = useAuth();

  const [currentIndex, setCurrentIndex] = useState(0);
  const [showResenaForm, setShowResenaForm] = useState(false);

  const { data, isLoading } = useResenas(0, 20);

  const reviews = useMemo(() => data?.content ?? [], [data?.content]);
  const featuredReviews = useMemo(() => reviews.slice(0, 5), [reviews]);

  const safeIndex = Math.min(
    currentIndex,
    Math.max(0, featuredReviews.length - 1),
  );

  const handlePrev = useCallback(() => {
    setCurrentIndex((i) => Math.max(0, i - 1));
  }, []);

  const handleNext = useCallback(() => {
    setCurrentIndex((i) => Math.min(featuredReviews.length - 1, i + 1));
  }, [featuredReviews.length]);

  const handleOpenReview = useCallback(() => {
    if (!user) {
      toast.info("Para dejar una reseña primero debes iniciar sesión");
      onLoginRequired?.();
      return;
    }

    setShowResenaForm(true);
  }, [user, onLoginRequired]);

  return (
    <section
      id="resenas"
      className="relative bg-stone-50 pt-12 md:pt-16 pb-12 md:pb-16 overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_left,var(--tw-gradient-stops))] from-rose-50/60 via-stone-50 to-transparent" />
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_bottom_right,var(--tw-gradient-stops))] from-rose-100/20 via-transparent to-transparent" />
      <div
        aria-hidden
        className="absolute inset-0 opacity-[0.015] pointer-events-none"
        style={{
          backgroundImage: `radial-gradient(circle at 1px 1px, #292524 1px, transparent 0)`,
          backgroundSize: "40px 40px",
        }}
      />

      <div
        aria-hidden
        className="absolute top-16 right-10 size-96 bg-rose-200/15 rounded-full blur-3xl pointer-events-none"
      />
      <div
        aria-hidden
        className="absolute bottom-40 left-10 size-80 bg-rose-300/10 rounded-full blur-3xl pointer-events-none"
      />
      <div
        aria-hidden
        className="absolute top-1/3 left-1/2 -translate-x-1/2 size-72 bg-rose-100/20 rounded-full blur-3xl pointer-events-none"
      />

      <div className="relative mx-auto max-w-[90rem] px-6">
        <div className="grid lg:grid-cols-12 gap-10 lg:gap-16 items-start">
          <div
            ref={headingRef}
            className={`
              lg:col-span-4 lg:sticky lg:top-32
              text-center lg:text-left
              transition-all duration-700
              ${headingInView ? "opacity-100 translate-y-0" : "opacity-0 translate-y-8"}
            `}>
            <p className="font-sans text-xs tracking-[0.3em] uppercase text-rose-500 font-medium mb-5">
              EXPERIENCIAS REALES
            </p>

            <h2 className="font-heading text-[2.75rem] md:text-6xl lg:text-[4.25rem] font-medium text-stone-900 leading-[1.05] tracking-tight">
              Cada estancia
              <br />
              <span className="italic font-normal">cuenta una historia</span>
            </h2>

            <div className="mx-auto lg:mx-0 mt-6 h-px w-16 bg-linear-to-r from-rose-300 via-rose-400 to-rose-300" />

            <p className="mt-6 font-sans text-base md:text-lg leading-relaxed text-stone-500 max-w-md mx-auto lg:mx-0 font-light">
              Descubre las experiencias de quienes hicieron de nuestro hotel
              parte de su viaje.
            </p>
          </div>

          <div className="lg:col-span-8 space-y-6 lg:space-y-8">
            <FeaturedResenaCard
              reviews={featuredReviews}
              currentIndex={safeIndex}
              onPrev={handlePrev}
              onNext={handleNext}
              isLoading={isLoading}
            />

            <ReputationPanel
              reviews={reviews}
              totalElements={data?.totalElements}
              isLoading={isLoading}
            />
          </div>
        </div>
      </div>

      <div className="mt-10 md:mt-14 px-2">
        <InfiniteMarquee reviews={reviews} isLoading={isLoading} />
      </div>

      <div className="relative mx-auto max-w-[90rem] px-6 mt-8 md:mt-12">
        <ResenasCTA onWriteReview={handleOpenReview} />
      </div>

      <ResenaFormDialog
        open={showResenaForm}
        onOpenChange={setShowResenaForm}
      />
    </section>
  );
}
