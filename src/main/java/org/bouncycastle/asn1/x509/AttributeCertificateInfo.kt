package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence

class AttributeCertificateInfo private constructor(
        seq: ASN1Sequence) : ASN1Object() {
    var version: ASN1Integer? = null
        private set
    val holder: Holder
    val issuer: AttCertIssuer
    val signature: AlgorithmIdentifier
    val serialNumber: ASN1Integer
    val attrCertValidityPeriod: AttCertValidityPeriod
    val attributes: ASN1Sequence
    var issuerUniqueID: DERBitString? = null
        private set
    var extensions: Extensions? = null
        private set

    init {
        if (seq.size() < 6 || seq.size() > 9) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val start: Int
        if (seq.getObjectAt(0) is ASN1Integer)
        // in version 1 certs version is DEFAULT  v1(0)
        {
            this.version = ASN1Integer.getInstance(seq.getObjectAt(0))
            start = 1
        } else {
            this.version = ASN1Integer(0)
            start = 0
        }

        this.holder = Holder.getInstance(seq.getObjectAt(start))
        this.issuer = AttCertIssuer.getInstance(seq.getObjectAt(start + 1))
        this.signature = AlgorithmIdentifier.getInstance(seq.getObjectAt(start + 2))
        this.serialNumber = ASN1Integer.getInstance(seq.getObjectAt(start + 3))
        this.attrCertValidityPeriod = AttCertValidityPeriod.getInstance(seq.getObjectAt(start + 4))
        this.attributes = ASN1Sequence.getInstance(seq.getObjectAt(start + 5))

        for (i in start + 6..seq.size() - 1) {
            val obj = seq.getObjectAt(i)

            if (obj is DERBitString) {
                this.issuerUniqueID = DERBitString.getInstance(seq.getObjectAt(i))
            } else if (obj is ASN1Sequence || obj is Extensions) {
                this.extensions = Extensions.getInstance(seq.getObjectAt(i))
            }
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * AttributeCertificateInfo ::= SEQUENCE {
     * version              AttCertVersion -- version is v2,
     * holder               Holder,
     * issuer               AttCertIssuer,
     * signature            AlgorithmIdentifier,
     * serialNumber         CertificateSerialNumber,
     * attrCertValidityPeriod   AttCertValidityPeriod,
     * attributes           SEQUENCE OF Attribute,
     * issuerUniqueID       UniqueIdentifier OPTIONAL,
     * extensions           Extensions OPTIONAL
     * }

     * AttCertVersion ::= INTEGER { v2(1) }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (version!!.value.toInt() != 0) {
            v.add(version)
        }
        v.add(holder)
        v.add(issuer)
        v.add(signature)
        v.add(serialNumber)
        v.add(attrCertValidityPeriod)
        v.add(attributes)

        if (issuerUniqueID != null) {
            v.add(issuerUniqueID)
        }

        if (extensions != null) {
            v.add(extensions)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): AttributeCertificateInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): AttributeCertificateInfo? {
            if (obj is AttributeCertificateInfo) {
                return obj
            } else if (obj != null) {
                return AttributeCertificateInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
