package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.X509Extensions

class SingleResponse : ASN1Object {
    var certID: CertID? = null
        private set
    var certStatus: CertStatus? = null
        private set
    var thisUpdate: ASN1GeneralizedTime? = null
        private set
    var nextUpdate: ASN1GeneralizedTime? = null
        private set
    var singleExtensions: Extensions? = null
        private set

    /**
     * @param certID
     * *
     * @param certStatus
     * *
     * @param thisUpdate
     * *
     * @param nextUpdate
     * *
     * @param singleExtensions
     */
    @Deprecated("use method taking ASN1GeneralizedTime and Extensions\n      ")
    constructor(
            certID: CertID,
            certStatus: CertStatus,
            thisUpdate: ASN1GeneralizedTime,
            nextUpdate: ASN1GeneralizedTime,
            singleExtensions: X509Extensions) : this(certID, certStatus, thisUpdate, nextUpdate, Extensions.getInstance(singleExtensions)) {
    }

    constructor(
            certID: CertID,
            certStatus: CertStatus,
            thisUpdate: ASN1GeneralizedTime,
            nextUpdate: ASN1GeneralizedTime,
            singleExtensions: Extensions) {
        this.certID = certID
        this.certStatus = certStatus
        this.thisUpdate = thisUpdate
        this.nextUpdate = nextUpdate
        this.singleExtensions = singleExtensions
    }

    private constructor(
            seq: ASN1Sequence) {
        this.certID = CertID.getInstance(seq.getObjectAt(0))
        this.certStatus = CertStatus.getInstance(seq.getObjectAt(1))
        this.thisUpdate = ASN1GeneralizedTime.getInstance(seq.getObjectAt(2))

        if (seq.size() > 4) {
            this.nextUpdate = ASN1GeneralizedTime.getInstance(
                    seq.getObjectAt(3) as ASN1TaggedObject, true)
            this.singleExtensions = Extensions.getInstance(
                    seq.getObjectAt(4) as ASN1TaggedObject, true)
        } else if (seq.size() > 3) {
            val o = seq.getObjectAt(3) as ASN1TaggedObject

            if (o.tagNo == 0) {
                this.nextUpdate = ASN1GeneralizedTime.getInstance(o, true)
            } else {
                this.singleExtensions = Extensions.getInstance(o, true)
            }
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * SingleResponse ::= SEQUENCE {
     * certID                       CertID,
     * certStatus                   CertStatus,
     * thisUpdate                   GeneralizedTime,
     * nextUpdate         [0]       EXPLICIT GeneralizedTime OPTIONAL,
     * singleExtensions   [1]       EXPLICIT Extensions OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certID)
        v.add(certStatus)
        v.add(thisUpdate)

        if (nextUpdate != null) {
            v.add(DERTaggedObject(true, 0, nextUpdate))
        }

        if (singleExtensions != null) {
            v.add(DERTaggedObject(true, 1, singleExtensions))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): SingleResponse {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): SingleResponse? {
            if (obj is SingleResponse) {
                return obj
            } else if (obj != null) {
                return SingleResponse(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
