package org.bouncycastle.asn1.esf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 *
 * OcspListID ::=  SEQUENCE {
 * ocspResponses  SEQUENCE OF OcspResponsesID
 * }
 *
 */
class OcspListID : ASN1Object {
    private var ocspResponses: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 1) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        this.ocspResponses = seq.getObjectAt(0) as ASN1Sequence
        val e = this.ocspResponses!!.objects
        while (e.hasMoreElements()) {
            OcspResponsesID.getInstance(e.nextElement())
        }
    }

    constructor(ocspResponses: Array<OcspResponsesID>) {
        this.ocspResponses = DERSequence(ocspResponses)
    }

    fun getOcspResponses(): Array<OcspResponsesID> {
        val result = arrayOfNulls<OcspResponsesID>(this.ocspResponses!!.size())
        for (idx in result.indices) {
            result[idx] = OcspResponsesID.getInstance(this.ocspResponses!!.getObjectAt(idx))
        }
        return result
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return DERSequence(this.ocspResponses)
    }

    companion object {

        fun getInstance(obj: Any?): OcspListID? {
            if (obj is OcspListID) {
                return obj
            } else if (obj != null) {
                return OcspListID(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
