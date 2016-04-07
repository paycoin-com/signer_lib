package org.bouncycastle.asn1.cms

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
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-5.3):
 * Signature container per Signer, see [SignerIdentifier].
 *
 * PKCS#7:

 * SignerInfo ::= SEQUENCE {
 * version                   Version,
 * sid                       SignerIdentifier,
 * digestAlgorithm           DigestAlgorithmIdentifier,
 * authenticatedAttributes   [0] IMPLICIT Attributes OPTIONAL,
 * digestEncryptionAlgorithm DigestEncryptionAlgorithmIdentifier,
 * encryptedDigest           EncryptedDigest,
 * unauthenticatedAttributes [1] IMPLICIT Attributes OPTIONAL
 * }

 * EncryptedDigest ::= OCTET STRING

 * DigestAlgorithmIdentifier ::= AlgorithmIdentifier

 * DigestEncryptionAlgorithmIdentifier ::= AlgorithmIdentifier

 * -----------------------------------------

 * RFC 5652:

 * SignerInfo ::= SEQUENCE {
 * version            CMSVersion,
 * sid                SignerIdentifier,
 * digestAlgorithm    DigestAlgorithmIdentifier,
 * signedAttrs        [0] IMPLICIT SignedAttributes OPTIONAL,
 * signatureAlgorithm SignatureAlgorithmIdentifier,
 * signature          SignatureValue,
 * unsignedAttrs      [1] IMPLICIT UnsignedAttributes OPTIONAL
 * }

 * -- [SignerIdentifier] referenced certificates are at containing
 * -- [SignedData] certificates element.

 * SignerIdentifier ::= CHOICE {
 * issuerAndSerialNumber [IssuerAndSerialNumber],
 * subjectKeyIdentifier  [0] SubjectKeyIdentifier }

 * -- See [Attributes] for generalized SET OF [Attribute]

 * SignedAttributes   ::= SET SIZE (1..MAX) OF Attribute
 * UnsignedAttributes ::= SET SIZE (1..MAX) OF Attribute

 * [Attribute] ::= SEQUENCE {
 * attrType   OBJECT IDENTIFIER,
 * attrValues SET OF AttributeValue }

 * AttributeValue ::= ANY

 * SignatureValue ::= OCTET STRING
 *
 */
class SignerInfo : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var sid: SignerIdentifier? = null
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

    /**

     * @param sid
     * *
     * @param digAlgorithm            CMS knows as 'digestAlgorithm'
     * *
     * @param authenticatedAttributes CMS knows as 'signedAttrs'
     * *
     * @param digEncryptionAlgorithm  CMS knows as 'signatureAlgorithm'
     * *
     * @param encryptedDigest         CMS knows as 'signature'
     * *
     * @param unauthenticatedAttributes CMS knows as 'unsignedAttrs'
     */
    constructor(
            sid: SignerIdentifier,
            digAlgorithm: AlgorithmIdentifier,
            authenticatedAttributes: ASN1Set,
            digEncryptionAlgorithm: AlgorithmIdentifier,
            encryptedDigest: ASN1OctetString,
            unauthenticatedAttributes: ASN1Set) {
        if (sid.isTagged) {
            this.version = ASN1Integer(3)
        } else {
            this.version = ASN1Integer(1)
        }

        this.sid = sid
        this.digestAlgorithm = digAlgorithm
        this.authenticatedAttributes = authenticatedAttributes
        this.digestEncryptionAlgorithm = digEncryptionAlgorithm
        this.encryptedDigest = encryptedDigest
        this.unauthenticatedAttributes = unauthenticatedAttributes
    }

    /**

     * @param sid
     * *
     * @param digAlgorithm            CMS knows as 'digestAlgorithm'
     * *
     * @param authenticatedAttributes CMS knows as 'signedAttrs'
     * *
     * @param digEncryptionAlgorithm  CMS knows as 'signatureAlgorithm'
     * *
     * @param encryptedDigest         CMS knows as 'signature'
     * *
     * @param unauthenticatedAttributes CMS knows as 'unsignedAttrs'
     */
    constructor(
            sid: SignerIdentifier,
            digAlgorithm: AlgorithmIdentifier,
            authenticatedAttributes: Attributes,
            digEncryptionAlgorithm: AlgorithmIdentifier,
            encryptedDigest: ASN1OctetString,
            unauthenticatedAttributes: Attributes) {
        if (sid.isTagged) {
            this.version = ASN1Integer(3)
        } else {
            this.version = ASN1Integer(1)
        }

        this.sid = sid
        this.digestAlgorithm = digAlgorithm
        this.authenticatedAttributes = ASN1Set.getInstance(authenticatedAttributes)
        this.digestEncryptionAlgorithm = digEncryptionAlgorithm
        this.encryptedDigest = encryptedDigest
        this.unauthenticatedAttributes = ASN1Set.getInstance(unauthenticatedAttributes)
    }


    @Deprecated("use getInstance() method.")
    constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        version = e.nextElement() as ASN1Integer
        sid = SignerIdentifier.getInstance(e.nextElement())
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
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(sid)
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

        /**
         * Return a SignerInfo object from the given input
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [SignerInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with SignerInfo structure inside
         *

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        @Throws(IllegalArgumentException::class)
        fun getInstance(
                o: Any?): SignerInfo? {
            if (o is SignerInfo) {
                return o
            } else if (o != null) {
                return SignerInfo(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
