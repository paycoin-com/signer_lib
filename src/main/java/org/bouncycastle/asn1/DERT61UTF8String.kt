package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * DER T61String (also the teletex string) - a "modern" encapsulation that uses UTF-8. If at all possible, avoid this one! It's only for emergencies.
 * Use UTF8String instead.
 */
@Deprecated("don't use this class, introduced in error, it will be removed.")
class DERT61UTF8String
/**
 * basic constructor - string encoded as a sequence of bytes.
 */
(
        private val string: ByteArray) : ASN1Primitive(), ASN1String {

    /**
     * basic constructor - with string UTF8 conversion assumed.
     */
    constructor(
            string: String) : this(Strings.toUTF8ByteArray(string)) {
    }

    /**
     * Decode the encoded string and return it, UTF8 assumed.

     * @return the decoded String
     */
    override fun getString(): String {
        return Strings.fromUTF8ByteArray(string)
    }

    override fun toString(): String {
        return getString()
    }

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(string.size) + string.size
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeEncoded(BERTags.T61_STRING, string)
    }

    /**
     * Return the encoded string as a byte array.

     * @return the actual bytes making up the encoded body of the T61 string.
     */
    val octets: ByteArray
        get() = Arrays.clone(string)

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is DERT61UTF8String) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    companion object {

        /**
         * return a T61 string from the passed in object. UTF-8 Encoding is assumed in this case.

         * @param obj a DERT61UTF8String or an object that can be converted into one.
         * *
         * @throws IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERT61UTF8String instance, or null
         */
        fun getInstance(
                obj: Any?): DERT61UTF8String {
            if (obj is DERT61String) {
                return DERT61UTF8String(obj.octets)
            }

            if (obj == null || obj is DERT61UTF8String) {
                return obj as DERT61UTF8String?
            }

            if (obj is ByteArray) {
                try {
                    return DERT61UTF8String((ASN1Primitive.fromByteArray(obj as ByteArray?) as DERT61String).octets)
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an T61 String from a tagged object. UTF-8 encoding is assumed in this case.

         * @param obj      the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *                 tagged false otherwise.
         * *
         * @throws IllegalArgumentException if the tagged object cannot
         * * be converted.
         * *
         * @return a DERT61UTF8String instance, or null
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERT61UTF8String {
            val o = obj.`object`

            if (explicit || o is DERT61String || o is DERT61UTF8String) {
                return getInstance(o)
            } else {
                return DERT61UTF8String(ASN1OctetString.getInstance(o).octets)
            }
        }
    }
}
