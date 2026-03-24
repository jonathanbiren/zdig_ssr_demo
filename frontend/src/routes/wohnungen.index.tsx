import { createFileRoute } from '@tanstack/solid-router'
import { WohnungenPage } from '@pages/wohnungen/WohnungenPage'

export const Route = createFileRoute('/wohnungen/')({
  component: WohnungenPage,
})
