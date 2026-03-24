package org.soldatenwohnen.katalog.application.service

import org.soldatenwohnen.katalog.application.port.`in`.WohnungDetailAnzeigenUseCase
import org.soldatenwohnen.katalog.application.port.`in`.WohnungenAuflistenUseCase
import org.soldatenwohnen.katalog.application.port.out.WohnungRepository
import org.soldatenwohnen.katalog.domain.model.Wohnung
import org.soldatenwohnen.katalog.domain.model.WohnungId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// Both use cases are grouped here because they share the same primary aggregate
// (Wohnung) and both are read-only query operations -- cohesive by nature.
// No business logic lives here: the service only orchestrates loading and returning.
// The service directly talks to the repo. This is what we would call a transaction script
@Service
@Transactional(readOnly = true)
class WohnungssucheService(
    private val wohnungRepository: WohnungRepository
) : WohnungenAuflistenUseCase, WohnungDetailAnzeigenUseCase {

    override fun alleVeroeffentlichtenAuflisten(): List<Wohnung> =
        wohnungRepository.findAlleVeroeffentlichten()

    override fun detailAnzeigen(id: WohnungId): Wohnung? =
        wohnungRepository.findById(id)
}
