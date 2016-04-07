package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * DER T61String (also the teletex string), try not to use this if you don't need to. The standard support the encoding for
 * this has been withdrawn.
 */
class DERT61String
/**
 * basic constructor - string encoded as a sequence of bytes.

 * @param string the byte encoding of the string to be wrapped.
 */
(
        private val string: ByteArray) : ASN1Primitive(), ASN1String {

    /**
     * basic constructor - with string 8 bit assumed.

     * @param string the string to be wrapped.
     */
    constructor(
            string: String) : this(Strings.toByteArray(string)) {
    }

    /**
     * Decode the encoded string and return it, 8 bit encoding assumed.
     * @return the decoded String
     */
    override fun getString(): String {
        return Strings.fromByteArray(string)
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
        if (o !is DERT61String) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    companion object {

        /**
         * return a T61 string from the passed in object.

         * @param obj a DERT61String or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERT61String instance, or null
         */
        fun getInstance(
                obj: Any?): DERT61String {
            if (obj == null || obj is DERT61String) {
                return obj as DERT61String?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERT61String
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an T61 String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return a DERT61String instance, or null
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERT61String {
            val o = obj.`object`

            if (explicit || o is DERT61String) {
                return getInstance(o)
            } else {
                return DERT61String(ASN1OctetString.getInstance(o).octets)
            }
        }
    }
}
