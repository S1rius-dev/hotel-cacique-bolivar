import { Helmet } from "react-helmet-async";

interface JSONLDProps {
  data: Record<string, unknown>;
}

export function JSONLD({ data }: JSONLDProps) {
  return (
    <Helmet>
      <script type="application/ld+json">{JSON.stringify(data)}</script>
    </Helmet>
  );
}
