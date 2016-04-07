package org.bouncycastle.asn1.x509

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA1Digest

/**
 * The AuthorityKeyIdentifier object.
 *
 * id-ce-authorityKeyIdentifier OBJECT IDENTIFIER ::=  { id-ce 35 }

 * AuthorityKeyIdentifier ::= SEQUENCE {
 * keyIdentifier             [0] IMPLICIT KeyIdentifier           OPTIONAL,
 * authorityCertIssuer       [1] IMPLICIT GeneralNames            OPTIONAL,
 * authorityCertSerialNumber [2] IMPLICIT CertificateSerialNumber OPTIONAL  }

 * KeyIdentifier ::= OCTET STRING
 *

 */
open class AuthorityKeyIdentifier : ASN1Object {
    internal var keyidentifier: ASN1OctetString? = null
    var authorityCertIssuer: GeneralNames? = null
        internal set
    internal var certserno: ASN1Integer? = null

    protected constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        while (e.hasMoreElements()) {
            val o = DERTaggedObject.getInstance(e.nextElement())

            when (o.tagNo) {
                0 -> this.keyidentifier = ASN1OctetString.getInstance(o, false)
                1 -> this.authorityCertIssuer = GeneralNames.getInstance(o, false)
                2 -> this.certserno = ASN1Integer.getInstance(o, false)
                else -> throw IllegalArgumentException("illegal tag")
            }
        }
    }

    /**

     * Calulates the keyidentifier using a SHA1 hash over the BIT STRING
     * from SubjectPublicKeyInfo as defined in RFC2459.

     * Example of making a AuthorityKeyIdentifier:
     *
     * SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((ASN1Sequence)new ASN1InputStream(
     * publicKey.getEncoded()).readObject());
     * AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);
     *
     */
    @Deprecated("create the extension using org.bouncycastle.cert.X509ExtensionUtils")
    constructor(
            spki: SubjectPublicKeyInfo) {
        val digest = SHA1Digest()
        val resBuf = ByteArray(digest.digestSize)

        val bytes = spki.publicKeyData.bytes
        digest.update(bytes, 0, bytes.size)
        digest.doFinal(resBuf, 0)
        this.keyidentifier = DEROctetString(resBuf)
    }

    /**
     * create an AuthorityKeyIdentifier with the GeneralNames tag and
     * the serial number provided as well.
     */
    @Deprecated("create the extension using org.bouncycastle.cert.X509ExtensionUtils")
    constructor(
            spki: SubjectPublicKeyInfo,
            name: GeneralNames,
            serialNumber: BigInteger) {
        val digest = SHA1Digest()
        val resBuf = ByteArray(digest.digestSize)

        val bytes = spki.publicKeyData.bytes
        digest.update(bytes, 0, bytes.size)
        digest.doFinal(resBuf, 0)

        this.keyidentifier = DEROctetString(resBuf)
        this.authorityCertIssuer = GeneralNames.getInstance(name.toASN1Primitive())
        this.certserno = ASN1Integer(serialNumber)
    }

    /**
     * create an AuthorityKeyIdentifier with the GeneralNames tag and
     * the serial number provided.
     */
    constructor(
            name: GeneralNames,
            serialNumber: BigInteger) : this(null as ByteArray, name, serialNumber) {
    }

    /**
     * create an AuthorityKeyIdentifier with a precomputed key identifier
     * and the GeneralNames tag and the serial number provided as well.
     */
    @JvmOverloads constructor(
            keyIdentifier: ByteArray?,
            name: GeneralNames? = null,
            serialNumber: BigInteger? = null) {
        this.keyidentifier = if (keyIdentifier != null) DEROctetString(keyIdentifier) else null
        this.authorityCertIssuer = name
        this.certserno = if (serialNumber != null) ASN1Integer(serialNumber) else null
    }

    val keyIdentifier: ByteArray?
        get() {
            if (keyidentifier != null) {
                return keyidentifier!!.octets
            }

            return null
        }

    val authorityCertSerialNumber: BigInteger?
        get() {
            if (certserno != null) {
                return certserno!!.value
            }

            return null
        }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (keyidentifier != null) {
            v.add(DERTaggedObject(false, 0, keyidentifier))
        }

        if (authorityCertIssuer != null) {
            v.add(DERTaggedObject(false, 1, authorityCertIssuer))
        }

        if (certserno != null) {
            v.add(DERTaggedObject(false, 2, certserno))
        }


        return DERSequence(v)
    }

    override fun toString(): String {
        return "AuthorityKeyIdentifier: KeyID(" + this.keyidentifier!!.octets + ")"
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): AuthorityKeyIdentifier {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): AuthorityKeyIdentifier? {
            if (obj is AuthorityKeyIdentifier) {
                return obj
            }
            if (obj != null) {
                return AuthorityKeyIdentifier(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun fromExtensions(extensions: Extensions): AuthorityKeyIdentifier {
            return AuthorityKeyIdentifier.getInstance(extensions.getExtensionParsedValue(Extension.authorityKeyIdentifier))
        }
    }
}
/**
 * create an AuthorityKeyIdentifier with a precomputed key identifier
 */
