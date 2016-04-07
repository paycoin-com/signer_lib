package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class Controls : ASN1Object {
    private var content: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        content = seq
    }

    constructor(atv: AttributeTypeAndValue) {
        content = DERSequence(atv)
    }

    constructor(atvs: Array<AttributeTypeAndValue>) {
        val v = ASN1EncodableVector()
        for (i in atvs.indices) {
            v.add(atvs[i])
        }
        content = DERSequence(v)
    }

    fun toAttributeTypeAndValueArray(): Array<AttributeTypeAndValue> {
        val result = arrayOfNulls<AttributeTypeAndValue>(content!!.size())

        for (i in result.indices) {
            result[i] = AttributeTypeAndValue.getInstance(content!!.getObjectAt(i))
        }

        return result
    }

    /**
     *
     * Controls  ::= SEQUENCE SIZE(1..MAX) OF AttributeTypeAndValue
     *

     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): Controls? {
            if (o is Controls) {
                return o
            }

            if (o != null) {
                return Controls(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
