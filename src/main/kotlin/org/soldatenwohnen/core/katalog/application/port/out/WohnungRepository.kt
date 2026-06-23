package org.soldatenwohnen.core.katalog.application.port.out

import org.soldatenwohnen.core.katalog.domain.model.Wohnung
import org.soldatenwohnen.core.katalog.domain.model.WohnungId

// Outbound port: the contract the application layer uses to access persistence.
// The application and domain layers depend on this interface.
// The actual database implementation lives in adapter/out/ -- not here.
// Remember that repositories are grouped by aggregate root!
interface WohnungRepository {
    fun findAlleVeroeffentlichten(): List<Wohnung>
    fun findById(id: WohnungId): Wohnung?
}
