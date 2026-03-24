package org.soldatenwohnen.katalog.application.port.out

import org.soldatenwohnen.katalog.domain.model.Wohnung
import org.soldatenwohnen.katalog.domain.model.WohnungId
import org.soldatenwohnen.katalog.domain.model.WohnungStatus

// Outbound port: the contract the application layer uses to access persistence.
// The application and domain layers depend on this interface.
// The actual database implementation lives in adapter/out/ -- not here.
interface WohnungRepository {
    fun findAlleVeroeffentlichten(): List<Wohnung>
    fun findById(id: WohnungId): Wohnung?
}
