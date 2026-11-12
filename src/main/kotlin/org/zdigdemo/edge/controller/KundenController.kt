package org.zdigdemo.edge.controller

import gg.jte.generated.precompiled.Templates
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.zdigdemo.edge.repositories.KundenRepository
import org.zdigdemo.edge.repositories.KundenSortColumn
import org.zdigdemo.edge.repositories.SortData
import org.zdigdemo.edge.repositories.SortDirection
import org.zdigdemo.edge.repositories.VerpflichtungRepository
import org.zdigdemo.jooq.tables.records.KundenRecord
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
        templates.pagesKundenIndex(kundenRepository.getAllClients().map { it.toViewModel() }, SortData()).render()

    @GetMapping("/kunden/{id}/edit")
    fun kundenEditieren(@PathVariable id: UUID): String {
        val kunde = kundenRepository.getClient(id)
            ?: return templates.pagesKundenTablePartialsError().render()
        return templates.pagesKundenFormsEditForm(kunde.toDTO()).render()
    }

    @GetMapping("/kunden/create")
    fun kundenAnlegen(): String {
        return templates.pagesKundenFormsEditForm(
            KundeDTO(
                id = UUID.randomUUID(),
                kurzname = "",
                name = "",
                zsNummer = 0,
                adresse = "",
                status = "",
                istBesondersSchuetzenswert = false
            )
        ).render()
    }
    //endregion

/*    @PatchMapping("/kunden/{id}/save")
    fun kundenSpeichern(kunde: KundeDTO): ResponseEntity<Void> {
        kundenRepository.editExistingClient(kunde)
        return ResponseEntity.ok()
            .header("HX-Location", "/")
            .build()
    }*/

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
        val kundenListe = kundenRepository.getClients(sortierInformation, sucheingabe).map { it.toViewModel() }

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

data class KundeDTO(
    var id: UUID,
    var name: String,
    var zsNummer: Long,
    var kurzname: String,
    var adresse: String,
    var status: String,
    var istBesondersSchuetzenswert: Boolean
)

fun KundenRecord.toDTO(): KundeDTO = KundeDTO(
    id = id!!,
    name = name!!,
    zsNummer = zsNummer!!,
    kurzname = kurzname!!,
    adresse = adresse!!,
    status = status!!,
    istBesondersSchuetzenswert = besondersSchuetzenswert!!
)

data class KundeViewModel(
    val id: String,
    val kurzname: String,
    val name: String,
    val zsNummer: Long,
    val adresse: String,
    val status: String,
    val istBesondersSchuetzenswert: Boolean,
)

fun KundenRecord.toViewModel(): KundeViewModel = KundeViewModel(
    id = id.toString(),
    kurzname = kurzname!!,
    name = name!!,
    zsNummer = zsNummer!!,
    adresse = adresse!!,
    status = status!!,
    istBesondersSchuetzenswert = besondersSchuetzenswert!!
)