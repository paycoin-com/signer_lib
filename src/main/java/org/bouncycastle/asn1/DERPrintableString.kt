package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * DER PrintableString object.
 */
class DERPrintableString : ASN1Primitive, ASN1String {
    private val string: ByteArray

    /**
     * basic constructor - byte encoded string.
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
     * * contains characters that should not be in a PrintableString.
     */
    @JvmOverloads constructor(
            string: String,
            validate: Boolean = false) {
        if (validate && !isPrintableString(string)) {
            throw IllegalArgumentException("string contains illegal characters")
        }

        this.string = Strings.toByteArray(string)
    }

    override fun getString(): String {
        return Strings.fromByteArray(string)
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
        out.writeEncoded(BERTags.PRINTABLE_STRING, string)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is DERPrintableString) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    override fun toString(): String {
        return getString()
    }

    companion object {

        /**
         * return a printable string from the passed in object.

         * @param obj a DERPrintableString or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERPrintableString instance, or null.
         */
        fun getInstance(
                obj: Any?): DERPrintableString {
            if (obj == null || obj is DERPrintableString) {
                return obj as DERPrintableString?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERPrintableString
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return a Printable String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return a DERPrintableString instance, or null.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERPrintableString {
            val o = obj.`object`

            if (explicit || o is DERPrintableString) {
                return getInstance(o)
            } else {
                return DERPrintableString(ASN1OctetString.getInstance(o).octets)
            }
        }

        /**
         * return true if the passed in String can be represented without
         * loss as a PrintableString, false otherwise.

         * @return true if in printable set, false otherwise.
         */
        fun isPrintableString(
                str: String): Boolean {
            for (i in str.length - 1 downTo 0) {
                val ch = str[i]

                if (ch.toInt() > 0x007f) {
                    return false
                }

                if ('a' <= ch && ch <= 'z') {
                    continue
                }

                if ('A' <= ch && ch <= 'Z') {
                    continue
                }

                if ('0' <= ch && ch <= '9') {
                    continue
                }

                when (ch) {
                    ' ', '\'', '(', ')', '+', '-', '.', ':', '=', '?', '/', ',' -> continue
                }

                return false
            }

            return true
        }
    }
}
/**
 * basic constructor - this does not validate the string
 */
