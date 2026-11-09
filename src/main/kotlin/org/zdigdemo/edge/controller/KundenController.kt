package org.zdigdemo.edge.controller

import gg.jte.generated.precompiled.Templates
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.zdigdemo.edge.repositories.KundenRepository
import org.zdigdemo.edge.repositories.KundenSortColumn
import org.zdigdemo.edge.repositories.SortData
import org.zdigdemo.edge.repositories.SortDirection
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
		templates.pagesKundenIndex(kundenRepository.getAllClients(), SortData()).render()

	@GetMapping("/kunden/{id}/edit")
	fun kundenEditieren(@PathVariable id: UUID): String {
		val kunde = kundenRepository.getClient(id)
			?: return templates.pagesKundenTablePartialsError().render()
		return templates.pagesKundenEditIndex(kunde).render()
	}
	//endregion

	//region HTMX endpoints
	@HxRequest
	@DeleteMapping("/kunden/{id}")
	fun kundenLoeschen(@PathVariable id: UUID): String {
		kundenRepository.deleteClient(id)
		val totalAmountOfClientsLeft = kundenRepository.getAllClients().count()
		return templates.pagesKundenTablePartialsKundenAnzahl(totalAmountOfClientsLeft).render()
	}

	@HxRequest
	@GetMapping("/kunden")
	fun generiereKundenListeSortiert(
		@RequestParam(required = false) sortColumn: String?,
		@RequestParam(required = false) sortDirection: String?,
		@RequestParam(required = false) sucheingabe: String?
	): String {
		//The repository returns List<KundeViewModel>. The full template model is constructed in the controller
		//It is the KundenSortColumn enmum (allowList) as well as the SortDirection, which takes care of the input validation
		val sortierInformation =
			SortData(
				sortColumn = KundenSortColumn.fromRequestName(sortColumn),
				sortDirection = SortDirection.create(sortDirection)
			)
		val kundenListe = kundenRepository.getClients(sortierInformation, sucheingabe)

		return templates.pagesKundenComponentsClientListWithDetailView(
			kundenListe = kundenListe,
			sortierInformation = sortierInformation
		).render()

	}
}



