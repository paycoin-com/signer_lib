package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.crmf.CertId
import org.bouncycastle.asn1.x509.Extensions

class RevAnnContent private constructor(seq: ASN1Sequence) : ASN1Object() {
    val status: PKIStatus
    val certId: CertId
    val willBeRevokedAt: ASN1GeneralizedTime
    val badSinceDate: ASN1GeneralizedTime
    var crlDetails: Extensions? = null
        private set

    init {
        status = PKIStatus.getInstance(seq.getObjectAt(0))
        certId = CertId.getInstance(seq.getObjectAt(1))
        willBeRevokedAt = ASN1GeneralizedTime.getInstance(seq.getObjectAt(2))
        badSinceDate = ASN1GeneralizedTime.getInstance(seq.getObjectAt(3))

        if (seq.size() > 4) {
            crlDetails = Extensions.getInstance(seq.getObjectAt(4))
        }
    }

    /**
     *
     * RevAnnContent ::= SEQUENCE {
     * status              PKIStatus,
     * certId              CertId,
     * willBeRevokedAt     GeneralizedTime,
     * badSinceDate        GeneralizedTime,
     * crlDetails          Extensions  OPTIONAL
     * -- extra CRL details (e.g., crl number, reason, location, etc.)
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(status)
        v.add(certId)
        v.add(willBeRevokedAt)
        v.add(badSinceDate)

        if (crlDetails != null) {
            v.add(crlDetails)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): RevAnnContent? {
            if (o is RevAnnContent) {
                return o
            }

            if (o != null) {
                return RevAnnContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
