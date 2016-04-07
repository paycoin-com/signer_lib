package org.bouncycastle.asn1.tsp

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.cmp.PKIStatusInfo
import org.bouncycastle.asn1.cms.ContentInfo


class TimeStampResp : ASN1Object {
    var status: PKIStatusInfo
        internal set

    var timeStampToken: ContentInfo? = null
        internal set

    private constructor(seq: ASN1Sequence) {

        val e = seq.objects

        // status
        status = PKIStatusInfo.getInstance(e.nextElement())

        if (e.hasMoreElements()) {
            timeStampToken = ContentInfo.getInstance(e.nextElement())
        }
    }

    constructor(pkiStatusInfo: PKIStatusInfo, timeStampToken: ContentInfo) {
        this.status = pkiStatusInfo
        this.timeStampToken = timeStampToken
    }

    /**
     *
     * TimeStampResp ::= SEQUENCE  {
     * status                  PKIStatusInfo,
     * timeStampToken          TimeStampToken     OPTIONAL  }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(status)
        if (timeStampToken != null) {
            v.add(timeStampToken)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): TimeStampResp? {
            if (o is TimeStampResp) {
                return o
            } else if (o != null) {
                return TimeStampResp(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
