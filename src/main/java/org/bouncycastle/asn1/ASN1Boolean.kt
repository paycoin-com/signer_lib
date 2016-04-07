package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays

/**
 * Public facade of ASN.1 Boolean data.
 *
 *
 * Use following to place a new instance of ASN.1 Boolean in your dataset:
 *
 *  *  ASN1Boolean.TRUE literal
 *  *  ASN1Boolean.FALSE literal
 *  *  [ASN1Boolean.getInstance(boolean)][ASN1Boolean.getInstance]
 *  *  [ASN1Boolean.getInstance(int)][ASN1Boolean.getInstance]
 *
 *
 */
open class ASN1Boolean : ASN1Primitive {

    private val value: ByteArray

    internal constructor(
            value: ByteArray) {
        if (value.size != 1) {
            throw IllegalArgumentException("byte value should have 1 byte in it")
        }

        if (value[0].toInt() == 0) {
            this.value = FALSE_VALUE
        } else if (value[0] and 0xff == 0xff) {
            this.value = TRUE_VALUE
        } else {
            this.value = Arrays.clone(value)
        }
    }

    /**
     * @param value true or false.
     */
    @Deprecated("use getInstance(boolean) method.\n      ")
    constructor(
            value: Boolean) {
        this.value = if (value) TRUE_VALUE else FALSE_VALUE
    }

    val isTrue: Boolean
        get() = value[0].toInt() != 0

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 3
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeEncoded(BERTags.BOOLEAN, value)
    }

    override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o is ASN1Boolean) {
            return value[0] == o.value[0]
        }

        return false
    }

    override fun hashCode(): Int {
        return value[0].toInt()
    }


    override fun toString(): String {
        return if (value[0].toInt() != 0) "TRUE" else "FALSE"
    }

    companion object {
        private val TRUE_VALUE = byteArrayOf(0xff.toByte())
        private val FALSE_VALUE = byteArrayOf(0)

        val FALSE = ASN1Boolean(false)
        val TRUE = ASN1Boolean(true)

        /**
         * return a boolean from the passed in object.

         * @param obj an ASN1Boolean or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return an ASN1Boolean instance.
         */
        fun getInstance(
                obj: Any?): ASN1Boolean {
            if (obj == null || obj is ASN1Boolean) {
                return obj as ASN1Boolean?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj) as ASN1Boolean
                } catch (e: IOException) {
                    throw IllegalArgumentException("failed to construct boolean from byte[]: " + e.message)
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an ASN1Boolean from the passed in boolean.
         * @param value true or false depending on the ASN1Boolean wanted.
         * *
         * @return an ASN1Boolean instance.
         */
        fun getInstance(
                value: Boolean): ASN1Boolean {
            return if (value) TRUE else FALSE
        }

        /**
         * return an ASN1Boolean from the passed in value.
         * @param value non-zero (true) or zero (false) depending on the ASN1Boolean wanted.
         * *
         * @return an ASN1Boolean instance.
         */
        fun getInstance(
                value: Int): ASN1Boolean {
            return if (value != 0) TRUE else FALSE
        }

        /**
         * return a Boolean from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return an ASN1Boolean instance.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1Boolean {
            val o = obj.`object`

            if (explicit || o is ASN1Boolean) {
                return getInstance(o)
            } else {
                return ASN1Boolean.fromOctetString((o as ASN1OctetString).octets)
            }
        }

        internal fun fromOctetString(value: ByteArray): ASN1Boolean {
            if (value.size != 1) {
                throw IllegalArgumentException("BOOLEAN value should have 1 byte in it")
            }

            if (value[0].toInt() == 0) {
                return FALSE
            } else if (value[0] and 0xff == 0xff) {
                return TRUE
            } else {
                return ASN1Boolean(value)
            }
        }
    }
}
