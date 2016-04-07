package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERIA5String

class SPuri(
        val uri: DERIA5String) {

    /**
     *
     * SPuri ::= IA5String
     *
     */
    fun toASN1Primitive(): ASN1Primitive {
        return uri.toASN1Primitive()
    }

    companion object {

        fun getInstance(
                obj: Any): SPuri? {
            if (obj is SPuri) {
                return obj
            } else if (obj is DERIA5String) {
                return SPuri(DERIA5String.getInstance(obj))
            }

            return null
        }
    }
}
