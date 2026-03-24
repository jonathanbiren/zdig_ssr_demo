package org.soldatenwohnen.katalog.adapter.`in`

import org.soldatenwohnen.katalog.application.port.`in`.WohnungenAuflistenUseCase
import org.soldatenwohnen.katalog.domain.model.Wohnung
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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
        val wohnungen = wohnungenAuflisten.alleVeroeffentlichtenAuflisten()
        return ResponseEntity.ok(wohnungen.map { it.toListeEintrag() })
    }
}

// ── Response DTOs (HTTP I/O models) ──────────────────────────────────────────
// These live in the adapter layer. They are what the API actually returns.
// They use primitive/standard types only -- no domain value objects.

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
