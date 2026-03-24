import { createResource, Show } from 'solid-js'
import { Link } from '@tanstack/solid-router'
import { fetchWohnungDetail } from '@entities/wohnung'

export function WohnungDetailPage({ id }: { id: string }) {
  const [wohnung] = createResource(() => id, fetchWohnungDetail)

  return (
    <div class="max-w-2xl mx-auto">
      <Link href="/wohnungen" class="text-sm text-gray-500 hover:text-gray-800 mb-6 inline-block">
        ← Zurück zur Übersicht
      </Link>

      <Show when={wohnung.loading}>
        <p class="text-gray-500">Wohnung wird geladen...</p>
      </Show>

      <Show when={wohnung.error}>
        <div class="bg-red-50 border border-red-200 rounded-lg p-4">
          <p class="text-red-700">{wohnung.error?.message}</p>
        </div>
      </Show>

      <Show when={wohnung()}>
        {(w) => (
          <div class="bg-white rounded-lg border border-gray-200 p-6">
            <h2 class="text-2xl font-bold text-gray-900">{w().titel}</h2>

            <div class="flex gap-2 mt-2 text-gray-500 text-sm">
              <span>{w().postleitzahl} {w().stadt}</span>
              <span>·</span>
              <span>{w().strasse} {w().hausnummer}</span>
            </div>

            <div class="flex gap-6 mt-5 py-4 border-y border-gray-100">
              <div>
                <p class="text-xs text-gray-400 uppercase tracking-wide">Mietpreis</p>
                <p class="text-xl font-bold text-gray-900 mt-0.5">
                  {Number(w().mietpreisEuro).toFixed(2)} € <span class="text-sm font-normal text-gray-500">/ Monat</span>
                </p>
              </div>
              <div>
                <p class="text-xs text-gray-400 uppercase tracking-wide">Fläche</p>
                <p class="text-xl font-semibold text-gray-900 mt-0.5">{Number(w().quadratmeter).toFixed(0)} m²</p>
              </div>
              <div>
                <p class="text-xs text-gray-400 uppercase tracking-wide">Zimmer</p>
                <p class="text-xl font-semibold text-gray-900 mt-0.5">{Number(w().zimmeranzahl)}</p>
              </div>
            </div>

            <div class="mt-5">
              <h3 class="font-semibold text-gray-900 mb-2">Beschreibung</h3>
              <p class="text-gray-600 leading-relaxed">{w().beschreibung}</p>
            </div>
          </div>
        )}
      </Show>
    </div>
  )
}
