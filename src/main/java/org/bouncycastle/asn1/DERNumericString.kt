package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * DER NumericString object - this is an ascii string of characters {0,1,2,3,4,5,6,7,8,9, }.
 */
class DERNumericString : ASN1Primitive, ASN1String {
    private val string: ByteArray

    /**
     * basic constructor - with bytes.
     */
    internal constructor(
            string: ByteArray) {
        this.string = string
    }

    /**
     * Constructor with optional validation.

     * @param string the base string to wrap.
     * *
     * @param validate whether or not to check the string.
     * *
     * @throws IllegalArgumentException if validate is true and the string
     * * contains characters that should not be in a NumericString.
     */
    @JvmOverloads constructor(
            string: String,
            validate: Boolean = false) {
        if (validate && !isNumericString(string)) {
            throw IllegalArgumentException("string contains illegal characters")
        }

        this.string = Strings.toByteArray(string)
    }

    override fun getString(): String {
        return Strings.fromByteArray(string)
    }

    override fun toString(): String {
        return getString()
    }

    val octets: ByteArray
        get() = Arrays.clone(string)

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(string.size) + string.size
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeEncoded(BERTags.NUMERIC_STRING, string)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is DERNumericString) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    companion object {

        /**
         * return a Numeric string from the passed in object

         * @param obj a DERNumericString or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERNumericString instance, or null
         */
        fun getInstance(
                obj: Any?): DERNumericString {
            if (obj == null || obj is DERNumericString) {
                return obj as DERNumericString?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERNumericString
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an Numeric String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return a DERNumericString instance, or null.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERNumericString {
            val o = obj.`object`

            if (explicit || o is DERNumericString) {
                return getInstance(o)
            } else {
                return DERNumericString(ASN1OctetString.getInstance(o).octets)
            }
        }

        /**
         * Return true if the string can be represented as a NumericString ('0'..'9', ' ')

         * @param str string to validate.
         * *
         * @return true if numeric, fale otherwise.
         */
        fun isNumericString(
                str: String): Boolean {
            for (i in str.length - 1 downTo 0) {
                val ch = str[i]

                if (ch.toInt() > 0x007f) {
                    return false
                }

                if ('0' <= ch && ch <= '9' || ch == ' ') {
                    continue
                }

                return false
            }

            return true
        }
    }
}
/**
 * basic constructor -  without validation..
 */
