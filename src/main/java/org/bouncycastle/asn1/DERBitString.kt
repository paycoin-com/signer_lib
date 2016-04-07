package org.bouncycastle.asn1

import java.io.IOException

/**
 * A BIT STRING with DER encoding.
 */
open class DERBitString : ASN1BitString {

    protected constructor(
            data: Byte,
            padBits: Int) : this(toByteArray(data), padBits) {
    }

    /**
     * @param data the octets making up the bit string.
     * *
     * @param padBits the number of extra bits at the end of the string.
     */
    @JvmOverloads constructor(
            data: ByteArray,
            padBits: Int = 0) : super(data, padBits) {
    }

    constructor(
            value: Int) : super(ASN1BitString.getBytes(value), ASN1BitString.getPadBits(value)) {
    }

    @Throws(IOException::class)
    constructor(
            obj: ASN1Encodable) : super(obj.toASN1Primitive().getEncoded(ASN1Encoding.DER), 0) {
    }

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(data.size + 1) + data.size + 1
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        val string = ASN1BitString.derForm(data, padBits)
        val bytes = ByteArray(string.size + 1)

        bytes[0] = padBits.toByte()
        System.arraycopy(string, 0, bytes, 1, bytes.size - 1)

        out.writeEncoded(BERTags.BIT_STRING, bytes)
    }

    companion object {
        /**
         * return a Bit String from the passed in object

         * @param obj a DERBitString or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERBitString instance, or null.
         */
        fun getInstance(
                obj: Any?): DERBitString {
            if (obj == null || obj is DERBitString) {
                return obj as DERBitString?
            }
            if (obj is DLBitString) {
                return DERBitString(obj.data, obj.padBits)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return a Bit String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return a DERBitString instance, or null.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERBitString {
            val o = obj.`object`

            if (explicit || o is DERBitString) {
                return getInstance(o)
            } else {
                return fromOctetString((o as ASN1OctetString).octets)
            }
        }

        private fun toByteArray(data: Byte): ByteArray {
            val rv = ByteArray(1)

            rv[0] = data

            return rv
        }

        internal fun fromOctetString(bytes: ByteArray): DERBitString {
            if (bytes.size < 1) {
                throw IllegalArgumentException("truncated BIT STRING detected")
            }

            val padBits = bytes[0].toInt()
            val data = ByteArray(bytes.size - 1)

            if (data.size != 0) {
                System.arraycopy(bytes, 1, data, 0, bytes.size - 1)
            }

            return DERBitString(data, padBits)
        }
    }
}
