package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class CertResponse : ASN1Object {
    var certReqId: ASN1Integer? = null
        private set
    var status: PKIStatusInfo? = null
        private set
    var certifiedKeyPair: CertifiedKeyPair? = null
        private set
    private var rspInfo: ASN1OctetString? = null

    private constructor(seq: ASN1Sequence) {
        certReqId = ASN1Integer.getInstance(seq.getObjectAt(0))
        status = PKIStatusInfo.getInstance(seq.getObjectAt(1))

        if (seq.size() >= 3) {
            if (seq.size() == 3) {
                val o = seq.getObjectAt(2)
                if (o is ASN1OctetString) {
                    rspInfo = ASN1OctetString.getInstance(o)
                } else {
                    certifiedKeyPair = CertifiedKeyPair.getInstance(o)
                }
            } else {
                certifiedKeyPair = CertifiedKeyPair.getInstance(seq.getObjectAt(2))
                rspInfo = ASN1OctetString.getInstance(seq.getObjectAt(3))
            }
        }
    }

    @JvmOverloads constructor(
            certReqId: ASN1Integer?,
            status: PKIStatusInfo?,
            certifiedKeyPair: CertifiedKeyPair? = null,
            rspInfo: ASN1OctetString? = null) {
        if (certReqId == null) {
            throw IllegalArgumentException("'certReqId' cannot be null")
        }
        if (status == null) {
            throw IllegalArgumentException("'status' cannot be null")
        }
        this.certReqId = certReqId
        this.status = status
        this.certifiedKeyPair = certifiedKeyPair
        this.rspInfo = rspInfo
    }

    /**
     *
     * CertResponse ::= SEQUENCE {
     * certReqId           INTEGER,
     * -- to match this response with corresponding request (a value
     * -- of -1 is to be used if certReqId is not specified in the
     * -- corresponding request)
     * status              PKIStatusInfo,
     * certifiedKeyPair    CertifiedKeyPair    OPTIONAL,
     * rspInfo             OCTET STRING        OPTIONAL
     * -- analogous to the id-regInfo-utf8Pairs string defined
     * -- for regInfo in CertReqMsg [CRMF]
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certReqId)
        v.add(status)

        if (certifiedKeyPair != null) {
            v.add(certifiedKeyPair)
        }

        if (rspInfo != null) {
            v.add(rspInfo)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CertResponse? {
            if (o is CertResponse) {
                return o
            }

            if (o != null) {
                return CertResponse(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
