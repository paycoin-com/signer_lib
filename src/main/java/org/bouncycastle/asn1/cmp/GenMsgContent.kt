package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class GenMsgContent : ASN1Object {
    private var content: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        content = seq
    }

    constructor(itv: InfoTypeAndValue) {
        content = DERSequence(itv)
    }

    constructor(itv: Array<InfoTypeAndValue>) {
        val v = ASN1EncodableVector()
        for (i in itv.indices) {
            v.add(itv[i])
        }
        content = DERSequence(v)
    }

    fun toInfoTypeAndValueArray(): Array<InfoTypeAndValue> {
        val result = arrayOfNulls<InfoTypeAndValue>(content!!.size())

        for (i in result.indices) {
            result[i] = InfoTypeAndValue.getInstance(content!!.getObjectAt(i))
        }

        return result
    }

    /**
     *
     * GenMsgContent ::= SEQUENCE OF InfoTypeAndValue
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): GenMsgContent? {
            if (o is GenMsgContent) {
                return o
            }

            if (o != null) {
                return GenMsgContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
