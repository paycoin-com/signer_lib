package org.bouncycastle.asn1.ocsp

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

class CrlID private constructor(
        seq: ASN1Sequence) : ASN1Object() {
    var crlUrl: DERIA5String? = null
        private set
    var crlNum: ASN1Integer? = null
        private set
    var crlTime: ASN1GeneralizedTime? = null
        private set

    init {
        val e = seq.objects

        while (e.hasMoreElements()) {
            val o = e.nextElement() as ASN1TaggedObject

            when (o.tagNo) {
                0 -> crlUrl = DERIA5String.getInstance(o, true)
                1 -> crlNum = ASN1Integer.getInstance(o, true)
                2 -> crlTime = ASN1GeneralizedTime.getInstance(o, true)
                else -> throw IllegalArgumentException(
                        "unknown tag number: " + o.tagNo)
            }
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * CrlID ::= SEQUENCE {
     * crlUrl               [0]     EXPLICIT IA5String OPTIONAL,
     * crlNum               [1]     EXPLICIT INTEGER OPTIONAL,
     * crlTime              [2]     EXPLICIT GeneralizedTime OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (crlUrl != null) {
            v.add(DERTaggedObject(true, 0, crlUrl))
        }

        if (crlNum != null) {
            v.add(DERTaggedObject(true, 1, crlNum))
        }

        if (crlTime != null) {
            v.add(DERTaggedObject(true, 2, crlTime))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): CrlID? {
            if (obj is CrlID) {
                return obj
            } else if (obj != null) {
                return CrlID(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
