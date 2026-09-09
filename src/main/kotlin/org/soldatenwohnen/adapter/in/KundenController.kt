package org.soldatenwohnen.adapter.`in`


import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.soldatenwohnen.core.katalog.application.port.out.IKundenRepository
import org.soldatenwohnen.core.katalog.domain.model.Kunde
import org.soldatenwohnen.core.katalog.domain.model.Verpflichtung
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

//Object for reducing the visual noise of Spring Boot not being able to resolve the JTE templates
private object Views {
	//pages
	const val INDEX = "pages/kunden/index"
	const val KUNDEN_EDIT = "pages/kunden/kundeEditieren"

	//HTMX fragments
	const val FILTERED_LIST = "pages/kunden/fragments/clientListWithDetailView"
	const val KUNDEN_DETAILS = "pages/kunden/fragments/kundenDetail"
	const val ERROR = "pages/kunden/fragments/error"
}

@Controller
class KundenController(
	val kundenRepository: IKundenRepository,
) {

	//region Full Page Endpoints
	@GetMapping("/")
	fun kundenMainPage(model: Model): String {
		val listeAllerKunden: List<KundePresentationModel> =
			kundenRepository.getAllClients().map { kunde -> kunde.toPresentationModel() }
		model.addAttribute("kundenListe", listeAllerKunden)
		return Views.INDEX
	}

	@GetMapping("/kunden/{id}/edit")
	fun kundenEditieren(
		@PathVariable id: UUID,
		model: Model
	): String {
		val kunde = kundenRepository.getClient(id)?.toPresentationModel() ?: return Views.ERROR
		model.addAttribute("kunde", kunde)
		return Views.KUNDEN_EDIT
	}
	//endregion

	//region HTMX endpoints
	@HxRequest
	@DeleteMapping("/kunden/{id}/delete")
	@ResponseBody
	fun kundenLoeschen(@PathVariable id: UUID): String {
		kundenRepository.deleteClient(id)
		return ""
	}

	@HxRequest
	@GetMapping("/kunden")
	fun generiereKundenListeGefiltert(
		@RequestParam sucheingabe: String?,
		model: Model
	): String {
		if (sucheingabe.isNullOrEmpty()) {
			val listeAllerKunden: List<KundePresentationModel> = kundenRepository
				.getAllClients().map { kunde -> kunde.toPresentationModel() }
			model.addAttribute("kundenListe", listeAllerKunden)
		} else {
			val listeGefilterterKunden = kundenRepository
				.getAllClientsFittingSearchterm(sucheingabe)
				.map { kunde -> kunde.toPresentationModel() }
			model.addAttribute("kundenListe", listeGefilterterKunden)
		}
		return Views.FILTERED_LIST
	}

	@HxRequest
	@GetMapping("/kunden/{id}/detail")
	fun kundenDetail(
		@PathVariable id: UUID,
		model: Model
	): String {
		val kundeMitVerpflichtungen =
			kundenRepository.getKundeMitVerpflichtungen(id) ?: return Views.ERROR
		model.addAttribute("kunde", kundeMitVerpflichtungen.kunde.toPresentationModel())
		model.addAttribute(
			"verpflichtungen",
			kundeMitVerpflichtungen.verpflichtungen.map { it.toPresentationModel() }
		)
		return Views.KUNDEN_DETAILS
	}
	//endregion
}

//region Presentation Models
// This only exists to showcase that you would not directly pass the domain entity to the view, but instead convert
// it all to Strings first, so that the presentation layer has no problem rendering the view
data class KundePresentationModel(
	val id: String,
	val kurzname: String,
	val name: String,
	val zsNummer: Long,
	val adresse: String,
	val status: String,
	val istBesondersSchuetzenswert: Boolean
)

// Amounts reach the view as preformatted Strings, like everything else: the template
// never does arithmetic, the presentation layer converts cents to euros.
data class VerpflichtungPresentationModel(
	val bezeichnung: String,
	val betrag: String
)
//endregion


//region Mappers
private fun Kunde.toPresentationModel(): KundePresentationModel = KundePresentationModel(
	id = this.id.toString(),
	kurzname = this.kurzname,
	name = this.name,
	zsNummer = this.zsNummer,
	adresse = this.adresse,
	status = this.status,
	istBesondersSchuetzenswert = this.istBesondersSchuetzenswert
)


private fun Verpflichtung.toPresentationModel(): VerpflichtungPresentationModel =
	VerpflichtungPresentationModel(
		bezeichnung = this.bezeichnung,
		betrag = NumberFormat.getCurrencyInstance(Locale.GERMANY)
			.format(this.betragCent.toBigDecimal().movePointLeft(2))
	)
//endregion

