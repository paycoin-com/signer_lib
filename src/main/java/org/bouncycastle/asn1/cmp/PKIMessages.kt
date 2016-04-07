package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class PKIMessages : ASN1Object {
    private var content: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        content = seq
    }

    constructor(msg: PKIMessage) {
        content = DERSequence(msg)
    }

    constructor(msgs: Array<PKIMessage>) {
        val v = ASN1EncodableVector()
        for (i in msgs.indices) {
            v.add(msgs[i])
        }
        content = DERSequence(v)
    }

    fun toPKIMessageArray(): Array<PKIMessage> {
        val result = arrayOfNulls<PKIMessage>(content!!.size())

        for (i in result.indices) {
            result[i] = PKIMessage.getInstance(content!!.getObjectAt(i))
        }

        return result
    }

    /**
     *
     * PKIMessages ::= SEQUENCE SIZE (1..MAX) OF PKIMessage
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): PKIMessages? {
            if (o is PKIMessages) {
                return o
            }

            if (o != null) {
                return PKIMessages(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
