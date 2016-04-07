package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class PollRepContent : ASN1Object {
    private var certReqId: Array<ASN1Integer>? = null
    private var checkAfter: Array<ASN1Integer>? = null
    private var reason: Array<PKIFreeText>? = null

    private constructor(seq: ASN1Sequence) {
        certReqId = arrayOfNulls<ASN1Integer>(seq.size())
        checkAfter = arrayOfNulls<ASN1Integer>(seq.size())
        reason = arrayOfNulls<PKIFreeText>(seq.size())

        for (i in 0..seq.size() - 1) {
            val s = ASN1Sequence.getInstance(seq.getObjectAt(i))

            certReqId[i] = ASN1Integer.getInstance(s.getObjectAt(0))
            checkAfter[i] = ASN1Integer.getInstance(s.getObjectAt(1))

            if (s.size() > 2) {
                reason[i] = PKIFreeText.getInstance(s.getObjectAt(2))
            }
        }
    }

    @JvmOverloads constructor(certReqId: ASN1Integer, checkAfter: ASN1Integer, reason: PKIFreeText? = null) {
        this.certReqId = arrayOfNulls<ASN1Integer>(1)
        this.checkAfter = arrayOfNulls<ASN1Integer>(1)
        this.reason = arrayOfNulls<PKIFreeText>(1)

        this.certReqId[0] = certReqId
        this.checkAfter[0] = checkAfter
        this.reason[0] = reason
    }

    fun size(): Int {
        return certReqId!!.size
    }

    fun getCertReqId(index: Int): ASN1Integer {
        return certReqId!![index]
    }

    fun getCheckAfter(index: Int): ASN1Integer {
        return checkAfter!![index]
    }

    fun getReason(index: Int): PKIFreeText {
        return reason!![index]
    }

    /**
     *
     * PollRepContent ::= SEQUENCE OF SEQUENCE {
     * certReqId              INTEGER,
     * checkAfter             INTEGER,  -- time in seconds
     * reason                 PKIFreeText OPTIONAL
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val outer = ASN1EncodableVector()

        for (i in certReqId!!.indices) {
            val v = ASN1EncodableVector()

            v.add(certReqId!![i])
            v.add(checkAfter!![i])

            if (reason!![i] != null) {
                v.add(reason!![i])
            }

            outer.add(DERSequence(v))
        }

        return DERSequence(outer)
    }

    companion object {

        fun getInstance(o: Any?): PollRepContent? {
            if (o is PollRepContent) {
                return o
            }

            if (o != null) {
                return PollRepContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
