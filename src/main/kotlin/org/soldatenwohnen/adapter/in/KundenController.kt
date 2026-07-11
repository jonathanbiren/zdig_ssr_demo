package org.soldatenwohnen.adapter.`in`

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

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
class KundenController() {

	@GetMapping("/")
	fun jteTest(model: Model): String {
		model.addAttribute("kundenListe", listeAllerKunden)
		return Views.INDEX
	}

	@GetMapping("/kunden/{id}/edit")
	fun kundenEditieren(
		@PathVariable id: Int,
		model: Model
	): String {
		val kunde = listeAllerKunden.find { it.id == id } ?: return Views.ERROR
		model.addAttribute("kunde", kunde)
		return Views.KUNDEN_EDIT
	}

	@HxRequest
	@GetMapping("/kunden")
	fun generiereKundenListeGefiltert(
		@RequestParam sucheingabe: String?,
		model: Model
	): String {
		val suchbegriff = sucheingabe?.trim()?.lowercase()
		val gefilterteKundenliste =
			if (suchbegriff.isNullOrEmpty()) listeAllerKunden else filterClientList(suchbegriff)
		model.addAttribute("kundenListe", gefilterteKundenliste)
		return Views.FILTERED_LIST
	}

	@HxRequest
	@DeleteMapping("/kunden/{id}")
	@ResponseBody
	fun kundenLoeschen(@PathVariable id: Int): String {
		listeAllerKunden.removeIf { it.id == id }
		return ""
	}

	@HxRequest
	@GetMapping("/kunden/{id}/detail")
	fun kundenDetail(
		@PathVariable id: Int,
		model: Model
	): String {
		val kundenDetail = kundenIdToDetailMap[id] ?: return Views.ERROR
		model.addAttribute("kundenDetail", kundenDetail)
		return Views.KUNDEN_DETAILS
	}
}

fun filterClientList(suchbegriff: String) =
	listeAllerKunden.filter { k ->
		k.kurzname.lowercase().contains(suchbegriff) ||
			k.name.lowercase().contains(suchbegriff) ||
			k.adresse.lowercase().contains(suchbegriff) ||
			k.status.lowercase().contains(suchbegriff) ||
			k.zsNummer.toString().contains(suchbegriff)
	}


data class Kunde(
	val id: Int,
	val kurzname: String,
	val name: String,
	val zsNummer: Int,
	val adresse: String,
	val status: String,
	val istBesondersSchuetzenswert: Boolean,
)

val kundenIdToDetailMap: Map<Int, String> = mapOf(
	1 to "First customer onboarded in the system",
	2 to "Largest contract volume in 2024",
	3 to "Family-owned business since 1987",
	4 to "Strategic partnership established",
	5 to "Consulting services specialist"
)

val listeAllerKunden: MutableList<Kunde> = mutableListOf(
	Kunde(
		id = 1,
		kurzname = "AI",
		name = "Ankundigung Inc",
		zsNummer = 4582,
		adresse = "40219 Düsseldorf",
		status = "Unterlagenführend",
		istBesondersSchuetzenswert = true,
	),
	Kunde(
		id = 2,
		kurzname = "BRAL",
		name = "Bayer",
		zsNummer = 4480,
		adresse = "56077 Koblenz",
		status = "Ausgeschieden",
		istBesondersSchuetzenswert = false
	),
	Kunde(
		id = 3,
		kurzname = "MUE",
		name = "Müller GmbH",
		zsNummer = 4521,
		adresse = "80331 München",
		status = "Aktiv",
		istBesondersSchuetzenswert = false
	),
	Kunde(
		id = 4,
		kurzname = "SCHM",
		name = "Schmidt & Partner",
		zsNummer = 4603,
		adresse = "20095 Hamburg",
		status = "Unterlagenführend",
		istBesondersSchuetzenswert = true
	),
	Kunde(
		id = 5,
		kurzname = "WEIS",
		name = "Weiss Consulting",
		zsNummer = 4495,
		adresse = "70173 Stuttgart",
		status = "Aktiv",
		istBesondersSchuetzenswert = false
	)
)