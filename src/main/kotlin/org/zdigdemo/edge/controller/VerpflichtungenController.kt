package org.zdigdemo.edge.controller

import gg.jte.generated.precompiled.Templates
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.zdigdemo.edge.repositories.SortDirection
import org.zdigdemo.edge.repositories.VerpflichtungPage
import org.zdigdemo.edge.repositories.VerpflichtungRepository
import org.zdigdemo.edge.repositories.VerpflichtungSortColumn
import org.zdigdemo.edge.repositories.VerpflichtungSortData
import java.util.UUID


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
		val sortierInformation = createSortData(
			sortColumn = sortColumn,
			sortDirection = sortDirection
		)

		val verpflichtungenPage = verpflichtungRepository.getVerpflichtungen(
			sortierInformation = sortierInformation,
			sucheingabe = sucheingabe
		)

		return if (hxRequest == "true") {
			renderSearchResult(
				verpflichtungenPage = verpflichtungenPage,
				sortierInformation = sortierInformation
			)
		} else {
			renderIndexPage(
				verpflichtungenPage = verpflichtungenPage,
				sortierInformation = sortierInformation
			)
		}
	}

	@HxRequest
	@GetMapping("/verpflichtungen/nextPage/{cursor}")
	fun nextPage(
		@PathVariable("cursor") cursor: UUID,
		@RequestParam(required = false) sortColumn: String?,
		@RequestParam(required = false) sortDirection: String?,
		@RequestParam(required = false) sucheingabe: String?
	): String {
		val sortierInformation = createSortData(
			sortColumn = sortColumn,
			sortDirection = sortDirection
		)

		val verpflichtungenPage = verpflichtungRepository.getNextVerpflichtungenPage(
			lastSeenId = cursor,
			sortierInformation = sortierInformation,
			sucheingabe = sucheingabe
		)

		return templates.pagesVerpflichtungenPartialsTablePage(
			verpflichtungenListe = verpflichtungenPage,
			sortierInformation = sortierInformation
		).render()
	}

	private fun createSortData(
		sortColumn: String?,
		sortDirection: String?,
	): VerpflichtungSortData =
		VerpflichtungSortData(
			sortColumn = VerpflichtungSortColumn.fromRequestName(sortColumn),
			sortDirection = SortDirection.create(sortDirection)
		)

	private fun renderSearchResult(
		verpflichtungenPage: VerpflichtungPage,
		sortierInformation: VerpflichtungSortData,
	): String =
		templates.pagesVerpflichtungenPartialsObligationTableWithSearchResultAmount(
			verpflichtungenListe = verpflichtungenPage.data,
			anzahlSuchergebnisse = verpflichtungenPage.amountSearchResult,
			sortierInformation = sortierInformation
		).render()

	private fun renderIndexPage(
		verpflichtungenPage: VerpflichtungPage,
		sortierInformation: VerpflichtungSortData,
	): String =
		templates.pagesVerpflichtungenIndex(
			verpflichtungenListe = verpflichtungenPage.data,
			gesamtanzahlVerpflichtungen = verpflichtungRepository.getAllVerpflichtungenCount(),
			sortierInformation = sortierInformation
		).render()
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
