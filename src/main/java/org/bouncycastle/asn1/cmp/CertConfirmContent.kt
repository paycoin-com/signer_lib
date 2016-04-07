package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence

class CertConfirmContent private constructor(private val content: ASN1Sequence) : ASN1Object() {

    fun toCertStatusArray(): Array<CertStatus> {
        val result = arrayOfNulls<CertStatus>(content.size())

        for (i in result.indices) {
            result[i] = CertStatus.getInstance(content.getObjectAt(i))
        }

        return result
    }

    /**
     *
     * CertConfirmContent ::= SEQUENCE OF CertStatus
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): CertConfirmContent? {
            if (o is CertConfirmContent) {
                return o
            }

            if (o != null) {
                return CertConfirmContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
