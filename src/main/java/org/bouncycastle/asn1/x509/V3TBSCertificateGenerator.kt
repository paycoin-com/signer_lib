package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1UTCTime
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name

/**
 * Generator for Version 3 TBSCertificateStructures.
 *
 * TBSCertificate ::= SEQUENCE {
 * version          [ 0 ]  Version DEFAULT v1(0),
 * serialNumber            CertificateSerialNumber,
 * signature               AlgorithmIdentifier,
 * issuer                  Name,
 * validity                Validity,
 * subject                 Name,
 * subjectPublicKeyInfo    SubjectPublicKeyInfo,
 * issuerUniqueID    [ 1 ] IMPLICIT UniqueIdentifier OPTIONAL,
 * subjectUniqueID   [ 2 ] IMPLICIT UniqueIdentifier OPTIONAL,
 * extensions        [ 3 ] Extensions OPTIONAL
 * }
 *

 */
class V3TBSCertificateGenerator {
    internal var version = DERTaggedObject(true, 0, ASN1Integer(2))

    internal var serialNumber: ASN1Integer? = null
    internal var signature: AlgorithmIdentifier? = null
    internal var issuer: X500Name? = null
    internal var startDate: Time? = null
    internal var endDate: Time? = null
    internal var subject: X500Name? = null
    internal var subjectPublicKeyInfo: SubjectPublicKeyInfo? = null
    internal var extensions: Extensions? = null

    private var altNamePresentAndCritical: Boolean = false
    private var issuerUniqueID: DERBitString? = null
    private var subjectUniqueID: DERBitString? = null

    fun setSerialNumber(
            serialNumber: ASN1Integer) {
        this.serialNumber = serialNumber
    }

    fun setSignature(
            signature: AlgorithmIdentifier) {
        this.signature = signature
    }


    @Deprecated("use X500Name method")
    fun setIssuer(
            issuer: X509Name) {
        this.issuer = X500Name.getInstance(issuer)
    }

    fun setIssuer(
            issuer: X500Name) {
        this.issuer = issuer
    }

    fun setStartDate(
            startDate: ASN1UTCTime) {
        this.startDate = Time(startDate)
    }

    fun setStartDate(
            startDate: Time) {
        this.startDate = startDate
    }

    fun setEndDate(
            endDate: ASN1UTCTime) {
        this.endDate = Time(endDate)
    }

    fun setEndDate(
            endDate: Time) {
        this.endDate = endDate
    }


    @Deprecated("use X500Name method")
    fun setSubject(
            subject: X509Name) {
        this.subject = X500Name.getInstance(subject.toASN1Primitive())
    }

    fun setSubject(
            subject: X500Name) {
        this.subject = subject
    }

    fun setIssuerUniqueID(
            uniqueID: DERBitString) {
        this.issuerUniqueID = uniqueID
    }

    fun setSubjectUniqueID(
            uniqueID: DERBitString) {
        this.subjectUniqueID = uniqueID
    }

    fun setSubjectPublicKeyInfo(
            pubKeyInfo: SubjectPublicKeyInfo) {
        this.subjectPublicKeyInfo = pubKeyInfo
    }

    /**
     * @param extensions
     */
    @Deprecated("use method taking Extensions\n      ")
    fun setExtensions(
            extensions: X509Extensions) {
        setExtensions(Extensions.getInstance(extensions))
    }

    fun setExtensions(
            extensions: Extensions?) {
        this.extensions = extensions
        if (extensions != null) {
            val altName = extensions.getExtension(Extension.subjectAlternativeName)

            if (altName != null && altName.isCritical) {
                altNamePresentAndCritical = true
            }
        }
    }

    fun generateTBSCertificate(): TBSCertificate {
        if (serialNumber == null || signature == null
                || issuer == null || startDate == null || endDate == null
                || subject == null && !altNamePresentAndCritical || subjectPublicKeyInfo == null) {
            throw IllegalStateException("not all mandatory fields set in V3 TBScertificate generator")
        }

        val v = ASN1EncodableVector()

        v.add(version)
        v.add(serialNumber)
        v.add(signature)
        v.add(issuer)

        //
        // before and after dates
        //
        val validity = ASN1EncodableVector()

        validity.add(startDate)
        validity.add(endDate)

        v.add(DERSequence(validity))

        if (subject != null) {
            v.add(subject)
        } else {
            v.add(DERSequence())
        }

        v.add(subjectPublicKeyInfo)

        if (issuerUniqueID != null) {
            v.add(DERTaggedObject(false, 1, issuerUniqueID))
        }

        if (subjectUniqueID != null) {
            v.add(DERTaggedObject(false, 2, subjectUniqueID))
        }

        if (extensions != null) {
            v.add(DERTaggedObject(true, 3, extensions))
        }

        return TBSCertificate.getInstance(DERSequence(v))
    }
}
