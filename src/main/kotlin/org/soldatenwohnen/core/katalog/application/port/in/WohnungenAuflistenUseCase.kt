package org.soldatenwohnen.core.katalog.application.port.`in`

import org.soldatenwohnen.core.katalog.domain.model.Wohnung

// Inbound port for the "view list of all apartments" use case.
// The controller depends on this interface, never on the service class directly.
// This keeps the adapter layer decoupled from the application layer.
//interface WohnungenAuflistenUseCase {
//    fun alleVeroeffentlichtenAuflisten(): List<Wohnung>
//}
