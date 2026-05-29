package org.soldatenwohnen.adapter.`in`

import org.soldatenwohnen.core.katalog.application.port.`in`.WohnungDetailAnzeigenUseCase
import org.soldatenwohnen.core.katalog.application.port.`in`.WohnungenAuflistenUseCase
import org.soldatenwohnen.core.katalog.domain.model.Wohnung
import org.soldatenwohnen.core.katalog.domain.model.WohnungId
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Controller
class KatalogPageController(
    private val wohnungenAuflisten: WohnungenAuflistenUseCase,
    private val wohnungDetailAnzeigen: WohnungDetailAnzeigenUseCase
) {
    @GetMapping("/", "/wohnungen")
    fun wohnungen(model: Model): String {
        model.addAttribute("wohnungen", wohnungenAuflisten.alleVeroeffentlichtenAuflisten().toCards())
        model.addAttribute("query", "")
        return "wohnungen/index"
    }

    @GetMapping("/wohnungen/suche")
    fun wohnungenSuchen(@RequestParam("q", required = false) query: String?, model: Model): String {
        model.addAttribute("wohnungen", wohnungenAuflisten.alleVeroeffentlichtenAuflisten().filterBy(query).toCards())
        model.addAttribute("query", query.orEmpty())
        return "wohnungen/fragments :: wohnungListe"
    }

    @GetMapping("/wohnungen/{id}")
    fun wohnungDetail(@PathVariable id: UUID, model: Model): String {
        val wohnung = wohnungDetailAnzeigen.detailAnzeigen(WohnungId(id)) ?: throw notFound()
        model.addAttribute("wohnung", wohnung.toDetail())
        return "wohnungen/detail"
    }

    @GetMapping("/wohnungen/{id}/kontakt")
    fun kontakt(@PathVariable id: UUID, model: Model): String {
        val wohnung = wohnungDetailAnzeigen.detailAnzeigen(WohnungId(id)) ?: throw notFound()
        model.addAttribute("wohnung", wohnung.toDetail())
        return "wohnungen/fragments :: kontakt"
    }

    private fun notFound() = ResponseStatusException(HttpStatus.NOT_FOUND, "Wohnung nicht gefunden")
}

data class WohnungCardView(
    val id: UUID,
    val titel: String,
    val beschreibungKurz: String,
    val stadt: String,
    val postleitzahl: String,
    val quadratmeter: String,
    val zimmeranzahl: String,
    val mietpreis: String,
    val erstelltAm: String
)

data class WohnungDetailView(
    val id: UUID,
    val titel: String,
    val beschreibung: String,
    val adresse: String,
    val stadt: String,
    val postleitzahl: String,
    val quadratmeter: String,
    val zimmeranzahl: String,
    val mietpreis: String,
    val erstelltAm: String,
    val mailtoHref: String
)

private val germany: Locale = Locale.GERMANY
private val berlin: ZoneId = ZoneId.of("Europe/Berlin")
private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(berlin)

private fun List<Wohnung>.filterBy(query: String?): List<Wohnung> {
    val normalizedQuery = query?.trim()?.lowercase(germany).orEmpty()
    if (normalizedQuery.isBlank()) return this

    return filter { wohnung ->
        listOf(
            wohnung.titel,
            wohnung.beschreibung,
            wohnung.adresse.stadt,
            wohnung.adresse.postleitzahl
        ).any { value -> normalizedQuery in value.lowercase(germany) }
    }
}

private fun List<Wohnung>.toCards(): List<WohnungCardView> = map { wohnung -> wohnung.toCard() }

private fun Wohnung.toCard() = WohnungCardView(
    id = id.value,
    titel = titel,
    beschreibungKurz = beschreibung.abbreviate(150),
    stadt = adresse.stadt,
    postleitzahl = adresse.postleitzahl,
    quadratmeter = quadratmeter.formatNumber(),
    zimmeranzahl = zimmeranzahl.formatNumber(),
    mietpreis = mietpreis.inEuro().formatCurrency(),
    erstelltAm = erstelltAm.formatDate()
)

private fun Wohnung.toDetail() = WohnungDetailView(
    id = id.value,
    titel = titel,
    beschreibung = beschreibung,
    adresse = "${adresse.strasse} ${adresse.hausnummer}",
    stadt = adresse.stadt,
    postleitzahl = adresse.postleitzahl,
    quadratmeter = quadratmeter.formatNumber(),
    zimmeranzahl = zimmeranzahl.formatNumber(),
    mietpreis = mietpreis.inEuro().formatCurrency(),
    erstelltAm = erstelltAm.formatDate(),
    mailtoHref = "mailto:vermittlung@soldaten-wohnen.local?subject=" +
        URLEncoder.encode("Anfrage zu $titel", StandardCharsets.UTF_8)
)

private fun String.abbreviate(maxLength: Int): String =
    if (length <= maxLength) this else take(maxLength - 3).trimEnd() + "..."

private fun BigDecimal.formatNumber(): String = NumberFormat.getNumberInstance(germany).apply {
    maximumFractionDigits = 2
}.format(this)

private fun BigDecimal.formatCurrency(): String = NumberFormat.getCurrencyInstance(germany).format(this)

private fun Instant.formatDate(): String = dateFormatter.format(this)
