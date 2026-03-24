import { createFileRoute } from '@tanstack/solid-router'
import { WohnungDetailPage } from '@pages/wohnung-detail/WohnungDetailPage'

export const Route = createFileRoute('/wohnungen/$id')({
  component: function WohnungDetailRoute() {
    const { id } = Route.useParams()
    return <WohnungDetailPage id={id} />
  },
})
