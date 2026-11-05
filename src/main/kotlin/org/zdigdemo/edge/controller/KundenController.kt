package org.zdigdemo.edge.controller

import gg.jte.generated.precompiled.Templates
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.zdigdemo.edge.repositories.KundenRepository
import java.util.*

@Controller
@ResponseBody
@RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
class KundenController(
	private val kundenRepository: KundenRepository,
	private val templates: Templates,
) {

	//region Full Page Endpoints
	@GetMapping("/")
	fun kundenMainPage(): String =
		templates.pagesKundenIndex(kundenRepository.getAllClients()).render()

	@GetMapping("/kunden/{id}/edit")
	fun kundenEditieren(@PathVariable id: UUID): String {
		val kunde = kundenRepository.getClient(id)
			?: return templates.pagesKundenTablePartialsError().render()
		return templates.pagesKundenEditIndex(kunde).render()
	}
	//endregion

	//region HTMX endpoints
	@HxRequest
	@DeleteMapping("/kunden/{id}/delete")
	fun kundenLoeschen(@PathVariable id: UUID): String {
		kundenRepository.deleteClient(id)
		val totalAmountOfClientsLeft = kundenRepository.getAllClients().count()
		return templates.pagesKundenTablePartialsKundenAnzahl(totalAmountOfClientsLeft).render()
	}

	@HxRequest
	@GetMapping("/kunden")
	fun generiereKundenListeGefiltert(@RequestParam sucheingabe: String?): String {
		val kundenListe = if (sucheingabe.isNullOrEmpty()) {
			kundenRepository.getAllClients()
		} else {
			kundenRepository.getAllClientsFittingSearchterm(sucheingabe)
		}
		return templates.pagesKundenComponentsClientListWithDetailView(kundenListe).render()
	}
}
