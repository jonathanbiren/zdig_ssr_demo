import { Link, Outlet, createRootRoute } from '@tanstack/solid-router'

export const Route = createRootRoute({
  component: RootLayout,
})

function RootLayout() {
  return (
    <div class="min-h-screen bg-gray-50 text-gray-900">
      <header class="border-b border-gray-200 bg-white px-6 py-4 flex items-center gap-8">
        <Link href="/" class="text-xl font-semibold text-gray-900">Soldaten Wohnen</Link>
        <nav class="flex gap-4 text-sm">
          <Link
            href="/wohnungen"
            class="text-gray-600 hover:text-gray-900"
            activeProps={{ class: 'text-gray-900 font-medium' }}
          >
            Wohnungen
          </Link>
        </nav>
      </header>
      <main class="p-6">
        <Outlet />
      </main>
    </div>
  )
}
