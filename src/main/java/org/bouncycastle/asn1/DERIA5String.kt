package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * DER IA5String object - this is an ascii string.
 */
class DERIA5String : ASN1Primitive, ASN1String {
    private val string: ByteArray

    /**
     * basic constructor - with bytes.
     * @param string the byte encoding of the characters making up the string.
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
     * * contains characters that should not be in an IA5String.
     */
    @JvmOverloads constructor(
            string: String?,
            validate: Boolean = false) {
        if (string == null) {
            throw NullPointerException("string cannot be null")
        }
        if (validate && !isIA5String(string)) {
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
        out.writeEncoded(BERTags.IA5_STRING, string)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is DERIA5String) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    companion object {

        /**
         * return a IA5 string from the passed in object

         * @param obj a DERIA5String or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERIA5String instance, or null.
         */
        fun getInstance(
                obj: Any?): DERIA5String {
            if (obj == null || obj is DERIA5String) {
                return obj as DERIA5String?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERIA5String
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an IA5 String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return a DERIA5String instance, or null.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERIA5String {
            val o = obj.`object`

            if (explicit || o is DERIA5String) {
                return getInstance(o)
            } else {
                return DERIA5String((o as ASN1OctetString).octets)
            }
        }

        /**
         * return true if the passed in String can be represented without
         * loss as an IA5String, false otherwise.

         * @param str the string to check.
         * *
         * @return true if character set in IA5String set, false otherwise.
         */
        fun isIA5String(
                str: String): Boolean {
            for (i in str.length - 1 downTo 0) {
                val ch = str[i]

                if (ch.toInt() > 0x007f) {
                    return false
                }
            }

            return true
        }
    }
}
/**
 * basic constructor - without validation.
 * @param string the base string to use..
 */
