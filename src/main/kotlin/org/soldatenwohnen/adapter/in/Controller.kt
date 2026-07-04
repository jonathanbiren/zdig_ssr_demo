package org.soldatenwohnen.adapter.`in`

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class Controller() {

	@GetMapping("/")
	fun index(model: Model): String {
		model.addAttribute("kundenListe", listeAllerKunden)
		return "sfa/index"
	}

	@HxRequest
	@GetMapping("/kunden")
	fun kundenSuchen(
		@RequestParam(value = "sucheingabe", required = false) sucheingabe: String?,
		model: Model
	): String {
		val suchbegriff = sucheingabe?.trim()?.lowercase()
		val gefilterteListe = if (suchbegriff.isNullOrEmpty()) {
			listeAllerKunden
		} else {
			listeAllerKunden.filter { k ->
				k.kurzname.lowercase().contains(suchbegriff) ||
				k.name.lowercase().contains(suchbegriff) ||
				k.adresse.lowercase().contains(suchbegriff) ||
				k.status.lowercase().contains(suchbegriff) ||
				k.zsNummer.toString().contains(suchbegriff)
			}
		}
		model.addAttribute("kundenListe", gefilterteListe)
		return "sfa/index :: kundenTabelle"
	}

	@HxRequest
	@DeleteMapping("/kunden/{id}")
	@ResponseBody
	fun kundenLoeschen(@PathVariable id: Int): String {
		listeAllerKunden.removeIf { it.id == id }
		return ""
	}
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