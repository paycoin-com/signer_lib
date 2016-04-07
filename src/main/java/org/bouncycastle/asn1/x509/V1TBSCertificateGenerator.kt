package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1UTCTime
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name

/**
 * Generator for Version 1 TBSCertificateStructures.
 *
 * TBSCertificate ::= SEQUENCE {
 * version          [ 0 ]  Version DEFAULT v1(0),
 * serialNumber            CertificateSerialNumber,
 * signature               AlgorithmIdentifier,
 * issuer                  Name,
 * validity                Validity,
 * subject                 Name,
 * subjectPublicKeyInfo    SubjectPublicKeyInfo,
 * }
 *

 */
class V1TBSCertificateGenerator {
    internal var version = DERTaggedObject(true, 0, ASN1Integer(0))

    internal var serialNumber: ASN1Integer? = null
    internal var signature: AlgorithmIdentifier? = null
    internal var issuer: X500Name? = null
    internal var startDate: Time? = null
    internal var endDate: Time? = null
    internal var subject: X500Name? = null
    internal var subjectPublicKeyInfo: SubjectPublicKeyInfo? = null

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
        this.issuer = X500Name.getInstance(issuer.toASN1Primitive())
    }

    fun setIssuer(
            issuer: X500Name) {
        this.issuer = issuer
    }

    fun setStartDate(
            startDate: Time) {
        this.startDate = startDate
    }

    fun setStartDate(
            startDate: ASN1UTCTime) {
        this.startDate = Time(startDate)
    }

    fun setEndDate(
            endDate: Time) {
        this.endDate = endDate
    }

    fun setEndDate(
            endDate: ASN1UTCTime) {
        this.endDate = Time(endDate)
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

    fun setSubjectPublicKeyInfo(
            pubKeyInfo: SubjectPublicKeyInfo) {
        this.subjectPublicKeyInfo = pubKeyInfo
    }

    fun generateTBSCertificate(): TBSCertificate {
        if (serialNumber == null || signature == null
                || issuer == null || startDate == null || endDate == null
                || subject == null || subjectPublicKeyInfo == null) {
            throw IllegalStateException("not all mandatory fields set in V1 TBScertificate generator")
        }

        val seq = ASN1EncodableVector()

        // seq.add(version); - not required as default value.
        seq.add(serialNumber)
        seq.add(signature)
        seq.add(issuer)

        //
        // before and after dates
        //
        val validity = ASN1EncodableVector()

        validity.add(startDate)
        validity.add(endDate)

        seq.add(DERSequence(validity))

        seq.add(subject)

        seq.add(subjectPublicKeyInfo)

        return TBSCertificate.getInstance(DERSequence(seq))
    }
}
