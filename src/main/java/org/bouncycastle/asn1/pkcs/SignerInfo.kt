package org.bouncycastle.asn1.pkcs

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * a PKCS#7 signer info object.
 */
class SignerInfo : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var issuerAndSerialNumber: IssuerAndSerialNumber? = null
        private set
    var digestAlgorithm: AlgorithmIdentifier? = null
        private set
    var authenticatedAttributes: ASN1Set? = null
        private set
    var digestEncryptionAlgorithm: AlgorithmIdentifier? = null
        private set
    var encryptedDigest: ASN1OctetString? = null
        private set
    var unauthenticatedAttributes: ASN1Set? = null
        private set

    constructor(
            version: ASN1Integer,
            issuerAndSerialNumber: IssuerAndSerialNumber,
            digAlgorithm: AlgorithmIdentifier,
            authenticatedAttributes: ASN1Set,
            digEncryptionAlgorithm: AlgorithmIdentifier,
            encryptedDigest: ASN1OctetString,
            unauthenticatedAttributes: ASN1Set) {
        this.version = version
        this.issuerAndSerialNumber = issuerAndSerialNumber
        this.digestAlgorithm = digAlgorithm
        this.authenticatedAttributes = authenticatedAttributes
        this.digestEncryptionAlgorithm = digEncryptionAlgorithm
        this.encryptedDigest = encryptedDigest
        this.unauthenticatedAttributes = unauthenticatedAttributes
    }

    constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        version = e.nextElement() as ASN1Integer
        issuerAndSerialNumber = IssuerAndSerialNumber.getInstance(e.nextElement())
        digestAlgorithm = AlgorithmIdentifier.getInstance(e.nextElement())

        val obj = e.nextElement()

        if (obj is ASN1TaggedObject) {
            authenticatedAttributes = ASN1Set.getInstance(obj, false)

            digestEncryptionAlgorithm = AlgorithmIdentifier.getInstance(e.nextElement())
        } else {
            authenticatedAttributes = null
            digestEncryptionAlgorithm = AlgorithmIdentifier.getInstance(obj)
        }

        encryptedDigest = DEROctetString.getInstance(e.nextElement())

        if (e.hasMoreElements()) {
            unauthenticatedAttributes = ASN1Set.getInstance(e.nextElement() as ASN1TaggedObject, false)
        } else {
            unauthenticatedAttributes = null
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * SignerInfo ::= SEQUENCE {
     * version Version,
     * issuerAndSerialNumber IssuerAndSerialNumber,
     * digestAlgorithm DigestAlgorithmIdentifier,
     * authenticatedAttributes [0] IMPLICIT Attributes OPTIONAL,
     * digestEncryptionAlgorithm DigestEncryptionAlgorithmIdentifier,
     * encryptedDigest EncryptedDigest,
     * unauthenticatedAttributes [1] IMPLICIT Attributes OPTIONAL
     * }

     * EncryptedDigest ::= OCTET STRING

     * DigestAlgorithmIdentifier ::= AlgorithmIdentifier

     * DigestEncryptionAlgorithmIdentifier ::= AlgorithmIdentifier
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(issuerAndSerialNumber)
        v.add(digestAlgorithm)

        if (authenticatedAttributes != null) {
            v.add(DERTaggedObject(false, 0, authenticatedAttributes))
        }

        v.add(digestEncryptionAlgorithm)
        v.add(encryptedDigest)

        if (unauthenticatedAttributes != null) {
            v.add(DERTaggedObject(false, 1, unauthenticatedAttributes))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                o: Any): SignerInfo {
            if (o is SignerInfo) {
                return o
            } else if (o is ASN1Sequence) {
                return SignerInfo(o)
            }

            throw IllegalArgumentException("unknown object in factory: " + o.javaClass.name)
        }
    }
}
