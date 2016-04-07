package org.bouncycastle.asn1.x509

import java.io.IOException
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence

/**
 * The object that contains the public key stored in a certficate.
 *
 *
 * The getEncoded() method in the public keys in the JCE produces a DER
 * encoded one of these.
 */
class SubjectPublicKeyInfo : ASN1Object {
    var algorithm: AlgorithmIdentifier? = null
        private set
    /**
     * for when the public key is raw bits.

     * @return the public key as the raw bit string...
     */
    var publicKeyData: DERBitString? = null
        private set

    @Throws(IOException::class)
    constructor(
            algId: AlgorithmIdentifier,
            publicKey: ASN1Encodable) {
        this.publicKeyData = DERBitString(publicKey)
        this.algorithm = algId
    }

    constructor(
            algId: AlgorithmIdentifier,
            publicKey: ByteArray) {
        this.publicKeyData = DERBitString(publicKey)
        this.algorithm = algId
    }


    @Deprecated("use SubjectPublicKeyInfo.getInstance()")
    constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val e = seq.objects

        this.algorithm = AlgorithmIdentifier.getInstance(e.nextElement())
        this.publicKeyData = DERBitString.getInstance(e.nextElement())
    }

    /**
     * @return    alg ID.
     */
    val algorithmId: AlgorithmIdentifier
        @Deprecated("use getAlgorithm()\n      ")
        get() = algorithm

    /**
     * for when the public key is an encoded object - if the bitstring
     * can't be decoded this routine throws an IOException.

     * @exception IOException - if the bit string doesn't represent a DER
     * * encoded object.
     * *
     * @return the public key as an ASN.1 primitive.
     */
    @Throws(IOException::class)
    fun parsePublicKey(): ASN1Primitive {
        val aIn = ASN1InputStream(publicKeyData!!.octets)

        return aIn.readObject()
    }

    /**
     * for when the public key is an encoded object - if the bitstring
     * can't be decoded this routine throws an IOException.

     * @exception IOException - if the bit string doesn't represent a DER
     * * encoded object.
     * *
     * @return the public key as an ASN.1 primitive.
     */
    val publicKey: ASN1Primitive
        @Deprecated("use parsePublicKey\n      ")
        @Throws(IOException::class)
        get() {
            val aIn = ASN1InputStream(publicKeyData!!.octets)

            return aIn.readObject()
        }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * SubjectPublicKeyInfo ::= SEQUENCE {
     * algorithm AlgorithmIdentifier,
     * publicKey BIT STRING }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(algorithm)
        v.add(publicKeyData)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): SubjectPublicKeyInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): SubjectPublicKeyInfo? {
            if (obj is SubjectPublicKeyInfo) {
                return obj
            } else if (obj != null) {
                return SubjectPublicKeyInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
