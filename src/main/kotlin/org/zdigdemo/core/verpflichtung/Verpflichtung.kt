package org.zdigdemo.core.verpflichtung

import arrow.core.Either
import java.time.OffsetDateTime
import java.util.UUID

data class Verpflichtung private constructor(
    val id: UUID,
    val kundenId: UUID,
    val personenId: UUID?,
    val verpflichtungsBezeichnung: Verplichtungsbezeichnung,
    val verpflichtungsBetrag: Verpflichtungsbetrag,
    val erstelldatum: OffsetDateTime
) {
    companion object {
        fun create(
            id: UUID?,
            kundenId: UUID,
            personenId: UUID?,
            verpflichtungsBezeichnung: String,
            verpflichtungsBetrag: String,
            erstelldatum: OffsetDateTime
        ): Either<List<VerpflichtungValidationError>, Verpflichtung> {
            val errors = mutableListOf<VerpflichtungValidationError>()

            if (id == null) {
                errors.add(VerpflichtungValidationError.IdError("Id darf nicht null sein"))
            }

            val validierteVerplichtungsbezeichnung = Verplichtungsbezeichnung.create(verpflichtungsBezeichnung).fold(
                ifLeft = {
                    errors.addAll(it)
                    null
                },
                ifRight = {
                    it
                }
            )

            val validierterVerpflichtungsBetrag = Verpflichtungsbetrag.create(verpflichtungsBetrag).fold(
                ifLeft = {
                    errors.addAll(it)
                    null
                },
                ifRight = {
                    it
                }
            )

            return if (errors.isNotEmpty()) {
                Either.Left(errors)
            } else {
                Either.Right(
                    Verpflichtung(
                        id = id!!,
                        kundenId = kundenId,
                        personenId = personenId,
                        verpflichtungsBezeichnung = validierteVerplichtungsbezeichnung!!,
                        verpflichtungsBetrag = validierterVerpflichtungsBetrag!!,
                        erstelldatum = erstelldatum
                    )
                )
            }
        }
    }

    sealed interface VerpflichtungValidationError {
        val errorMessage: String

        data class IdError(override val errorMessage: String) : VerpflichtungValidationError
    }
}

@JvmInline
value class Verplichtungsbezeichnung private constructor(val value: String) {
    companion object {
        fun create(verpflichtungsbezeichnung: String): Either<List<VerpflichtungsbezeichnungValidationError>, Verplichtungsbezeichnung> {
            val errors = mutableListOf<VerpflichtungsbezeichnungValidationError>()

            if (verpflichtungsbezeichnung.isBlank()) {
                errors.add(VerpflichtungsbezeichnungValidationError.EmptyError())
            }
            if (verpflichtungsbezeichnung.length > 100) {
                errors.add(VerpflichtungsbezeichnungValidationError.TooLongError())
            }
            if (!verpflichtungsbezeichnung.matches(Regex("^[a-zA-Z0-9 .,:()]+$"))) {
                errors.add(VerpflichtungsbezeichnungValidationError.SpecialCharacterError())
            }

            return if (errors.isNotEmpty()) {
                Either.Left(errors)
            } else {
                Either.Right(Verplichtungsbezeichnung(verpflichtungsbezeichnung))
            }
        }
    }

    sealed interface VerpflichtungsbezeichnungValidationError : Verpflichtung.VerpflichtungValidationError {
        override val errorMessage: String

        data class EmptyError(override val errorMessage: String = "Verpflichtungsbezeichnung darf nicht leer sein") :
            VerpflichtungsbezeichnungValidationError

        data class TooLongError(override val errorMessage: String = "Verpflichtungsbezeichnung darf nicht länger als 100 Zeichen sein") :
            VerpflichtungsbezeichnungValidationError

        data class SpecialCharacterError(override val errorMessage: String = "Verpflichtungsbezeichnung darf nur Buchstaben, Zahlen und Leerzeichen, sowie \".\" \",\" \":\" \"(\" und \")\" enthalten") :
            VerpflichtungsbezeichnungValidationError
    }
}

@JvmInline
value class Verpflichtungsbetrag private constructor(val value: Long) {
    companion object {
        fun create(verpflichtungsbetrag: String): Either<List<VerpflichtungsbetragValidationError>, Verpflichtungsbetrag> {
            val errors = mutableListOf<VerpflichtungsbetragValidationError>()
            val normalizedValue = verpflichtungsbetrag.trim().replace(',', '.')

            if (!normalizedValue.matches(Regex("""^-?\d+(\.\d{1,2})?$"""))) {
                errors.add(VerpflichtungsbetragValidationError.InvalidFormatError())
            }

            if (normalizedValue.startsWith("-")) {
                errors.add(VerpflichtungsbetragValidationError.NegativeError())
            }

            val centBetrag = if (errors.isEmpty()) {
                val parts = normalizedValue.split('.')
                val euroPart = parts[0].toLong()
                val centPart = parts.getOrNull(1)?.padEnd(2, '0')?.toLong() ?: 0L

                euroPart * 100 + centPart
            } else {
                null
            }

            return if (errors.isNotEmpty()) {
                Either.Left(errors)
            } else {
                Either.Right(Verpflichtungsbetrag(centBetrag!!))
            }
        }

    }

    sealed interface VerpflichtungsbetragValidationError : Verpflichtung.VerpflichtungValidationError {
        data class NegativeError(override val errorMessage: String = "Verpflichtungsbetrag darf nicht negativ sein") :
            VerpflichtungsbetragValidationError

        data class InvalidFormatError(override val errorMessage: String = "Verpflichtungsbetrag darf nur ganze Cent-Beträge enthalten") :
            VerpflichtungsbetragValidationError
    }
}