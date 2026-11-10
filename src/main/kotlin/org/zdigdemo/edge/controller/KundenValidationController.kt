package org.zdigdemo.edge.controller

import gg.jte.generated.precompiled.Templates
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.zdigdemo.core.kunde.Kunde
import org.zdigdemo.edge.repositories.KundenRepository

@Controller
@ResponseBody
@RequestMapping(produces = [MediaType.TEXT_HTML_VALUE])
class KundenValidationController(
    private val kundenRepository: KundenRepository,
    private val templates: Templates,
) {
    @GetMapping("/kunden/{id}/validateZsNr")
    fun validateZsNr(@RequestParam("zsNummer", required = false) zsNummer: String?): String {
        val zsNr = zsNummer?.trim()?.toLongOrNull()
        val istValide = Kunde.istValideZsNr(zsNr = zsNr)
        return templates.pagesKundenFormsValidationReturnsZsNrValidation(istValide).render()
    }

}

