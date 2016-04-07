package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Null
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERNull

class PKIConfirmContent : ASN1Object {
    private var `val`: ASN1Null? = null

    private constructor(`val`: ASN1Null) {
        this.`val` = `val`
    }

    constructor() {
        `val` = DERNull.INSTANCE
    }

    /**
     *
     * PKIConfirmContent ::= NULL
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return `val`
    }

    companion object {

        fun getInstance(o: Any?): PKIConfirmContent {
            if (o == null || o is PKIConfirmContent) {
                return o as PKIConfirmContent?
            }

            if (o is ASN1Null) {
                return PKIConfirmContent(o as ASN1Null?)
            }

            throw IllegalArgumentException("Invalid object: " + o.javaClass.name)
        }
    }
}
