package org.zdigdemo.edge.controller

import gg.jte.generated.precompiled.Templates
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.zdigdemo.edge.repositories.KundenRepository
import org.zdigdemo.edge.repositories.SortDirection
import org.zdigdemo.edge.repositories.VerpflichtungRepository
import org.zdigdemo.edge.repositories.VerpflichtungSortColumn
import org.zdigdemo.edge.repositories.VerpflichtungSortData


@Controller
@ResponseBody
@RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
class VerpflichtungenController(
	private val verpflichtungRepository: VerpflichtungRepository,
	private val templates: Templates
) {

	@GetMapping("/verpflichtungen")
	fun index(
		@RequestParam(required = false) sortColumn: String?,
		@RequestParam(required = false) sortDirection: String?,
		@RequestParam(required = false) sucheingabe: String?,
		@RequestHeader("HX-Request", required = false) hxRequest: String?
	): String {
		val sortierInformation = VerpflichtungSortData(
			sortColumn = VerpflichtungSortColumn.fromRequestName(sortColumn),
			sortDirection = SortDirection.create(sortDirection)
		)

		val verpflichtungen = verpflichtungRepository.getVerpflichtungen(
			sortierInformation = sortierInformation,
			sucheingabe = sucheingabe
		)

		return if (hxRequest == "true") {
			templates.pagesVerpflichtungenComponentsObligationListWithDetailView(
				verpflichtungenListe = verpflichtungen,
				sortierInformation = sortierInformation
			).render()
		} else {
			templates.pagesVerpflichtungenIndex(
				verpflichtungen,
				sortierInformation
			).render()
		}
	}
}

data class VerpflichtungViewModel2(
	val id: String,
	val kundeId: String,
	val kundeName: String,
	val personId: String,
	val personName: String,
	val verpflichtungName: String,
	val verpflichtungBetrag: String,
)
