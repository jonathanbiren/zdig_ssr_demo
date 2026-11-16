package org.zdigdemo.core.kunde

import arrow.core.Either
import java.time.OffsetDateTime
import java.util.UUID

data class Kunde private constructor(
    val id: UUID,
    val kundenName: KundenName,
    val kundenKurzname: KundenKurzname,
    val adresse: String,
    val kundenZsNummer: KundenZsNummer,
    val status: String,
    val besondersSchuetzenswert: Boolean?,
    val erstelltAm: OffsetDateTime?
) {
    companion object {
        fun create(
            id: UUID?,
            kundenName: String,
            kundenKurzname: String,
            adresse: String,
            kundenZsNummer: String?,
            status: String,
            besondersSchuetzenswert: Boolean?,
            erstelltAm: OffsetDateTime? = null,
        ): Either<List<KundeValidationError>, Kunde> {
            val errors = mutableListOf<KundeValidationError>()

            if (id == null) {
                errors += KundeValidationError.IdError("Id must not be null")
            }

            val validatedKundenName = KundenName.create(kundenName).fold(
                ifLeft = {
                    errors += it
                    null
                },
                ifRight = { it },
            )

            val validatedKundenKurzname = KundenKurzname.create(kundenKurzname).fold(
                ifLeft = {
                    errors += it
                    null
                },
                ifRight = { it },
            )

            val validatedKundenZsNummer = KundenZsNummer.create(kundenZsNummer).fold(
                ifLeft = {
                    errors += it
                    null
                },
                ifRight = { it },
            )

            return if (errors.isNotEmpty()) {
                Either.Left(errors)
            } else {
                Either.Right(
                    Kunde(
                        id = id!!,
                        kundenName = validatedKundenName!!,
                        kundenKurzname = validatedKundenKurzname!!,
                        adresse = adresse,
                        kundenZsNummer = validatedKundenZsNummer!!,
                        status = status,
                        besondersSchuetzenswert = besondersSchuetzenswert,
                        erstelltAm = erstelltAm,
                    )
                )
            }
        }
    }

    sealed interface KundeValidationError {
        val errorMessage: String

        data class IdError(
            override val errorMessage: String,
        ) : KundeValidationError
    }
}


@JvmInline
value class KundenName private constructor(val value: String) {
    companion object {
        fun create(name: String): Either<List<NameValidationError>, KundenName> {
            val errors = mutableListOf<NameValidationError>()

            if (name.isBlank()) {
                errors += NameValidationError.EmptyError()
            }

            if (name.length >= 50) {
                errors += NameValidationError.TooLongError()
            }

            return if (errors.isNotEmpty()) {
                Either.Left(errors)
            } else {
                Either.Right(KundenName(name))
            }
        }
    }

    sealed interface NameValidationError : Kunde.KundeValidationError {
        override val errorMessage: String

        data class EmptyError(
            override val errorMessage: String = "Name darf nicht leer sein",
        ) : NameValidationError

        data class TooLongError(
            override val errorMessage: String = "Name muss weniger als 50 Zeichen lang sein",
        ) : NameValidationError
    }
}

@JvmInline
value class KundenKurzname private constructor(val value: String) {
    companion object {
        fun create(kurzname: String): Either<List<KurznameValidationError>, KundenKurzname> {
            val errors = mutableListOf<KurznameValidationError>()

            if (kurzname.isBlank()) {
                errors += KurznameValidationError.EmptyError()
            }

            if (kurzname.length >= 10) {
                errors += KurznameValidationError.TooLongError()
            }

            return if (errors.isNotEmpty()) {
                Either.Left(errors)
            } else {
                Either.Right(KundenKurzname(kurzname))
            }
        }
    }

    sealed interface KurznameValidationError : Kunde.KundeValidationError {
        override val errorMessage: String

        data class EmptyError(
            override val errorMessage: String = "Kurzname darf nicht leer sein",
        ) : KurznameValidationError

        data class TooLongError(
            override val errorMessage: String = "Kurzname muss weniger als 10 Zeichen lang sein",
        ) : KurznameValidationError
    }
}

@JvmInline
value class KundenZsNummer private constructor(val value: Long) {

    companion object {
        fun create(zsNummer: Long?): Either<List<ZsValidationError>, KundenZsNummer> {
            val errors = mutableListOf<ZsValidationError>()

            if (zsNummer == null) {
                errors += ZsValidationError.NotANumberError()
            } else {
                if (zsNummer <= 999) {
                    errors += ZsValidationError.TooShortError()
                }

                if (zsNummer >= 100000) {
                    errors += ZsValidationError.TooLongError()
                }
            }

            return if (errors.isNotEmpty()) {
                Either.Left(errors)
            } else {
                Either.Right(KundenZsNummer(zsNummer!!))
            }
        }

        fun create(zsNnummer: String?): Either<List<ZsValidationError>, KundenZsNummer> {
            val parsedLong = zsNnummer?.toLongOrNull()
            return create(parsedLong)
        }
    }

    sealed interface ZsValidationError : Kunde.KundeValidationError {
        override val errorMessage: String

        data class TooShortError(
            override val errorMessage: String = "ZS-Nummer muss größer als 999 sein",
        ) : ZsValidationError

        data class TooLongError(
            override val errorMessage: String = "ZS-Nummer muss kleiner als 100000 sein",
        ) : ZsValidationError

        data class NotANumberError(
            override val errorMessage: String = "ZS-Nummer muss eine gültige Zahl sein",
        ) : ZsValidationError
    }
}
