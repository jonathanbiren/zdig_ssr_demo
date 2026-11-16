package org.zdigdemo.edge.controller

import gg.jte.generated.precompiled.Templates
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.zdigdemo.core.kunde.KundenKurzname
import org.zdigdemo.core.kunde.KundenName
import org.zdigdemo.core.kunde.KundenZsNummer
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
        val validationResult = KundenZsNummer.create(zsNummer?.trim() ?: "")
        return validationResult.fold(
            ifLeft = { errors ->
                errors.joinToString(separator = "<br>") { error ->
                    templates.pagesKundenFormsValidationReturnsActiveValidationError(error).render()
                }
            },
            ifRight = { """<span></span>""" }
        )
    }

    @GetMapping("/kunden/{id}/validateName")
    fun validateName(@RequestParam("name", required = false) name: String?): String {
        val validationResult = KundenName.create(name?.trim() ?: "")
        return validationResult.fold(
            ifLeft = { errors ->
                errors.joinToString(separator = "<br>") { error ->
                    templates.pagesKundenFormsValidationReturnsActiveValidationError(error).render()
                }
            },
            ifRight = { """<span></span>""" }
        )
    }

    @GetMapping("/kunden/{id}/validateKurzname")
    fun validateKurzname(@RequestParam("kurzname", required = false) kurzname: String?): String {
        val validationResult = KundenKurzname.create(kurzname?.trim() ?: "")
        return validationResult.fold(
            ifLeft = { errors ->
                errors.joinToString(separator = "<br>") { error ->
                    templates.pagesKundenFormsValidationReturnsActiveValidationError(error).render()
                }
            },
            ifRight = { """<span></span>""" }
        )
    }
}

