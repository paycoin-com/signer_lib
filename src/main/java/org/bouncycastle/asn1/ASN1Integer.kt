package org.bouncycastle.asn1

import java.io.IOException
import java.math.BigInteger

import org.bouncycastle.util.Arrays

/**
 * Class representing the ASN.1 INTEGER type.
 */
open class ASN1Integer : ASN1Primitive {
    private val bytes: ByteArray

    constructor(
            value: Long) {
        bytes = BigInteger.valueOf(value).toByteArray()
    }

    constructor(
            value: BigInteger) {
        bytes = value.toByteArray()
    }

    constructor(
            bytes: ByteArray) : this(bytes, true) {
    }

    internal constructor(bytes: ByteArray, clone: Boolean) {
        this.bytes = if (clone) Arrays.clone(bytes) else bytes
    }

    val value: BigInteger
        get() = BigInteger(bytes)

    /**
     * in some cases positive values get crammed into a space,
     * that's not quite big enough...
     * @return the BigInteger that results from treating this ASN.1 INTEGER as unsigned.
     */
    val positiveValue: BigInteger
        get() = BigInteger(1, bytes)

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(bytes.size) + bytes.size
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeEncoded(BERTags.INTEGER, bytes)
    }

    override fun hashCode(): Int {
        var value = 0

        for (i in bytes.indices) {
            value = value xor (bytes[i] and 0xff shl i % 4)
        }

        return value
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1Integer) {
            return false
        }

        return Arrays.areEqual(bytes, o.bytes)
    }

    override fun toString(): String {
        return value.toString()
    }

    companion object {

        /**
         * return an integer from the passed in object

         * @param obj an ASN1Integer or an object that can be converted into one.
         * *
         * @throws IllegalArgumentException if the object cannot be converted.
         * *
         * @return an ASN1Integer instance.
         */
        fun getInstance(
                obj: Any?): ASN1Integer {
            if (obj == null || obj is ASN1Integer) {
                return obj as ASN1Integer?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as ASN1Integer
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an Integer from a tagged object.

         * @param obj      the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *                 tagged false otherwise.
         * *
         * @throws IllegalArgumentException if the tagged object cannot
         * * be converted.
         * *
         * @return an ASN1Integer instance.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1Integer {
            val o = obj.`object`

            if (explicit || o is ASN1Integer) {
                return getInstance(o)
            } else {
                return ASN1Integer(ASN1OctetString.getInstance(obj.`object`).octets)
            }
        }
    }

}
