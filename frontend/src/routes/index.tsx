import { createFileRoute } from '@tanstack/solid-router'

export const Route = createFileRoute('/')({
  component: HomePage,
})

function HomePage() {
  return (
    <div>
      <h2 class="text-2xl font-bold">Willkommen</h2>
      <p class="mt-2 text-gray-600">Soldaten Wohnen - Startseite</p>
    </div>
  )
}
