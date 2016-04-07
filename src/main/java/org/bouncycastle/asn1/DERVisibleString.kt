package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * DER VisibleString object encoding ISO 646 (ASCII) character code points 32 to 126.
 *
 *
 * Explicit character set escape sequences are not allowed.
 *
 */
class DERVisibleString : ASN1Primitive, ASN1String {
    private val string: ByteArray

    /*
     * Basic constructor - byte encoded string.
     */
    internal constructor(
            string: ByteArray) {
        this.string = string
    }

    /**
     * Basic constructor

     * @param string the string to be carried in the VisibleString object,
     */
    constructor(
            string: String) {
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
        out.writeEncoded(BERTags.VISIBLE_STRING, this.string)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is DERVisibleString) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    companion object {

        /**
         * Return a Visible String from the passed in object.

         * @param obj a DERVisibleString or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERVisibleString instance, or null
         */
        fun getInstance(
                obj: Any?): DERVisibleString {
            if (obj == null || obj is DERVisibleString) {
                return obj as DERVisibleString?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERVisibleString
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * Return a Visible String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return a DERVisibleString instance, or null
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERVisibleString {
            val o = obj.`object`

            if (explicit || o is DERVisibleString) {
                return getInstance(o)
            } else {
                return DERVisibleString(ASN1OctetString.getInstance(o).octets)
            }
        }
    }
}
