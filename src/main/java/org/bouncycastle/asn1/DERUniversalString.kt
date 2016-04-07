package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException

import org.bouncycastle.util.Arrays

/**
 * DER UniversalString object.
 */
class DERUniversalString
/**
 * basic constructor - byte encoded string.

 * @param string the byte encoding of the string to be carried in the UniversalString object,
 */
(
        val octets: ByteArray) : ASN1Primitive(), ASN1String {

    override val string: String
        get() {
            val buf = StringBuffer("#")
            val bOut = ByteArrayOutputStream()
            val aOut = ASN1OutputStream(bOut)

            try {
                aOut.writeObject(this)
            } catch (e: IOException) {
                throw RuntimeException("internal error encoding BitString")
            }

            val string = bOut.toByteArray()

            for (i in string.indices) {
                buf.append(table[string[i].ushr(4) and 0xf])
                buf.append(table[string[i] and 0xf])
            }

            return buf.toString()
        }

    override fun toString(): String {
        return string
    }

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        return 1 + StreamUtil.calculateBodyLength(octets.size) + octets.size
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeEncoded(BERTags.UNIVERSAL_STRING, this.octets)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is DERUniversalString) {
            return false
        }

        return Arrays.areEqual(octets, o.octets)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(octets)
    }

    companion object {
        private val table = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

        /**
         * return a Universal String from the passed in object.

         * @param obj a DERUniversalString or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return a DERUniversalString instance, or null
         */
        fun getInstance(
                obj: Any?): DERUniversalString {
            if (obj == null || obj is DERUniversalString) {
                return obj as DERUniversalString?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as DERUniversalString
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return a Universal String from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return a DERUniversalString instance, or null
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DERUniversalString {
            val o = obj.`object`

            if (explicit || o is DERUniversalString) {
                return getInstance(o)
            } else {
                return DERUniversalString((o as ASN1OctetString).octets)
            }
        }
    }
}
