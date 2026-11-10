package org.zdigdemo.edge.controller

import gg.jte.generated.precompiled.Templates
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.zdigdemo.edge.repositories.KundeViewModel
import org.zdigdemo.edge.repositories.KundenRepository
import org.zdigdemo.edge.repositories.KundenSortColumn
import org.zdigdemo.edge.repositories.SortData
import org.zdigdemo.edge.repositories.SortDirection
import org.zdigdemo.edge.repositories.VerpflichtungRepository
import java.util.*

@Controller
@ResponseBody
@RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
class KundenController(
	private val kundenRepository: KundenRepository,
	private val verpflichtungenRepository: VerpflichtungRepository,
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
		return templates.pagesKundenFormsEditForm(kunde).render()
	}

	@GetMapping("/kunden/create")
	fun kundenAnlegen(): String {
		return templates.pagesKundenFormsEditForm(KundeViewModel(
            id = UUID.randomUUID().toString(),
            kurzname = "",
            name = "",
			zsNummer = 0,
            adresse = "",
            status = "",
            istBesondersSchuetzenswert = false
        )).render()
	}
	//endregion

	@PatchMapping("/kunden/{id}/save")
	fun kundenSpeichern(kunde: KundeViewModel): ResponseEntity<Void> {
		kundenRepository.editExistingClient(kunde)
		return ResponseEntity.ok()
			.header("HX-Location", "/verpflichtungen")
			.build()
	}
	//region HTMX endpoints
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

	@HxRequest
	@GetMapping("/kunden/{id}/detail")
	fun getKundenDetail(@PathVariable id: UUID): String {
		val verpflichtungenDesKunden = verpflichtungenRepository.findByKundeId(id)
		return templates.pagesKundenTablePartialsKundeVerpflichtungen(verpflichtungenDesKunden).render()
	}

	@HxRequest
	@DeleteMapping("/kunden/{id}")
	fun kundenLoeschen(@PathVariable id: UUID): String {
		kundenRepository.deleteClient(id)
		val totalAmountOfClientsLeft = kundenRepository.getAllClients().count()
		return templates.pagesKundenTablePartialsKundenAnzahl(totalAmountOfClientsLeft).render()
	}
	//endregion
}