package org.zdigdemo.edge.controller


import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.zdigdemo.edge.repositories.KundeViewModel
import org.zdigdemo.edge.repositories.KundenRepository
import java.util.*


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
		return "pages/kunden/index.kte"
	}

	@GetMapping("/kunden/{id}/edit")
	fun kundenEditieren(
		@PathVariable id: UUID,
		model: Model
	): String {
		val kunde = kundenRepository.getClient(id) ?: return "pages/kunden/table/partials/error.kte"
		model.addAttribute("kunde", kunde)
		return "pages/kunden/edit/index.kte"
	}
	//endregion

	//region HTMX endpoints
	@HxRequest
	@DeleteMapping("/kunden/{id}/delete")
	fun kundenLoeschen(@PathVariable id: UUID, model: Model): String {
		kundenRepository.deleteClient(id)
		val totalAmountOfClientsLeft = kundenRepository.getAllClients().count()
		model.addAttribute("kundenAnzahl", totalAmountOfClientsLeft)
		return "pages/kunden/table/partials/kundenAnzahl.kte"
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
		return "pages/kunden/components/clientListWithDetailView.kte"
	}
}