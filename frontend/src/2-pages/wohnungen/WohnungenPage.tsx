import {createResource, For, Show} from 'solid-js'
import {Link} from '@tanstack/solid-router'
import {fetchWohnungListe} from '@entities/wohnung'
import type {WohnungListeEintrag} from '@entities/wohnung'

export function WohnungenPage() {
    const [wohnungen] = createResource(fetchWohnungListe)

    return (
        <div class="max-w-4xl mx-auto">
            <h2 class="text-2xl font-bold mb-6">Verfügbare Wohnungen</h2>

            <Show when={wohnungen.loading}>
                <p class="text-gray-500">Wohnungen werden geladen...</p>
            </Show>

            <Show when={wohnungen.error}>
                <p class="text-red-600">Fehler: {wohnungen.error?.message}</p>
            </Show>

            <Show when={wohnungen()}>
                <Show
                    when={(wohnungen()?.length ?? 0) > 0}
                    fallback={<p class="text-gray-500">Keine Wohnungen verfügbar.</p>}
                >
                    <div class="grid gap-4">
                        <For each={wohnungen()}>
                            {(wohnung) => <WohnungKarte wohnung={wohnung}/>}
                        </For>
                    </div>
                </Show>
            </Show>
        </div>
    )
}

function WohnungKarte({wohnung}: { wohnung: WohnungListeEintrag }) {
    return (
        <Link
            href={`/wohnungen/${wohnung.id}`}
            class="block bg-white rounded-lg border border-gray-200 p-5 hover:border-gray-400 hover:shadow-sm transition-all"
        >
            <div class="flex justify-between items-start gap-4">
                <div>
                    <h3 class="font-semibold text-lg text-gray-900">{wohnung.titel}</h3>
                    <p class="text-gray-500 text-sm mt-1">
                        {wohnung.postleitzahl} {wohnung.stadt}
                    </p>
                </div>
                <div class="text-right shrink-0">
                    <p class="font-bold text-gray-900">
                        {Number(wohnung.mietpreisEuro).toFixed(2)} €
                        <span class="text-sm font-normal text-gray-500"> / Monat</span>
                    </p>
                </div>
            </div>
            <div class="flex gap-4 mt-3 text-sm text-gray-600">
                <span>{Number(wohnung.quadratmeter).toFixed(0)} m²</span>
                <span>{Number(wohnung.zimmeranzahl)} Zimmer</span>
            </div>
        </Link>
    )
}
