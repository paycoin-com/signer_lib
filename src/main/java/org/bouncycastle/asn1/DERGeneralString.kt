package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * Carrier class for a DER encoding GeneralString
 */
class DERGeneralString : ASN1Primitive, ASN1String {
    private val string: ByteArray

    internal constructor(string: ByteArray) {
        this.string = string
    }

    /**
     * Construct a GeneralString from the passed in String.

     * @param string the string to be contained in this object.
     */
    constructor(string: String) {
        this.string = Strings.toByteArray(string)
    }

    /**
     * Return a Java String representation of our contained String.

     * @return a Java String representing our contents.
     */
    override fun getString(): String {
        return Strings.fromByteArray(string)
    }

    override fun toString(): String {
        return getString()
    }

    /**
     * Return a byte array representation of our contained String.

     * @return a byte array representing our contents.
     */
    val octets: ByteArray
        get() = Arrays.clone(string)

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(string.size) + string.size
    }

    @Throws(IOException::class)
    internal override fun encode(out: ASN1OutputStream) {
        out.writeEncoded(BERTags.GENERAL_STRING, string)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(string)
    }

    internal override fun asn1Equals(o: ASN1Primitive): Boolean {
        if (o !is DERGeneralString) {
            return false
        }

        return Arrays.areEqual(string, o.string)
    }

    companion object {

        /**
         * return a GeneralString from the given object.

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERBMPString instance, or null.
         */
        fun getInstance(
                obj: Any?): DERGeneralString {
            if (obj == null || obj is DERGeneralString) {
                return obj as DERGeneralString?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERGeneralString
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return a GeneralString from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *              be converted.
         * *
         * @return a DERGeneralString instance.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERGeneralString {
            val o = obj.`object`

            if (explicit || o is DERGeneralString) {
                return getInstance(o)
            } else {
                return DERGeneralString((o as ASN1OctetString).octets)
            }
        }
    }
}
