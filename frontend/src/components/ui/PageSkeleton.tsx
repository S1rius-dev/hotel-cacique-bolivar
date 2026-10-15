const PageSkeleton: React.FC = () => {
  return (
    <div className="min-h-screen bg-card animate-pulse">
      {/* Navbar skeleton */}
      <header className="h-16 border-b bg-background flex items-center px-6 gap-4">
        <div className="w-8 h-8 rounded-full bg-muted" />
        <div className="h-4 w-32 rounded bg-muted" />
        <div className="ml-auto flex gap-3">
          <div className="h-8 w-8 rounded-full bg-muted" />
          <div className="h-8 w-8 rounded-full bg-muted" />
        </div>
      </header>

      {/* Hero skeleton (for home-like pages) */}
      <div className="h-[50vh] bg-muted" />

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 py-8 space-y-6">
        <div className="h-8 w-64 rounded bg-muted" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="h-40 rounded-xl bg-muted" />
          <div className="h-40 rounded-xl bg-muted" />
          <div className="h-40 rounded-xl bg-muted" />
        </div>
        <div className="h-64 rounded-xl bg-muted" />
      </div>
    </div>
  );
};

export default PageSkeleton;
