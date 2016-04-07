package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException

import org.bouncycastle.util.Encodable

/**
 * Base class for defining an ASN.1 object.
 */
abstract class ASN1Object : ASN1Encodable, Encodable {
    /**
     * Return the default BER or DER encoding for this object.

     * @return BER/DER byte encoded object.
     * *
     * @throws java.io.IOException on encoding error.
     */
    @Throws(IOException::class)
    override fun getEncoded(): ByteArray {
        val bOut = ByteArrayOutputStream()
        val aOut = ASN1OutputStream(bOut)

        aOut.writeObject(this)

        return bOut.toByteArray()
    }

    /**
     * Return either the default for "BER" or a DER encoding if "DER" is specified.

     * @param encoding name of encoding to use.
     * *
     * @return byte encoded object.
     * *
     * @throws IOException on encoding error.
     */
    @Throws(IOException::class)
    fun getEncoded(
            encoding: String): ByteArray {
        if (encoding == ASN1Encoding.DER) {
            val bOut = ByteArrayOutputStream()
            val dOut = DEROutputStream(bOut)

            dOut.writeObject(this)

            return bOut.toByteArray()
        } else if (encoding == ASN1Encoding.DL) {
            val bOut = ByteArrayOutputStream()
            val dOut = DLOutputStream(bOut)

            dOut.writeObject(this)

            return bOut.toByteArray()
        }

        return this.encoded
    }

    override fun hashCode(): Int {
        return this.toASN1Primitive().hashCode()
    }

    override fun equals(
            o: Any?): Boolean {
        if (this === o) {
            return true
        }

        if (o !is ASN1Encodable) {
            return false
        }

        return this.toASN1Primitive() == o.toASN1Primitive()
    }

    /**
     * @return the underlying primitive type.
     */
    @Deprecated("use toASN1Primitive()\n      ")
    fun toASN1Object(): ASN1Primitive {
        return this.toASN1Primitive()
    }

    /**
     * Method providing a primitive representation of this object suitable for encoding.
     * @return a primitive representation of this object.
     */
    abstract override fun toASN1Primitive(): ASN1Primitive

    companion object {

        /**
         * Return true if obj is a byte array and represents an object with the given tag value.

         * @param obj object of interest.
         * *
         * @param tagValue tag value to check for.
         * *
         * @return  true if obj is a byte encoding starting with the given tag value, false otherwise.
         */
        protected fun hasEncodedTagValue(obj: Any, tagValue: Int): Boolean {
            return obj is ByteArray && obj[0].toInt() == tagValue
        }
    }
}
