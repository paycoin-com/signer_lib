package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence

class POPODecKeyRespContent private constructor(private val content: ASN1Sequence) : ASN1Object() {

    fun toASN1IntegerArray(): Array<ASN1Integer> {
        val result = arrayOfNulls<ASN1Integer>(content.size())

        for (i in result.indices) {
            result[i] = ASN1Integer.getInstance(content.getObjectAt(i))
        }

        return result
    }

    /**
     *
     * POPODecKeyRespContent ::= SEQUENCE OF INTEGER
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): POPODecKeyRespContent? {
            if (o is POPODecKeyRespContent) {
                return o
            }

            if (o != null) {
                return POPODecKeyRespContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
