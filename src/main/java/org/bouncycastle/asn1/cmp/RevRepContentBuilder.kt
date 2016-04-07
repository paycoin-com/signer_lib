package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.crmf.CertId
import org.bouncycastle.asn1.x509.CertificateList

class RevRepContentBuilder {
    private val status = ASN1EncodableVector()
    private val revCerts = ASN1EncodableVector()
    private val crls = ASN1EncodableVector()

    fun add(status: PKIStatusInfo): RevRepContentBuilder {
        this.status.add(status)

        return this
    }

    fun add(status: PKIStatusInfo, certId: CertId): RevRepContentBuilder {
        if (this.status.size() != this.revCerts.size()) {
            throw IllegalStateException("status and revCerts sequence must be in common order")
        }
        this.status.add(status)
        this.revCerts.add(certId)

        return this
    }

    fun addCrl(crl: CertificateList): RevRepContentBuilder {
        this.crls.add(crl)

        return this
    }

    fun build(): RevRepContent {
        val v = ASN1EncodableVector()

        v.add(DERSequence(status))

        if (revCerts.size() != 0) {
            v.add(DERTaggedObject(true, 0, DERSequence(revCerts)))
        }

        if (crls.size() != 0) {
            v.add(DERTaggedObject(true, 1, DERSequence(crls)))
        }

        return RevRepContent.getInstance(DERSequence(v))
    }
}
