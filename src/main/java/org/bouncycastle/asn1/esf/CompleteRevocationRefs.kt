package org.bouncycastle.asn1.esf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 *
 * CompleteRevocationRefs ::= SEQUENCE OF CrlOcspRef
 *
 */
class CompleteRevocationRefs : ASN1Object {

    private var crlOcspRefs: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        val seqEnum = seq.objects
        while (seqEnum.hasMoreElements()) {
            CrlOcspRef.getInstance(seqEnum.nextElement())
        }
        this.crlOcspRefs = seq
    }

    constructor(crlOcspRefs: Array<CrlOcspRef>) {
        this.crlOcspRefs = DERSequence(crlOcspRefs)
    }

    fun getCrlOcspRefs(): Array<CrlOcspRef> {
        val result = arrayOfNulls<CrlOcspRef>(this.crlOcspRefs!!.size())
        for (idx in result.indices) {
            result[idx] = CrlOcspRef.getInstance(this.crlOcspRefs!!.getObjectAt(idx))
        }
        return result
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return this.crlOcspRefs
    }

    companion object {

        fun getInstance(obj: Any?): CompleteRevocationRefs? {
            if (obj is CompleteRevocationRefs) {
                return obj
            } else if (obj != null) {
                return CompleteRevocationRefs(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
