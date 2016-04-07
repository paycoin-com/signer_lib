package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays

/**
 * Carrier class for DER encoding BMPString object.
 */
class DERBMPString : ASN1Primitive, ASN1String {
    private val string: CharArray

    /**
     * basic constructor - byte encoded string.
     * @param string the encoded BMP STRING to wrap.
     */
    internal constructor(
            string: ByteArray) {
        val cs = CharArray(string.size / 2)

        for (i in cs.indices) {
            cs[i] = (string[2 * i] shl 8 or (string[2 * i + 1] and 0xff)).toChar()
        }

        this.string = cs
    }

    internal constructor(string: CharArray) {
        this.string = string
    }

    /**
     * basic constructor
     * @param string a String to wrap as a BMP STRING.
     */
    constructor(
            string: String) {
        this.string = string.toCharArray()
    }

    override fun getString(): String {
        return String(string)
    }

    override fun toString(): String {
        return getString()
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is DERBMPString) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(string.size * 2) + string.size * 2
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.write(BERTags.BMP_STRING)
        out.writeLength(string.size * 2)

        for (i in string.indices) {
            val c = string[i]

            out.write((c.toInt() shr 8).toByte().toInt())
            out.write(c.toByte().toInt())
        }
    }

    companion object {

        /**
         * return a BMP String from the given object.

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERBMPString instance, or null.
         */
        fun getInstance(
                obj: Any?): DERBMPString {
            if (obj == null || obj is DERBMPString) {
                return obj as DERBMPString?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERBMPString
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return a BMP String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *              be converted.
         * *
         * @return a DERBMPString instance.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERBMPString {
            val o = obj.`object`

            if (explicit || o is DERBMPString) {
                return getInstance(o)
            } else {
                return DERBMPString(ASN1OctetString.getInstance(o).octets)
            }
        }
    }
}
