package org.bouncycastle.asn1.x509

import java.io.IOException

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence

/**
 * an object for the elements in the X.509 V3 extension block.
 */
class Extension : ASN1Object {

    var extnId: ASN1ObjectIdentifier? = null
        private set
    var isCritical: Boolean = false
        private set
    var extnValue: ASN1OctetString? = null
        private set

    constructor(
            extnId: ASN1ObjectIdentifier,
            critical: ASN1Boolean,
            value: ASN1OctetString) : this(extnId, critical.isTrue, value) {
    }

    constructor(
            extnId: ASN1ObjectIdentifier,
            critical: Boolean,
            value: ByteArray) : this(extnId, critical, DEROctetString(value)) {
    }

    constructor(
            extnId: ASN1ObjectIdentifier,
            critical: Boolean,
            value: ASN1OctetString) {
        this.extnId = extnId
        this.isCritical = critical
        this.extnValue = value
    }

    private constructor(seq: ASN1Sequence) {
        if (seq.size() == 2) {
            this.extnId = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
            this.isCritical = false
            this.extnValue = ASN1OctetString.getInstance(seq.getObjectAt(1))
        } else if (seq.size() == 3) {
            this.extnId = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
            this.isCritical = ASN1Boolean.getInstance(seq.getObjectAt(1)).isTrue
            this.extnValue = ASN1OctetString.getInstance(seq.getObjectAt(2))
        } else {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
    }

    val parsedValue: ASN1Encodable
        get() = convertValueToObject(this)

    override fun hashCode(): Int {
        if (this.isCritical) {
            return this.extnValue.hashCode() xor this.extnId.hashCode()
        }

        return (this.extnValue.hashCode() xor this.extnId.hashCode()).inv()
    }

    override fun equals(
            o: Any?): Boolean {
        if (o !is Extension) {
            return false
        }

        return o.extnId == this.extnId
                && o.extnValue == this.extnValue
                && o.isCritical == this.isCritical
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(extnId)

        if (isCritical) {
            v.add(ASN1Boolean.getInstance(true))
        }

        v.add(extnValue)

        return DERSequence(v)
    }

    companion object {
        /**
         * Subject Directory Attributes
         */
        val subjectDirectoryAttributes = ASN1ObjectIdentifier("2.5.29.9").intern()

        /**
         * Subject Key Identifier
         */
        val subjectKeyIdentifier = ASN1ObjectIdentifier("2.5.29.14").intern()

        /**
         * Key Usage
         */
        val keyUsage = ASN1ObjectIdentifier("2.5.29.15").intern()

        /**
         * Private Key Usage Period
         */
        val privateKeyUsagePeriod = ASN1ObjectIdentifier("2.5.29.16").intern()

        /**
         * Subject Alternative Name
         */
        val subjectAlternativeName = ASN1ObjectIdentifier("2.5.29.17").intern()

        /**
         * Issuer Alternative Name
         */
        val issuerAlternativeName = ASN1ObjectIdentifier("2.5.29.18").intern()

        /**
         * Basic Constraints
         */
        val basicConstraints = ASN1ObjectIdentifier("2.5.29.19").intern()

        /**
         * CRL Number
         */
        val cRLNumber = ASN1ObjectIdentifier("2.5.29.20").intern()

        /**
         * Reason code
         */
        val reasonCode = ASN1ObjectIdentifier("2.5.29.21").intern()

        /**
         * Hold Instruction Code
         */
        val instructionCode = ASN1ObjectIdentifier("2.5.29.23").intern()

        /**
         * Invalidity Date
         */
        val invalidityDate = ASN1ObjectIdentifier("2.5.29.24").intern()

        /**
         * Delta CRL indicator
         */
        val deltaCRLIndicator = ASN1ObjectIdentifier("2.5.29.27").intern()

        /**
         * Issuing Distribution Point
         */
        val issuingDistributionPoint = ASN1ObjectIdentifier("2.5.29.28").intern()

        /**
         * Certificate Issuer
         */
        val certificateIssuer = ASN1ObjectIdentifier("2.5.29.29").intern()

        /**
         * Name Constraints
         */
        val nameConstraints = ASN1ObjectIdentifier("2.5.29.30").intern()

        /**
         * CRL Distribution Points
         */
        val cRLDistributionPoints = ASN1ObjectIdentifier("2.5.29.31").intern()

        /**
         * Certificate Policies
         */
        val certificatePolicies = ASN1ObjectIdentifier("2.5.29.32").intern()

        /**
         * Policy Mappings
         */
        val policyMappings = ASN1ObjectIdentifier("2.5.29.33").intern()

        /**
         * Authority Key Identifier
         */
        val authorityKeyIdentifier = ASN1ObjectIdentifier("2.5.29.35").intern()

        /**
         * Policy Constraints
         */
        val policyConstraints = ASN1ObjectIdentifier("2.5.29.36").intern()

        /**
         * Extended Key Usage
         */
        val extendedKeyUsage = ASN1ObjectIdentifier("2.5.29.37").intern()

        /**
         * Freshest CRL
         */
        val freshestCRL = ASN1ObjectIdentifier("2.5.29.46").intern()

        /**
         * Inhibit Any Policy
         */
        val inhibitAnyPolicy = ASN1ObjectIdentifier("2.5.29.54").intern()

        /**
         * Authority Info Access
         */
        val authorityInfoAccess = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.1").intern()

        /**
         * Subject Info Access
         */
        val subjectInfoAccess = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.11").intern()

        /**
         * Logo Type
         */
        val logoType = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.12").intern()

        /**
         * BiometricInfo
         */
        val biometricInfo = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.2").intern()

        /**
         * QCStatements
         */
        val qCStatements = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.3").intern()

        /**
         * Audit identity extension in attribute certificates.
         */
        val auditIdentity = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.4").intern()

        /**
         * NoRevAvail extension in attribute certificates.
         */
        val noRevAvail = ASN1ObjectIdentifier("2.5.29.56").intern()

        /**
         * TargetInformation extension in attribute certificates.
         */
        val targetInformation = ASN1ObjectIdentifier("2.5.29.55").intern()

        fun getInstance(obj: Any?): Extension? {
            if (obj is Extension) {
                return obj
            } else if (obj != null) {
                return Extension(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        /**
         * Convert the value of the passed in extension to an object
         * @param ext the extension to parse
         * *
         * @return the object the value string contains
         * *
         * @exception IllegalArgumentException if conversion is not possible
         */
        @Throws(IllegalArgumentException::class)
        private fun convertValueToObject(
                ext: Extension): ASN1Primitive {
            try {
                return ASN1Primitive.fromByteArray(ext.extnValue.getOctets())
            } catch (e: IOException) {
                throw IllegalArgumentException("can't convert extension: " + e)
            }

        }
    }
}
