package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class AttCertValidityPeriod : ASN1Object {
    var notBeforeTime: ASN1GeneralizedTime
        internal set
    var notAfterTime: ASN1GeneralizedTime
        internal set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        notBeforeTime = ASN1GeneralizedTime.getInstance(seq.getObjectAt(0))
        notAfterTime = ASN1GeneralizedTime.getInstance(seq.getObjectAt(1))
    }

    /**
     * @param notBeforeTime
     * *
     * @param notAfterTime
     */
    constructor(
            notBeforeTime: ASN1GeneralizedTime,
            notAfterTime: ASN1GeneralizedTime) {
        this.notBeforeTime = notBeforeTime
        this.notAfterTime = notAfterTime
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * AttCertValidityPeriod  ::= SEQUENCE {
     * notBeforeTime  GeneralizedTime,
     * notAfterTime   GeneralizedTime
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(notBeforeTime)
        v.add(notAfterTime)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): AttCertValidityPeriod? {
            if (obj is AttCertValidityPeriod) {
                return obj
            } else if (obj != null) {
                return AttCertValidityPeriod(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
