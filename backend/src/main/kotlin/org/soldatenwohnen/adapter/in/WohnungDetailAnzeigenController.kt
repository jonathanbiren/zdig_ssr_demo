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
      // We wrap the response of the controller with a ResponseEntity. This is a standard Spring class which adds an
      // HTTP status code to the response instead of just the JSON data.
      @GetMapping("/{id}")
      fun detail(@PathVariable id: UUID): ResponseEntity<WohnungDetailResponse> {
            // Here you have exception handling code inside your controller, resulting in code that is not very
            // cohesive. Maybe you should look into working with controller advice to handle exceptions globally.
            val wohnung = wohnungDetailService.detailAnzeigen(WohnungId(id))
                  ?: return ResponseEntity.notFound().build()
            return ResponseEntity.ok(wohnung.toDetail())
      }
}

// The detail response includes the full address and description --
// more information than the list entry response. This is the output model of this controller. Jackson will convert
// this to JSON before it is returned back to the client which made the request.
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

// Private extension function is used so that the domain model does not have to be aware of the controller's output
// model. This is a good practice to avoid circular dependencies. The use of the 'private' keyword means that this
// function is only accessible within this file. This might be a useful pattern to use in other controllers as well.
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
