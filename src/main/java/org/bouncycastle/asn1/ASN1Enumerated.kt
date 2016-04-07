package org.bouncycastle.asn1

import java.io.IOException
import java.math.BigInteger

import org.bouncycastle.util.Arrays

/**
 * Class representing the ASN.1 ENUMERATED type.
 */
open class ASN1Enumerated : ASN1Primitive {
    private val bytes: ByteArray

    /**
     * Constructor from int.

     * @param value the value of this enumerated.
     */
    constructor(
            value: Int) {
        bytes = BigInteger.valueOf(value.toLong()).toByteArray()
    }

    /**
     * Constructor from BigInteger

     * @param value the value of this enumerated.
     */
    constructor(
            value: BigInteger) {
        bytes = value.toByteArray()
    }

    /**
     * Constructor from encoded BigInteger.

     * @param bytes the value of this enumerated as an encoded BigInteger (signed).
     */
    constructor(
            bytes: ByteArray) {
        this.bytes = bytes
    }

    val value: BigInteger
        get() = BigInteger(bytes)

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(bytes.size) + bytes.size
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeEncoded(BERTags.ENUMERATED, bytes)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1Enumerated) {
            return false
        }

        return Arrays.areEqual(this.bytes, o.bytes)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(bytes)
    }

    companion object {

        /**
         * return an enumerated from the passed in object

         * @param obj an ASN1Enumerated or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return an ASN1Enumerated instance, or null.
         */
        fun getInstance(
                obj: Any?): ASN1Enumerated {
            if (obj == null || obj is ASN1Enumerated) {
                return obj as ASN1Enumerated?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as ASN1Enumerated
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an Enumerated from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return an ASN1Enumerated instance, or null.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1Enumerated {
            val o = obj.`object`

            if (explicit || o is ASN1Enumerated) {
                return getInstance(o)
            } else {
                return fromOctetString((o as ASN1OctetString).octets)
            }
        }

        private val cache = arrayOfNulls<ASN1Enumerated>(12)

        internal fun fromOctetString(enc: ByteArray): ASN1Enumerated {
            if (enc.size > 1) {
                return ASN1Enumerated(Arrays.clone(enc))
            }

            if (enc.size == 0) {
                throw IllegalArgumentException("ENUMERATED has zero length")
            }
            val value = enc[0] and 0xff

            if (value >= cache.size) {
                return ASN1Enumerated(Arrays.clone(enc))
            }

            var possibleMatch: ASN1Enumerated? = cache[value]

            if (possibleMatch == null) {
                possibleMatch = cache[value] = ASN1Enumerated(Arrays.clone(enc))
            }

            return possibleMatch
        }
    }
}
