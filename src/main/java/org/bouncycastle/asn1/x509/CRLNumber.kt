package org.bouncycastle.asn1.x509

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive

/**
 * The CRLNumber object.
 *
 * CRLNumber::= INTEGER(0..MAX)
 *
 */
class CRLNumber(
        val crlNumber: BigInteger) : ASN1Object() {

    override fun toString(): String {
        return "CRLNumber: " + crlNumber
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return ASN1Integer(crlNumber)
    }

    companion object {

        fun getInstance(o: Any?): CRLNumber? {
            if (o is CRLNumber) {
                return o
            } else if (o != null) {
                return CRLNumber(ASN1Integer.getInstance(o).value)
            }

            return null
        }
    }
}
