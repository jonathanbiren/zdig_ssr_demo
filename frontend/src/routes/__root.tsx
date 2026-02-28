import { Outlet, createRootRoute } from '@tanstack/solid-router'

export const Route = createRootRoute({
  component: RootLayout,
})

function RootLayout() {
  return (
    <div class="min-h-screen bg-gray-50 text-gray-900">
      <header class="border-b border-gray-200 bg-white px-6 py-4">
        <h1 class="text-xl font-semibold bg-red-700">Soldaten Wohnen</h1>
      </header>
      <main class="p-6">
        <Outlet />
      </main>
    </div>
  )
}
