package org.soldatenwohnen.core.katalog.application.service

import org.soldatenwohnen.core.katalog.application.port.`in`.WohnungDetailAnzeigenUseCase
import org.soldatenwohnen.core.katalog.application.port.`in`.WohnungenAuflistenUseCase
import org.soldatenwohnen.core.katalog.application.port.out.WohnungRepository
import org.soldatenwohnen.core.katalog.domain.model.Wohnung
import org.soldatenwohnen.core.katalog.domain.model.WohnungId
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// Both use cases are grouped here because they share the same primary aggregate
// (Wohnung) and both are read-only query operations -- cohesive by nature.
// No business logic lives here: the service only orchestrates loading and returning.
// The use cases are implemented by the service directly talking to the repo. This is what we would call
// a transaction script. You will have to decide on an individual bases which use cases inside of a bounded context
// should be simple transaction scripts and which should be implemented using rich domain models or even
// event-sourced domain models.
@Service
@Transactional(readOnly = true)
@Scope("singleton")
class WohnungssucheService(
      private val wohnungRepository: WohnungRepository
) : WohnungenAuflistenUseCase, WohnungDetailAnzeigenUseCase {

      override fun alleVeroeffentlichtenAuflisten(): List<Wohnung> =
            wohnungRepository.findAlleVeroeffentlichten()

      override fun detailAnzeigen(id: WohnungId): Wohnung? =
            wohnungRepository.findById(id)
}
