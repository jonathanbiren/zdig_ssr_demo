package org.soldatenwohnen.core.katalog.application.port.out

import org.soldatenwohnen.core.katalog.domain.model.Wohnung
import org.soldatenwohnen.core.katalog.domain.model.WohnungId

// Outbound port: the contract the application layer uses to access persistence.
// The application and domain layers depend on this interface.
// The actual database implementation lives in adapter/out/ -- not here.
// Remember that repositories are grouped by aggregate root!
// You can see the advantage of the contract in full effect here. The interface clearly imposes the need for returning
// domain objects on the adapter which implements it, meaning the adapter needs to know the domain type, but the
// application does not need to know anything about the adapter, other than the fact that it returns its domain objects
// ready to use

//Wohnung is one aggregate so one Repository for 'Wohnung' aggregate
//interface IWohnungRepository {
//    fun findAlleVeroeffentlichten(): List<Wohnung>
//    fun findById(id: WohnungId): Wohnung?
//}
