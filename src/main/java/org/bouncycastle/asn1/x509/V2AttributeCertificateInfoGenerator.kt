package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet

/**
 * Generator for Version 2 AttributeCertificateInfo
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
 *

 */
class V2AttributeCertificateInfoGenerator {
    private val version: ASN1Integer
    private var holder: Holder? = null
    private var issuer: AttCertIssuer? = null
    private var signature: AlgorithmIdentifier? = null
    private var serialNumber: ASN1Integer? = null
    private val attributes: ASN1EncodableVector?
    private var issuerUniqueID: DERBitString? = null
    private var extensions: Extensions? = null

    // Note: validity period start/end dates stored directly
    //private AttCertValidityPeriod attrCertValidityPeriod;
    private var startDate: ASN1GeneralizedTime? = null
    private var endDate: ASN1GeneralizedTime? = null

    init {
        this.version = ASN1Integer(1)
        attributes = ASN1EncodableVector()
    }

    fun setHolder(holder: Holder) {
        this.holder = holder
    }

    fun addAttribute(oid: String, value: ASN1Encodable) {
        attributes!!.add(Attribute(ASN1ObjectIdentifier(oid), DERSet(value)))
    }

    /**
     * @param attribute
     */
    fun addAttribute(attribute: Attribute) {
        attributes!!.add(attribute)
    }

    fun setSerialNumber(
            serialNumber: ASN1Integer) {
        this.serialNumber = serialNumber
    }

    fun setSignature(
            signature: AlgorithmIdentifier) {
        this.signature = signature
    }

    fun setIssuer(
            issuer: AttCertIssuer) {
        this.issuer = issuer
    }

    fun setStartDate(
            startDate: ASN1GeneralizedTime) {
        this.startDate = startDate
    }

    fun setEndDate(
            endDate: ASN1GeneralizedTime) {
        this.endDate = endDate
    }

    fun setIssuerUniqueID(
            issuerUniqueID: DERBitString) {
        this.issuerUniqueID = issuerUniqueID
    }

    /**
     * @param extensions
     */
    @Deprecated("use method taking Extensions\n      ")
    fun setExtensions(
            extensions: X509Extensions) {
        this.extensions = Extensions.getInstance(extensions.toASN1Primitive())
    }

    fun setExtensions(
            extensions: Extensions) {
        this.extensions = extensions
    }

    fun generateAttributeCertificateInfo(): AttributeCertificateInfo {
        if (serialNumber == null || signature == null
                || issuer == null || startDate == null || endDate == null
                || holder == null || attributes == null) {
            throw IllegalStateException("not all mandatory fields set in V2 AttributeCertificateInfo generator")
        }

        val v = ASN1EncodableVector()

        v.add(version)
        v.add(holder)
        v.add(issuer)
        v.add(signature)
        v.add(serialNumber)

        //
        // before and after dates => AttCertValidityPeriod
        //
        val validity = AttCertValidityPeriod(startDate, endDate)
        v.add(validity)

        // Attributes
        v.add(DERSequence(attributes))

        if (issuerUniqueID != null) {
            v.add(issuerUniqueID)
        }

        if (extensions != null) {
            v.add(extensions)
        }

        return AttributeCertificateInfo.getInstance(DERSequence(v))
    }
}
