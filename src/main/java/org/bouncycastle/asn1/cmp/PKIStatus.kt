package org.bouncycastle.asn1.cmp

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive

class PKIStatus private constructor(private val value: ASN1Integer) : ASN1Object() {

    private constructor(value: Int) : this(ASN1Integer(value.toLong())) {
    }

    fun getValue(): BigInteger {
        return value.value
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return value
    }

    companion object {
        val GRANTED = 0
        val GRANTED_WITH_MODS = 1
        val REJECTION = 2
        val WAITING = 3
        val REVOCATION_WARNING = 4
        val REVOCATION_NOTIFICATION = 5
        val KEY_UPDATE_WARNING = 6

        val granted = PKIStatus(GRANTED)
        val grantedWithMods = PKIStatus(GRANTED_WITH_MODS)
        val rejection = PKIStatus(REJECTION)
        val waiting = PKIStatus(WAITING)
        val revocationWarning = PKIStatus(REVOCATION_WARNING)
        val revocationNotification = PKIStatus(REVOCATION_NOTIFICATION)
        val keyUpdateWaiting = PKIStatus(KEY_UPDATE_WARNING)

        fun getInstance(o: Any?): PKIStatus? {
            if (o is PKIStatus) {
                return o
            }

            if (o != null) {
                return PKIStatus(ASN1Integer.getInstance(o))
            }

            return null
        }
    }
}
