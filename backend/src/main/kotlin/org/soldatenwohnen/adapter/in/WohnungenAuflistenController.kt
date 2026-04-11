package org.soldatenwohnen.adapter.`in`

import org.soldatenwohnen.core.katalog.application.port.`in`.WohnungenAuflistenUseCase
import org.soldatenwohnen.core.katalog.domain.model.Wohnung
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/katalog/wohnungen")
class WohnungenAuflistenController(
    // Depends on the interface, not the service class.
    private val wohnungenAuflisten: WohnungenAuflistenUseCase
) {
    @GetMapping
    fun auflisten(): ResponseEntity<List<WohnungListeEintragResponse>> {
        val wohnungen: List<Wohnung> = wohnungenAuflisten.alleVeroeffentlichtenAuflisten()
        return ResponseEntity.ok(wohnungen.map { wohnung -> wohnung.toListeEintrag() })
    }
}

// ── Response DTOs (HTTP I/O models) ──────────────────────────────────────────
// These live in the adapter layer. They are what the API actually returns.
// They use primitive/standard types only -- no domain value objects. They are usually flat and simple, avoiding nested
// structures for simplicity and ease of serialization.

data class WohnungListeEintragResponse(
    val id: UUID,
    val titel: String,
    val stadt: String,
    val postleitzahl: String,
    val quadratmeter: BigDecimal,
    val zimmeranzahl: BigDecimal,
    val mietpreisEuro: BigDecimal,
    val erstelltAm: Instant
)

// Adding a private extension function so that we can map a Wohnung entity object to a WohnungListeEintragResponse
// This is the output model of the WohnungController. We don't have an input model in this case, since we have a 'GET'
// endpoint without any parameters. We therefore always receive back the same response and don't require any input.
// The private function means that we can only use this function within the current file, meaning all dependencies point
// inwards towards the domain layer since the file in which 'Wohnung' is declared won't know anything about the
// WohnungListeEintragResponse object type
private fun Wohnung.toListeEintrag() = WohnungListeEintragResponse(
    id = id.value,
    titel = titel,
    stadt = adresse.stadt,
    postleitzahl = adresse.postleitzahl,
    quadratmeter = quadratmeter,
    zimmeranzahl = zimmeranzahl,
    mietpreisEuro = mietpreis.inEuro(),
    erstelltAm = erstelltAm
)
