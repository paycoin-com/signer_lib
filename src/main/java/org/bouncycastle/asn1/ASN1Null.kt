package org.bouncycastle.asn1

import java.io.IOException

/**
 * A NULL object - use DERNull.INSTANCE for populating structures.
 */
abstract class ASN1Null : ASN1Primitive() {

    override fun hashCode(): Int {
        return -1
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1Null) {
            return false
        }

        return true
    }

    @Throws(IOException::class)
    internal abstract override fun encode(out: ASN1OutputStream)

    override fun toString(): String {
        return "NULL"
    }

    companion object {
        /**
         * Return an instance of ASN.1 NULL from the passed in object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [ASN1Null] object
         *  *  a byte[] containing ASN.1 NULL object
         *
         *

         * @param o object to be converted.
         * *
         * @return an instance of ASN1Null, or null.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(o: Any?): ASN1Null? {
            if (o is ASN1Null) {
                return o
            }

            if (o != null) {
                try {
                    return ASN1Null.getInstance(ASN1Primitive.fromByteArray(o as ByteArray?))
                } catch (e: IOException) {
                    throw IllegalArgumentException("failed to construct NULL from byte[]: " + e.message)
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException("unknown object in getInstance(): " + o.javaClass.name)
                }

            }

            return null
        }
    }
}
