package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence

class POPODecKeyChallContent private constructor(private val content: ASN1Sequence) : ASN1Object() {

    fun toChallengeArray(): Array<Challenge> {
        val result = arrayOfNulls<Challenge>(content.size())

        for (i in result.indices) {
            result[i] = Challenge.getInstance(content.getObjectAt(i))
        }

        return result
    }

    /**
     *
     * POPODecKeyChallContent ::= SEQUENCE OF Challenge
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): POPODecKeyChallContent? {
            if (o is POPODecKeyChallContent) {
                return o
            }

            if (o != null) {
                return POPODecKeyChallContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
