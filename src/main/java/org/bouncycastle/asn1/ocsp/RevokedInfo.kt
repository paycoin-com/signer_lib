package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.CRLReason

class RevokedInfo : ASN1Object {
    var revocationTime: ASN1GeneralizedTime? = null
        private set
    var revocationReason: CRLReason? = null
        private set

    constructor(
            revocationTime: ASN1GeneralizedTime,
            revocationReason: CRLReason) {
        this.revocationTime = revocationTime
        this.revocationReason = revocationReason
    }

    private constructor(
            seq: ASN1Sequence) {
        this.revocationTime = ASN1GeneralizedTime.getInstance(seq.getObjectAt(0))

        if (seq.size() > 1) {
            this.revocationReason = CRLReason.getInstance(ASN1Enumerated.getInstance(
                    seq.getObjectAt(1) as ASN1TaggedObject, true))
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * RevokedInfo ::= SEQUENCE {
     * revocationTime              GeneralizedTime,
     * revocationReason    [0]     EXPLICIT CRLReason OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(revocationTime)
        if (revocationReason != null) {
            v.add(DERTaggedObject(true, 0, revocationReason))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): RevokedInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): RevokedInfo? {
            if (obj is RevokedInfo) {
                return obj
            } else if (obj != null) {
                return RevokedInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
