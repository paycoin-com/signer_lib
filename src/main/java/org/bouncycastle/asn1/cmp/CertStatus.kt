package org.bouncycastle.asn1.cmp

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence

class CertStatus : ASN1Object {
    var certHash: ASN1OctetString? = null
        private set
    var certReqId: ASN1Integer? = null
        private set
    var statusInfo: PKIStatusInfo? = null
        private set

    private constructor(seq: ASN1Sequence) {
        certHash = ASN1OctetString.getInstance(seq.getObjectAt(0))
        certReqId = ASN1Integer.getInstance(seq.getObjectAt(1))

        if (seq.size() > 2) {
            statusInfo = PKIStatusInfo.getInstance(seq.getObjectAt(2))
        }
    }

    constructor(certHash: ByteArray, certReqId: BigInteger) {
        this.certHash = DEROctetString(certHash)
        this.certReqId = ASN1Integer(certReqId)
    }

    constructor(certHash: ByteArray, certReqId: BigInteger, statusInfo: PKIStatusInfo) {
        this.certHash = DEROctetString(certHash)
        this.certReqId = ASN1Integer(certReqId)
        this.statusInfo = statusInfo
    }

    /**
     *
     * CertStatus ::= SEQUENCE {
     * certHash    OCTET STRING,
     * -- the hash of the certificate, using the same hash algorithm
     * -- as is used to create and verify the certificate signature
     * certReqId   INTEGER,
     * -- to match this confirmation with the corresponding req/rep
     * statusInfo  PKIStatusInfo OPTIONAL
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certHash)
        v.add(certReqId)

        if (statusInfo != null) {
            v.add(statusInfo)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CertStatus? {
            if (o is CertStatus) {
                return o
            }

            if (o != null) {
                return CertStatus(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
