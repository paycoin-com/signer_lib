package org.bouncycastle.asn1.pkcs

import java.io.IOException
import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Encoding
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

class PrivateKeyInfo : ASN1Object {
    private var privKey: ASN1OctetString? = null
    var privateKeyAlgorithm: AlgorithmIdentifier? = null
        private set
    var attributes: ASN1Set? = null
        private set

    @Throws(IOException::class)
    @JvmOverloads constructor(
            algId: AlgorithmIdentifier,
            privateKey: ASN1Encodable,
            attributes: ASN1Set? = null) {
        this.privKey = DEROctetString(privateKey.toASN1Primitive().getEncoded(ASN1Encoding.DER))
        this.privateKeyAlgorithm = algId
        this.attributes = attributes
    }

    /**
     * @param seq
     */
    @Deprecated("use PrivateKeyInfo.getInstance()\n      ")
    constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        val version = (e.nextElement() as ASN1Integer).value
        if (version.toInt() != 0) {
            throw IllegalArgumentException("wrong version for private key info")
        }

        privateKeyAlgorithm = AlgorithmIdentifier.getInstance(e.nextElement())
        privKey = ASN1OctetString.getInstance(e.nextElement())

        if (e.hasMoreElements()) {
            attributes = ASN1Set.getInstance(e.nextElement() as ASN1TaggedObject, false)
        }
    }

    val algorithmId: AlgorithmIdentifier
        @Deprecated("use getPrivateKeyAlgorithm()")
        get() = privateKeyAlgorithm

    @Throws(IOException::class)
    fun parsePrivateKey(): ASN1Encodable {
        return ASN1Primitive.fromByteArray(privKey!!.octets)
    }


    val privateKey: ASN1Primitive
        @Deprecated("use parsePrivateKey()")
        get() {
            try {
                return parsePrivateKey().toASN1Primitive()
            } catch (e: IOException) {
                throw IllegalStateException("unable to parse private key")
            }

        }

    /**
     * write out an RSA private key with its associated information
     * as described in PKCS8.
     *
     * PrivateKeyInfo ::= SEQUENCE {
     * version Version,
     * privateKeyAlgorithm AlgorithmIdentifier {{PrivateKeyAlgorithms}},
     * privateKey PrivateKey,
     * attributes [0] IMPLICIT Attributes OPTIONAL
     * }
     * Version ::= INTEGER {v1(0)} (v1,...)

     * PrivateKey ::= OCTET STRING

     * Attributes ::= SET OF Attribute
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(ASN1Integer(0))
        v.add(privateKeyAlgorithm)
        v.add(privKey)

        if (attributes != null) {
            v.add(DERTaggedObject(false, 0, attributes))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): PrivateKeyInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): PrivateKeyInfo? {
            if (obj is PrivateKeyInfo) {
                return obj
            } else if (obj != null) {
                return PrivateKeyInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
