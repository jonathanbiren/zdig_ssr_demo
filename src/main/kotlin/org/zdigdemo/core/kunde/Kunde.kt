package org.zdigdemo.core.kunde

class Kunde {
    var name: String = ""
    var kurzname: String = ""
    var adresse: String = ""
    var zsNr: Long = 0
    var status: String = ""
    var besondersSchuetzenswert: Boolean = false

    companion object    {
        fun istValideZsNr(zsNr: Long?): Boolean {
                return zsNr != null && zsNr>999
        }

        fun istValiderKurzname(kurzname: String): Boolean {
            return kurzname.length<5
        }
    }
}