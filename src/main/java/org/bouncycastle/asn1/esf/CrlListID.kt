package org.bouncycastle.asn1.esf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 *
 * CRLListID ::= SEQUENCE {
 * crls SEQUENCE OF CrlValidatedID }
 *
 */
class CrlListID : ASN1Object {

    private var crls: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        this.crls = seq.getObjectAt(0) as ASN1Sequence
        val e = this.crls!!.objects
        while (e.hasMoreElements()) {
            CrlValidatedID.getInstance(e.nextElement())
        }
    }

    constructor(crls: Array<CrlValidatedID>) {
        this.crls = DERSequence(crls)
    }

    fun getCrls(): Array<CrlValidatedID> {
        val result = arrayOfNulls<CrlValidatedID>(this.crls!!.size())
        for (idx in result.indices) {
            result[idx] = CrlValidatedID.getInstance(this.crls!!.getObjectAt(idx))
        }
        return result
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return DERSequence(this.crls)
    }

    companion object {

        fun getInstance(obj: Any?): CrlListID? {
            if (obj is CrlListID) {
                return obj
            } else if (obj != null) {
                return CrlListID(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
