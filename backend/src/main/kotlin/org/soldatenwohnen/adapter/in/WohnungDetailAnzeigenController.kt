package org.soldatenwohnen.adapter.`in`

import org.soldatenwohnen.core.katalog.application.port.`in`.WohnungDetailAnzeigenUseCase
import org.soldatenwohnen.core.katalog.domain.model.Wohnung
import org.soldatenwohnen.core.katalog.domain.model.WohnungId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/katalog/wohnungen")
class WohnungDetailAnzeigenController(
    private val wohnungDetailService: WohnungDetailAnzeigenUseCase
) {
    @GetMapping("/{id}")
    fun detail(@PathVariable id: UUID): ResponseEntity<WohnungDetailResponse> {
        val wohnung = wohnungDetailService.detailAnzeigen(WohnungId(id))
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(wohnung.toDetail())
    }
}

// The detail response includes the full address and description --
// more information than the list entry response.
data class WohnungDetailResponse(
    val id: UUID,
    val titel: String,
    val beschreibung: String,
    val strasse: String,
    val hausnummer: String,
    val postleitzahl: String,
    val stadt: String,
    val quadratmeter: BigDecimal,
    val zimmeranzahl: BigDecimal,
    val mietpreisEuro: BigDecimal,
    val erstelltAm: Instant
)

private fun Wohnung.toDetail() = WohnungDetailResponse(
    id = id.value,
    titel = titel,
    beschreibung = beschreibung,
    strasse = adresse.strasse,
    hausnummer = adresse.hausnummer,
    postleitzahl = adresse.postleitzahl,
    stadt = adresse.stadt,
    quadratmeter = quadratmeter,
    zimmeranzahl = zimmeranzahl,
    mietpreisEuro = mietpreis.inEuro(),
    erstelltAm = erstelltAm
)
