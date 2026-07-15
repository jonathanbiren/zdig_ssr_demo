package org.soldatenwohnen.adapter.`in`

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.soldatenwohnen.adapter.out.KundenRepository
import org.soldatenwohnen.core.katalog.domain.model.Kunde
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.util.UUID

//Object for reducing the visual noise of Spring Boot not being able to resolve the JTE templates
object Views {
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
	val kundenRepository: KundenRepository,
	val verpflichtungenRepository:
	) {

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
		return Views.KUNDEN_DETAILS
	}
}

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


private fun Kunde.toPresentationModel(): KundePresentationModel = KundePresentationModel(
	id = this.id.toString(),
	kurzname = this.kurzname,
	name = this.name,
	zsNummer = this.zsNummer,
	adresse = this.adresse,
	status = this.status,
	istBesondersSchuetzenswert = this.istBesondersSchuetzenswert
)


//fun filterClientList(suchbegriff: String) =
//	listeAllerKunden.filter { k ->
//		k.kurzname.lowercase().contains(suchbegriff) ||
//			k.name.lowercase().contains(suchbegriff) ||
//			k.adresse.lowercase().contains(suchbegriff) ||
//			k.status.lowercase().contains(suchbegriff) ||
//			k.zsNummer.toString().contains(suchbegriff)
//	}


//data class Kunde(
//	val id: Int,
//	val kurzname: String,
//	val name: String,
//	val zsNummer: Int,
//	val adresse: String,
//	val status: String,
//	val istBesondersSchuetzenswert: Boolean,
//)
//
//val kundenIdToDetailMap: Map<Int, String> = mapOf(
//	1 to "First customer onboarded in the system",
//	2 to "Largest contract volume in 2024",
//	3 to "Family-owned business since 1987",
//	4 to "Strategic partnership established",
//	5 to "Consulting services specialist"
//)

//val listeAllerKunden: MutableList<Kunde> = mutableListOf(
//	Kunde(
//		id = 1,
//		kurzname = "AI",
//		name = "Ankundigung Inc",
//		zsNummer = 4582,
//		adresse = "40219 Düsseldorf",
//		status = "Unterlagenführend",
//		istBesondersSchuetzenswert = true,
//	),
//	Kunde(
//		id = 2,
//		kurzname = "BRAL",
//		name = "Bayer",
//		zsNummer = 4480,
//		adresse = "56077 Koblenz",
//		status = "Ausgeschieden",
//		istBesondersSchuetzenswert = false
//	),
//	Kunde(
//		id = 3,
//		kurzname = "MUE",
//		name = "Müller GmbH",
//		zsNummer = 4521,
//		adresse = "80331 München",
//		status = "Aktiv",
//		istBesondersSchuetzenswert = false
//	),
//	Kunde(
//		id = 4,
//		kurzname = "SCHM",
//		name = "Schmidt & Partner",
//		zsNummer = 4603,
//		adresse = "20095 Hamburg",
//		status = "Unterlagenführend",
//		istBesondersSchuetzenswert = true
//	),
//	Kunde(
//		id = 5,
//		kurzname = "WEIS",
//		name = "Weiss Consulting",
//		zsNummer = 4495,
//		adresse = "70173 Stuttgart",
//		status = "Aktiv",
//		istBesondersSchuetzenswert = false
//	)
//)