package org.zdigdemo.core.kunde

import arrow.core.Either
import arrow.core.raise.context.either
import arrow.core.raise.context.ensure
import java.time.OffsetDateTime
import java.util.UUID

data class Kunde(
    val id: UUID,
    val kundenName: KundenName,
    val kundenKurzname: KundenKurzname,
    val adresse: String,
    val kundenZsNummer: KundenZsNummer,
    val status: String,
    val besondersSchuetzenswert: Boolean?,
    val erstelltAm: OffsetDateTime?
)


@JvmInline
value class KundenName private constructor(val value: String) {
    companion object {
        fun create(name: String): Either<NameValidationError, KundenName> = either {
            ensure(name.isNotBlank()) { NameValidationError.EmptyError("Name must not be empty") }
            ensure(name.length < 50) { NameValidationError.TooLongError("Name must be less than 50 characters") }
            KundenName(name)
        }
    }

    sealed interface NameValidationError {
        data class EmptyError(val errorMessage: String) : NameValidationError
        data class TooLongError(val errorMessage: String) : NameValidationError
    }
}

@JvmInline
value class KundenKurzname private constructor(val value: String){
    companion object{
        fun create(kurzname: String): Either<KurznameValidationError, KundenKurzname> = either {
            ensure(kurzname.isNotBlank()) { KurznameValidationError.EmptyError("Kurzname must not be empty") }
            ensure(kurzname.length < 10) { KurznameValidationError.TooLongError("Kurzname must be less than 10 characters") }
            KundenKurzname(kurzname)
        }
    }

    sealed interface KurznameValidationError {
        data class EmptyError(val errorMessage: String) : KurznameValidationError
        data class TooLongError(val errorMessage: String) : KurznameValidationError
    }
}

@JvmInline
value class KundenZsNummer private constructor(val value: Long) {

    companion object {
        fun create(zsNummer: Long?): Either<ZsValidationError, KundenZsNummer> = either {
            ensure(zsNummer != null) { ZsValidationError.NotANumberError("ZS-Nummer muss eine gültige Zahl sein") }
            ensure(zsNummer > 999) { ZsValidationError.TooShortError("ZS-Nummer muss größer als 999 sein") }
            ensure(zsNummer < 10000) { ZsValidationError.TooLongError("ZS-Nummer muss kleiner als 10000 sein") }
            KundenZsNummer(zsNummer)
        }

        fun create(zsNnummer: String): Either<ZsValidationError, KundenZsNummer> = either {
            val parsedLong = zsNnummer.toLongOrNull()
                ?: return Either.Left(ZsValidationError.NotANumberError("ZS-Nummer muss eine gültige Zahl sein"))
            return create(parsedLong)
        }
    }

    sealed interface ZsValidationError {
        val errorMessage: String
        data class TooShortError(override val errorMessage: String) : ZsValidationError
        data class TooLongError(override val errorMessage: String) : ZsValidationError
        data class NotANumberError(override val errorMessage: String) : ZsValidationError
    }
}
