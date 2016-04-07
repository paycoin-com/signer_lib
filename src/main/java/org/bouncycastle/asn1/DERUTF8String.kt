package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * DER UTF8String object.
 */
class DERUTF8String : ASN1Primitive, ASN1String {
    private val string: ByteArray

    /*
     * Basic constructor - byte encoded string.
     */
    internal constructor(string: ByteArray) {
        this.string = string
    }

    /**
     * Basic constructor

     * @param string the string to be carried in the UTF8String object,
     */
    constructor(string: String) {
        this.string = Strings.toUTF8ByteArray(string)
    }

    override fun getString(): String {
        return Strings.fromUTF8ByteArray(string)
    }

    override fun toString(): String {
        return getString()
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    internal override fun asn1Equals(o: ASN1Primitive): Boolean {
        if (o !is DERUTF8String) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    internal override val isConstructed: Boolean
        get() = false

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(string.size) + string.size
    }

    @Throws(IOException::class)
    internal override fun encode(out: ASN1OutputStream) {
        out.writeEncoded(BERTags.UTF8_STRING, string)
    }

    companion object {

        /**
         * Return an UTF8 string from the passed in object.

         * @param obj a DERUTF8String or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException
         * *                if the object cannot be converted.
         * *
         * @return a DERUTF8String instance, or null
         */
        fun getInstance(obj: Any?): DERUTF8String {
            if (obj == null || obj is DERUTF8String) {
                return obj as DERUTF8String?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERUTF8String
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * Return an UTF8 String from a tagged object.

         * @param obj
         * *            the tagged object holding the object we want
         * *
         * @param explicit
         * *            true if the object is meant to be explicitly tagged false
         * *            otherwise.
         * *
         * @exception IllegalArgumentException
         * *                if the tagged object cannot be converted.
         * *
         * @return a DERUTF8String instance, or null
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERUTF8String {
            val o = obj.`object`

            if (explicit || o is DERUTF8String) {
                return getInstance(o)
            } else {
                return DERUTF8String(ASN1OctetString.getInstance(o).octets)
            }
        }
    }
}
