package org.bouncycastle.asn1.x509

import java.io.IOException

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive

/**
 * an object for the elements in the X.509 V3 extension block.
 */
@Deprecated("use Extension")
class X509Extension {

    var isCritical: Boolean = false
        internal set
    var value: ASN1OctetString
        internal set

    constructor(
            critical: ASN1Boolean,
            value: ASN1OctetString) {
        this.isCritical = critical.isTrue
        this.value = value
    }

    constructor(
            critical: Boolean,
            value: ASN1OctetString) {
        this.isCritical = critical
        this.value = value
    }

    val parsedValue: ASN1Encodable
        get() = convertValueToObject(this)

    override fun hashCode(): Int {
        if (this.isCritical) {
            return this.value.hashCode()
        }

        return this.value.hashCode().inv()
    }

    override fun equals(
            o: Any?): Boolean {
        if (o !is X509Extension) {
            return false
        }

        return o.value == this.value && o.isCritical == this.isCritical
    }

    companion object {
        /**
         * Subject Directory Attributes
         */
        val subjectDirectoryAttributes = ASN1ObjectIdentifier("2.5.29.9")

        /**
         * Subject Key Identifier
         */
        val subjectKeyIdentifier = ASN1ObjectIdentifier("2.5.29.14")

        /**
         * Key Usage
         */
        val keyUsage = ASN1ObjectIdentifier("2.5.29.15")

        /**
         * Private Key Usage Period
         */
        val privateKeyUsagePeriod = ASN1ObjectIdentifier("2.5.29.16")

        /**
         * Subject Alternative Name
         */
        val subjectAlternativeName = ASN1ObjectIdentifier("2.5.29.17")

        /**
         * Issuer Alternative Name
         */
        val issuerAlternativeName = ASN1ObjectIdentifier("2.5.29.18")

        /**
         * Basic Constraints
         */
        val basicConstraints = ASN1ObjectIdentifier("2.5.29.19")

        /**
         * CRL Number
         */
        val cRLNumber = ASN1ObjectIdentifier("2.5.29.20")

        /**
         * Reason code
         */
        val reasonCode = ASN1ObjectIdentifier("2.5.29.21")

        /**
         * Hold Instruction Code
         */
        val instructionCode = ASN1ObjectIdentifier("2.5.29.23")

        /**
         * Invalidity Date
         */
        val invalidityDate = ASN1ObjectIdentifier("2.5.29.24")

        /**
         * Delta CRL indicator
         */
        val deltaCRLIndicator = ASN1ObjectIdentifier("2.5.29.27")

        /**
         * Issuing Distribution Point
         */
        val issuingDistributionPoint = ASN1ObjectIdentifier("2.5.29.28")

        /**
         * Certificate Issuer
         */
        val certificateIssuer = ASN1ObjectIdentifier("2.5.29.29")

        /**
         * Name Constraints
         */
        val nameConstraints = ASN1ObjectIdentifier("2.5.29.30")

        /**
         * CRL Distribution Points
         */
        val cRLDistributionPoints = ASN1ObjectIdentifier("2.5.29.31")

        /**
         * Certificate Policies
         */
        val certificatePolicies = ASN1ObjectIdentifier("2.5.29.32")

        /**
         * Policy Mappings
         */
        val policyMappings = ASN1ObjectIdentifier("2.5.29.33")

        /**
         * Authority Key Identifier
         */
        val authorityKeyIdentifier = ASN1ObjectIdentifier("2.5.29.35")

        /**
         * Policy Constraints
         */
        val policyConstraints = ASN1ObjectIdentifier("2.5.29.36")

        /**
         * Extended Key Usage
         */
        val extendedKeyUsage = ASN1ObjectIdentifier("2.5.29.37")

        /**
         * Freshest CRL
         */
        val freshestCRL = ASN1ObjectIdentifier("2.5.29.46")

        /**
         * Inhibit Any Policy
         */
        val inhibitAnyPolicy = ASN1ObjectIdentifier("2.5.29.54")

        /**
         * Authority Info Access
         */
        val authorityInfoAccess = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.1")

        /**
         * Subject Info Access
         */
        val subjectInfoAccess = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.11")

        /**
         * Logo Type
         */
        val logoType = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.12")

        /**
         * BiometricInfo
         */
        val biometricInfo = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.2")

        /**
         * QCStatements
         */
        val qCStatements = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.3")

        /**
         * Audit identity extension in attribute certificates.
         */
        val auditIdentity = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.4")

        /**
         * NoRevAvail extension in attribute certificates.
         */
        val noRevAvail = ASN1ObjectIdentifier("2.5.29.56")

        /**
         * TargetInformation extension in attribute certificates.
         */
        val targetInformation = ASN1ObjectIdentifier("2.5.29.55")

        /**
         * Convert the value of the passed in extension to an object
         * @param ext the extension to parse
         * *
         * @return the object the value string contains
         * *
         * @exception IllegalArgumentException if conversion is not possible
         */
        @Throws(IllegalArgumentException::class)
        fun convertValueToObject(
                ext: X509Extension): ASN1Primitive {
            try {
                return ASN1Primitive.fromByteArray(ext.value.octets)
            } catch (e: IOException) {
                throw IllegalArgumentException("can't convert extension: " + e)
            }

        }
    }
}
