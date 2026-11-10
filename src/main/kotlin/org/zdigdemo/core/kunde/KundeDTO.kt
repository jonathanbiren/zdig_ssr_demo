package org.zdigdemo.core.kunde

import java.util.UUID

data class KundeDTO (
    var id: UUID,
    var name: String,
    var zsNr: String,
    var kurzName: String,
) {
}