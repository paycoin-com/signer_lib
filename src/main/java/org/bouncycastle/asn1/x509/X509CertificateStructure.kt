package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name

/**
 * an X509Certificate structure.
 *
 * Certificate ::= SEQUENCE {
 * tbsCertificate          TBSCertificate,
 * signatureAlgorithm      AlgorithmIdentifier,
 * signature               BIT STRING
 * }
 *
 */
@Deprecated("use org.bouncycastle.asn1.x509.Certificate")
class X509CertificateStructure(
        internal var seq: ASN1Sequence) : ASN1Object(), X509ObjectIdentifiers, PKCSObjectIdentifiers {
    var tbsCertificate: TBSCertificateStructure
        internal set
    var signatureAlgorithm: AlgorithmIdentifier
        internal set
    var signature: DERBitString
        internal set

    init {

        //
        // correct x509 certficate
        //
        if (seq.size() == 3) {
            tbsCertificate = TBSCertificateStructure.getInstance(seq.getObjectAt(0))
            signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))

            signature = DERBitString.getInstance(seq.getObjectAt(2))
        } else {
            throw IllegalArgumentException("sequence wrong size for a certificate")
        }
    }

    val version: Int
        get() = tbsCertificate.version

    val serialNumber: ASN1Integer
        get() = tbsCertificate.serialNumber

    val issuer: X500Name
        get() = tbsCertificate.issuer

    val startDate: Time
        get() = tbsCertificate.startDate

    val endDate: Time
        get() = tbsCertificate.endDate

    val subject: X500Name
        get() = tbsCertificate.subject

    val subjectPublicKeyInfo: SubjectPublicKeyInfo
        get() = tbsCertificate.subjectPublicKeyInfo

    override fun toASN1Primitive(): ASN1Primitive {
        return seq
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): X509CertificateStructure {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): X509CertificateStructure? {
            if (obj is X509CertificateStructure) {
                return obj
            } else if (obj != null) {
                return X509CertificateStructure(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
