package org.bouncycastle.asn1.x509

import java.util.Enumeration
import java.util.Hashtable
import java.util.Vector

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence


@Deprecated("use Extensions")
class X509Extensions : ASN1Object {

    private val extensions = Hashtable()
    private val ordering = Vector()

    /**
     * Constructor from ASN1Sequence.

     * the extensions are a list of constructed sequences, either with (OID, OctetString) or (OID, Boolean, OctetString)
     */
    constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        while (e.hasMoreElements()) {
            val s = ASN1Sequence.getInstance(e.nextElement())

            if (s.size() == 3) {
                extensions.put(s.getObjectAt(0), X509Extension(ASN1Boolean.getInstance(s.getObjectAt(1)), ASN1OctetString.getInstance(s.getObjectAt(2))))
            } else if (s.size() == 2) {
                extensions.put(s.getObjectAt(0), X509Extension(false, ASN1OctetString.getInstance(s.getObjectAt(1))))
            } else {
                throw IllegalArgumentException("Bad sequence size: " + s.size())
            }

            ordering.addElement(s.getObjectAt(0))
        }
    }

    /**
     * constructor from a table of extensions.
     *
     *
     * it's is assumed the table contains OID/String pairs.
     */
    constructor(
            extensions: Hashtable<Any, Any>) : this(null, extensions) {
    }

    /**
     * Constructor from a table of extensions with ordering.
     *
     *
     * It's is assumed the table contains OID/String pairs.
     */
    @Deprecated("use Extensions")
    constructor(
            ordering: Vector<Any>?,
            extensions: Hashtable<Any, Any>) {
        var e: Enumeration<Any>

        if (ordering == null) {
            e = extensions.keys()
        } else {
            e = ordering.elements()
        }

        while (e.hasMoreElements()) {
            this.ordering.addElement(ASN1ObjectIdentifier.getInstance(e.nextElement()))
        }

        e = this.ordering.elements()

        while (e.hasMoreElements()) {
            val oid = ASN1ObjectIdentifier.getInstance(e.nextElement())
            val ext = extensions[oid] as X509Extension

            this.extensions.put(oid, ext)
        }
    }

    /**
     * Constructor from two vectors

     * @param objectIDs a vector of the object identifiers.
     * *
     * @param values a vector of the extension values.
     * *
     */
    @Deprecated("use Extensions")
    constructor(
            objectIDs: Vector<Any>,
            values: Vector<Any>) {
        var e: Enumeration<Any> = objectIDs.elements()

        while (e.hasMoreElements()) {
            this.ordering.addElement(e.nextElement())
        }

        var count = 0

        e = this.ordering.elements()

        while (e.hasMoreElements()) {
            val oid = e.nextElement() as ASN1ObjectIdentifier
            val ext = values.elementAt(count) as X509Extension

            this.extensions.put(oid, ext)
            count++
        }
    }

    /**
     * return an Enumeration of the extension field's object ids.
     */
    fun oids(): Enumeration<Any> {
        return ordering.elements()
    }

    /**
     * return the extension represented by the object identifier
     * passed in.

     * @return the extension if it's present, null otherwise.
     */
    fun getExtension(
            oid: ASN1ObjectIdentifier): X509Extension {
        return extensions.get(oid)
    }

    /**
     *
     * Extensions        ::=   SEQUENCE SIZE (1..MAX) OF Extension

     * Extension         ::=   SEQUENCE {
     * extnId            EXTENSION.&amp;id ({ExtensionSet}),
     * critical          BOOLEAN DEFAULT FALSE,
     * extnValue         OCTET STRING }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()
        val e = ordering.elements()

        while (e.hasMoreElements()) {
            val v = ASN1EncodableVector()

            v.add(e.nextElement())

            if (extensions.get(e.nextElement()).isCritical) {
                v.add(ASN1Boolean.TRUE)
            }

            v.add(extensions.get(e.nextElement()).value)

            vec.add(DERSequence(v))
        }

        return DERSequence(vec)
    }

    fun equivalent(
            other: X509Extensions): Boolean {
        if (extensions.size != other.extensions.size) {
            return false
        }

        val e1 = extensions.keys()

        while (e1.hasMoreElements()) {
            val key = e1.nextElement()

            if (extensions.get(key) != other.extensions.get(key)) {
                return false
            }
        }

        return true
    }

    val extensionOIDs: Array<ASN1ObjectIdentifier>
        get() = toOidArray(ordering)

    val nonCriticalExtensionOIDs: Array<ASN1ObjectIdentifier>
        get() = getExtensionOIDs(false)

    val criticalExtensionOIDs: Array<ASN1ObjectIdentifier>
        get() = getExtensionOIDs(true)

    private fun getExtensionOIDs(isCritical: Boolean): Array<ASN1ObjectIdentifier> {
        val oidVec = Vector()

        for (i in ordering.indices) {
            val oid = ordering.elementAt(i)

            if ((extensions.get(oid) as X509Extension).isCritical == isCritical) {
                oidVec.addElement(oid)
            }
        }

        return toOidArray(oidVec)
    }

    private fun toOidArray(oidVec: Vector<Any>): Array<ASN1ObjectIdentifier> {
        val oids = arrayOfNulls<ASN1ObjectIdentifier>(oidVec.size)

        for (i in oids.indices) {
            oids[i] = oidVec.elementAt(i) as ASN1ObjectIdentifier
        }
        return oids
    }

    companion object {
        /**
         * Subject Directory Attributes
         */
        @Deprecated("use X509Extension value.")
        val SubjectDirectoryAttributes = ASN1ObjectIdentifier("2.5.29.9")

        /**
         * Subject Key Identifier
         */
        @Deprecated("use X509Extension value.")
        val SubjectKeyIdentifier = ASN1ObjectIdentifier("2.5.29.14")

        /**
         * Key Usage
         */
        @Deprecated("use X509Extension value.")
        val KeyUsage = ASN1ObjectIdentifier("2.5.29.15")

        /**
         * Private Key Usage Period
         */
        @Deprecated("use X509Extension value.")
        val PrivateKeyUsagePeriod = ASN1ObjectIdentifier("2.5.29.16")

        /**
         * Subject Alternative Name
         */
        @Deprecated("use X509Extension value.")
        val SubjectAlternativeName = ASN1ObjectIdentifier("2.5.29.17")

        /**
         * Issuer Alternative Name
         */
        @Deprecated("use X509Extension value.")
        val IssuerAlternativeName = ASN1ObjectIdentifier("2.5.29.18")

        /**
         * Basic Constraints
         */
        @Deprecated("use X509Extension value.")
        val BasicConstraints = ASN1ObjectIdentifier("2.5.29.19")

        /**
         * CRL Number
         */
        @Deprecated("use X509Extension value.")
        val CRLNumber = ASN1ObjectIdentifier("2.5.29.20")

        /**
         * Reason code
         */
        @Deprecated("use X509Extension value.")
        val ReasonCode = ASN1ObjectIdentifier("2.5.29.21")

        /**
         * Hold Instruction Code
         */
        @Deprecated("use X509Extension value.")
        val InstructionCode = ASN1ObjectIdentifier("2.5.29.23")

        /**
         * Invalidity Date
         */
        @Deprecated("use X509Extension value.")
        val InvalidityDate = ASN1ObjectIdentifier("2.5.29.24")

        /**
         * Delta CRL indicator
         */
        @Deprecated("use X509Extension value.")
        val DeltaCRLIndicator = ASN1ObjectIdentifier("2.5.29.27")

        /**
         * Issuing Distribution Point
         */
        @Deprecated("use X509Extension value.")
        val IssuingDistributionPoint = ASN1ObjectIdentifier("2.5.29.28")

        /**
         * Certificate Issuer
         */
        @Deprecated("use X509Extension value.")
        val CertificateIssuer = ASN1ObjectIdentifier("2.5.29.29")

        /**
         * Name Constraints
         */
        @Deprecated("use X509Extension value.")
        val NameConstraints = ASN1ObjectIdentifier("2.5.29.30")

        /**
         * CRL Distribution Points
         */
        @Deprecated("use X509Extension value.")
        val CRLDistributionPoints = ASN1ObjectIdentifier("2.5.29.31")

        /**
         * Certificate Policies
         */
        @Deprecated("use X509Extension value.")
        val CertificatePolicies = ASN1ObjectIdentifier("2.5.29.32")

        /**
         * Policy Mappings
         */
        @Deprecated("use X509Extension value.")
        val PolicyMappings = ASN1ObjectIdentifier("2.5.29.33")

        /**
         * Authority Key Identifier
         */
        @Deprecated("use X509Extension value.")
        val AuthorityKeyIdentifier = ASN1ObjectIdentifier("2.5.29.35")

        /**
         * Policy Constraints
         */
        @Deprecated("use X509Extension value.")
        val PolicyConstraints = ASN1ObjectIdentifier("2.5.29.36")

        /**
         * Extended Key Usage
         */
        @Deprecated("use X509Extension value.")
        val ExtendedKeyUsage = ASN1ObjectIdentifier("2.5.29.37")

        /**
         * Freshest CRL
         */
        @Deprecated("use X509Extension value.")
        val FreshestCRL = ASN1ObjectIdentifier("2.5.29.46")

        /**
         * Inhibit Any Policy
         */
        @Deprecated("use X509Extension value.")
        val InhibitAnyPolicy = ASN1ObjectIdentifier("2.5.29.54")

        /**
         * Authority Info Access
         */
        @Deprecated("use X509Extension value.")
        val AuthorityInfoAccess = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.1")

        /**
         * Subject Info Access
         */
        @Deprecated("use X509Extension value.")
        val SubjectInfoAccess = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.11")

        /**
         * Logo Type
         */
        @Deprecated("use X509Extension value.")
        val LogoType = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.12")

        /**
         * BiometricInfo
         */
        @Deprecated("use X509Extension value.")
        val BiometricInfo = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.2")

        /**
         * QCStatements
         */
        @Deprecated("use X509Extension value.")
        val QCStatements = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.3")

        /**
         * Audit identity extension in attribute certificates.
         */
        @Deprecated("use X509Extension value.")
        val AuditIdentity = ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.4")

        /**
         * NoRevAvail extension in attribute certificates.
         */
        @Deprecated("use X509Extension value.")
        val NoRevAvail = ASN1ObjectIdentifier("2.5.29.56")

        /**
         * TargetInformation extension in attribute certificates.
         */
        @Deprecated("use X509Extension value.")
        val TargetInformation = ASN1ObjectIdentifier("2.5.29.55")

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): X509Extensions {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): X509Extensions {
            if (obj == null || obj is X509Extensions) {
                return obj as X509Extensions?
            }

            if (obj is ASN1Sequence) {
                return X509Extensions(obj as ASN1Sequence?)
            }

            if (obj is Extensions) {
                return X509Extensions(obj.toASN1Primitive() as ASN1Sequence)
            }

            if (obj is ASN1TaggedObject) {
                return getInstance(obj.`object`)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}
