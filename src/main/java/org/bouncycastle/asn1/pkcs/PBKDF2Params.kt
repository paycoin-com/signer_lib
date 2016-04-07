package org.bouncycastle.asn1.pkcs

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.util.Arrays

/**
 *
 * PBKDF2-params ::= SEQUENCE {
 * salt CHOICE {
 * specified OCTET STRING,
 * otherSource AlgorithmIdentifier {{PBKDF2-SaltSources}}
 * },
 * iterationCount INTEGER (1..MAX),
 * keyLength INTEGER (1..MAX) OPTIONAL,
 * prf AlgorithmIdentifier {{PBKDF2-PRFs}} DEFAULT algid-hmacWithSHA1 }
 *
 */
class PBKDF2Params : ASN1Object {

    private val octStr: ASN1OctetString
    private val iterationCount: ASN1Integer
    private val keyLength: ASN1Integer?
    private val prf: AlgorithmIdentifier?

    /**
     * Create a PBKDF2Params with the specified salt, iteration count, keyLength, and a defined prf.

     * @param salt           input salt.
     * *
     * @param iterationCount input iteration count.
     * *
     * @param keyLength      intended key length to be produced.
     * *
     * @param prf            the pseudo-random function to use.
     */
    @JvmOverloads constructor(
            salt: ByteArray,
            iterationCount: Int,
            keyLength: Int = 0,
            prf: AlgorithmIdentifier? = null) {
        this.octStr = DEROctetString(Arrays.clone(salt))
        this.iterationCount = ASN1Integer(iterationCount.toLong())

        if (keyLength > 0) {
            this.keyLength = ASN1Integer(keyLength.toLong())
        } else {
            this.keyLength = null
        }

        this.prf = prf
    }

    /**
     * Create a PBKDF2Params with the specified salt, iteration count, and a defined prf.

     * @param salt           input salt.
     * *
     * @param iterationCount input iteration count.
     * *
     * @param prf            the pseudo-random function to use.
     */
    constructor(
            salt: ByteArray,
            iterationCount: Int,
            prf: AlgorithmIdentifier) : this(salt, iterationCount, 0, prf) {
    }

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        octStr = e.nextElement() as ASN1OctetString
        iterationCount = e.nextElement() as ASN1Integer

        if (e.hasMoreElements()) {
            var o: Any? = e.nextElement()

            if (o is ASN1Integer) {
                keyLength = ASN1Integer.getInstance(o)
                if (e.hasMoreElements()) {
                    o = e.nextElement()
                } else {
                    o = null
                }
            } else {
                keyLength = null
            }

            if (o != null) {
                prf = AlgorithmIdentifier.getInstance(o)
            } else {
                prf = null
            }
        } else {
            keyLength = null
            prf = null
        }
    }

    /**
     * Return the salt to use.

     * @return the input salt.
     */
    val salt: ByteArray
        get() = octStr.octets

    /**
     * Return the iteration count to use.

     * @return the input iteration count.
     */
    fun getIterationCount(): BigInteger {
        return iterationCount.value
    }

    /**
     * Return the intended length in octets of the derived key.

     * @return length in octets for derived key, if specified.
     */
    fun getKeyLength(): BigInteger? {
        if (keyLength != null) {
            return keyLength.value
        }

        return null
    }

    /**
     * Return true if the PRF is the default (hmacWithSHA1)

     * @return true if PRF is default, false otherwise.
     */
    val isDefaultPrf: Boolean
        get() = prf == null || prf == algid_hmacWithSHA1

    /**
     * Return the algId of the underlying pseudo random function to use.

     * @return the prf algorithm identifier.
     */
    fun getPrf(): AlgorithmIdentifier {
        if (prf != null) {
            return prf
        }

        return algid_hmacWithSHA1
    }

    /**
     * Return an ASN.1 structure suitable for encoding.

     * @return the object as an ASN.1 encodable structure.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(octStr)
        v.add(iterationCount)

        if (keyLength != null) {
            v.add(keyLength)
        }

        if (prf != null && prf != algid_hmacWithSHA1) {
            v.add(prf)
        }

        return DERSequence(v)
    }

    companion object {
        private val algid_hmacWithSHA1 = AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA1, DERNull.INSTANCE)

        /**
         * Create PBKDF2Params from the passed in object,

         * @param obj either PBKDF2Params or an ASN1Sequence.
         * *
         * @return a PBKDF2Params instance.
         */
        fun getInstance(
                obj: Any?): PBKDF2Params? {
            if (obj is PBKDF2Params) {
                return obj
            }

            if (obj != null) {
                return PBKDF2Params(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
/**
 * Create a PBKDF2Params with the specified salt, iteration count, and algid-hmacWithSHA1 for the prf.

 * @param salt           input salt.
 * *
 * @param iterationCount input iteration count.
 */
/**
 * Create a PBKDF2Params with the specified salt, iteration count, keyLength, and algid-hmacWithSHA1 for the prf.

 * @param salt           input salt.
 * *
 * @param iterationCount input iteration count.
 * *
 * @param keyLength      intended key length to be produced.
 */
