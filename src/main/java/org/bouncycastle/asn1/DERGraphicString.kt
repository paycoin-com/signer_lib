package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

class DERGraphicString
/**
 * basic constructor - with bytes.
 * @param string the byte encoding of the characters making up the string.
 */
(
        string: ByteArray) : ASN1Primitive(), ASN1String {
    private val string: ByteArray

    init {
        this.string = Arrays.clone(string)
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
        out.writeEncoded(BERTags.GRAPHIC_STRING, string)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is DERGraphicString) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    override fun getString(): String {
        return Strings.fromByteArray(string)
    }

    companion object {

        /**
         * return a Graphic String from the passed in object

         * @param obj a DERGraphicString or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERGraphicString instance, or null.
         */
        fun getInstance(
                obj: Any?): DERGraphicString {
            if (obj == null || obj is DERGraphicString) {
                return obj as DERGraphicString?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERGraphicString
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return a Graphic String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return a DERGraphicString instance, or null.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERGraphicString {
            val o = obj.`object`

            if (explicit || o is DERGraphicString) {
                return getInstance(o)
            } else {
                return DERGraphicString((o as ASN1OctetString).octets)
            }
        }
    }
}
