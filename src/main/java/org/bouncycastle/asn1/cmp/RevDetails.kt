package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.crmf.CertTemplate
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.X509Extensions

class RevDetails : ASN1Object {
    var certDetails: CertTemplate? = null
        private set
    var crlEntryDetails: Extensions? = null
        private set

    private constructor(seq: ASN1Sequence) {
        certDetails = CertTemplate.getInstance(seq.getObjectAt(0))
        if (seq.size() > 1) {
            crlEntryDetails = Extensions.getInstance(seq.getObjectAt(1))
        }
    }

    constructor(certDetails: CertTemplate) {
        this.certDetails = certDetails
    }

    /**
     * @param certDetails
     * *
     * @param crlEntryDetails
     */
    @Deprecated("use method taking Extensions\n      ")
    constructor(certDetails: CertTemplate, crlEntryDetails: X509Extensions) {
        this.certDetails = certDetails
        this.crlEntryDetails = Extensions.getInstance(crlEntryDetails.toASN1Primitive())
    }

    constructor(certDetails: CertTemplate, crlEntryDetails: Extensions) {
        this.certDetails = certDetails
        this.crlEntryDetails = crlEntryDetails
    }

    /**
     *
     * RevDetails ::= SEQUENCE {
     * certDetails         CertTemplate,
     * -- allows requester to specify as much as they can about
     * -- the cert. for which revocation is requested
     * -- (e.g., for cases in which serialNumber is not available)
     * crlEntryDetails     Extensions       OPTIONAL
     * -- requested crlEntryExtensions
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certDetails)

        if (crlEntryDetails != null) {
            v.add(crlEntryDetails)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): RevDetails? {
            if (o is RevDetails) {
                return o
            }

            if (o != null) {
                return RevDetails(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
