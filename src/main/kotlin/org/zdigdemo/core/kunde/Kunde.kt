package org.zdigdemo.core.kunde

import io.micrometer.core.instrument.config.validate.Validated

class Kunde {
    var name: String = ""
    var kurzname: String = ""
    var adresse: String = ""
    var zsNummer: ZsNummer = ZsNummer.create(1000)
    var status: String = ""
    var besondersSchuetzenswert: Boolean = false

    companion object {
        fun istValideZsNr(zsNr: Long?): Boolean {
            return zsNr != null && zsNr > 999
        }

        fun istValiderKurzname(kurzname: String): Boolean {
            return kurzname.length < 5
        }
    }

    @JvmInline
    value class ZsNummer private constructor(val zsNr: Long) {
        companion object {
            fun create(zsNummer: Long): Validated.Either<ZsNummer, ZsValidationError> =
                zsNummer.takeIf { it > 999 }?.let { ZsNummer(it) }
        }
    }

    sealed interface ZsValidationError {
        data class TooShortError(val errorMessage: String) : ZsValidationError
        data class TooLongError(val errorMessage: String) : ZsValidationError
        data class notANumberError(val errorMessage: String) : ZsValidationError
    }
}