package org.zdigdemo.edge.controller


import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.zdigdemo.edge.repositories.KundeViewModel
import org.zdigdemo.edge.repositories.KundenRepository
import java.util.*

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
	val kundenRepository: KundenRepository,
) {

	//region Full Page Endpoints
	@GetMapping("/")
	fun kundenMainPage(model: Model): String {
		val listeAllerKunden: List<KundeViewModel> =
			kundenRepository.getAllClients()
		model.addAttribute("kundenListe", listeAllerKunden)
		return Views.INDEX
	}

	@GetMapping("/kunden/{id}/edit")
	fun kundenEditieren(
		@PathVariable id: UUID,
		model: Model
	): String {
		val kunde = kundenRepository.getClient(id) ?: return Views.ERROR
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
			val listeAllerKunden = kundenRepository
				.getAllClients()
			model.addAttribute("kundenListe", listeAllerKunden)
		} else {
			val listeGefilterterKunden = kundenRepository
				.getAllClientsFittingSearchterm(sucheingabe)
			model.addAttribute("kundenListe", listeGefilterterKunden)
		}
		return Views.FILTERED_LIST
	}

//	@HxRequest
//	@GetMapping("/kunden/{id}/detail")
//	fun kundenDetail(
//		@PathVariable id: UUID,
//		model: Model
//	): String {
//		val kundeMitVerpflichtungen =
//			kundenRepository.getKundeMitVerpflichtungen(id) ?: return Views.ERROR
//		model.addAttribute("kunde", kundeMitVerpflichtungen.kunde.toPresentationModel())
//		model.addAttribute(
//			"verpflichtungen",
//			kundeMitVerpflichtungen.verpflichtungen.map { it.toPresentationModel() }
//		)
//		return Views.KUNDEN_DETAILS
//	}
	//endregion
}