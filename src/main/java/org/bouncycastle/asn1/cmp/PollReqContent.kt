package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class PollReqContent private constructor(private val content: ASN1Sequence) : ASN1Object() {

    /**
     * Create a pollReqContent for a single certReqId.

     * @param certReqId the certificate request ID.
     */
    constructor(certReqId: ASN1Integer) : this(DERSequence(DERSequence(certReqId))) {
    }

    val certReqIds: Array<Array<ASN1Integer>>
        get() {
            val result = arrayOfNulls<Array<ASN1Integer>>(content.size())

            for (i in result.indices) {
                result[i] = sequenceToASN1IntegerArray(content.getObjectAt(i) as ASN1Sequence)
            }

            return result
        }

    /**
     *
     * PollReqContent ::= SEQUENCE OF SEQUENCE {
     * certReqId              INTEGER
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): PollReqContent? {
            if (o is PollReqContent) {
                return o
            }

            if (o != null) {
                return PollReqContent(ASN1Sequence.getInstance(o))
            }

            return null
        }

        private fun sequenceToASN1IntegerArray(seq: ASN1Sequence): Array<ASN1Integer> {
            val result = arrayOfNulls<ASN1Integer>(seq.size())

            for (i in result.indices) {
                result[i] = ASN1Integer.getInstance(seq.getObjectAt(i))
            }

            return result
        }
    }
}
