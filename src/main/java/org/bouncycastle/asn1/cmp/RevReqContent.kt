package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class RevReqContent : ASN1Object {
    private var content: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        content = seq
    }

    constructor(revDetails: RevDetails) {
        this.content = DERSequence(revDetails)
    }

    constructor(revDetailsArray: Array<RevDetails>) {
        val v = ASN1EncodableVector()

        for (i in revDetailsArray.indices) {
            v.add(revDetailsArray[i])
        }

        this.content = DERSequence(v)
    }

    fun toRevDetailsArray(): Array<RevDetails> {
        val result = arrayOfNulls<RevDetails>(content!!.size())

        for (i in result.indices) {
            result[i] = RevDetails.getInstance(content!!.getObjectAt(i))
        }

        return result
    }

    /**
     *
     * RevReqContent ::= SEQUENCE OF RevDetails
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): RevReqContent? {
            if (o is RevReqContent) {
                return o
            }

            if (o != null) {
                return RevReqContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
