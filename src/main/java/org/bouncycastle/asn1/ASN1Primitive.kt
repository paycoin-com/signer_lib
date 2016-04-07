package org.bouncycastle.asn1

import java.io.IOException

/**
 * Base class for ASN.1 primitive objects. These are the actual objects used to generate byte encodings.
 */
abstract class ASN1Primitive internal constructor() : ASN1Object() {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }

        return o is ASN1Encodable && asn1Equals(o.toASN1Primitive())
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return this
    }

    /**
     * Return the current object as one which encodes using Distinguished Encoding Rules.

     * @return a DER version of this.
     */
    internal open fun toDERObject(): ASN1Primitive {
        return this
    }

    /**
     * Return the current object as one which encodes using Definite Length encoding.

     * @return a DL version of this.
     */
    internal open fun toDLObject(): ASN1Primitive {
        return this
    }

    abstract override fun hashCode(): Int

    internal abstract val isConstructed: Boolean

    @Throws(IOException::class)
    internal abstract fun encodedLength(): Int

    @Throws(IOException::class)
    internal abstract fun encode(out: ASN1OutputStream)

    internal abstract fun asn1Equals(o: ASN1Primitive): Boolean

    companion object {

        /**
         * Create a base ASN.1 object from a byte stream.

         * @param data the byte stream to parse.
         * *
         * @return the base ASN.1 object represented by the byte stream.
         * *
         * @exception IOException if there is a problem parsing the data, or parsing the stream did not exhaust the available data.
         */
        @Throws(IOException::class)
        fun fromByteArray(data: ByteArray): ASN1Primitive {
            val aIn = ASN1InputStream(data)

            try {
                val o = aIn.readObject()

                if (aIn.available() != 0) {
                    throw IOException("Extra data detected in stream")
                }

                return o
            } catch (e: ClassCastException) {
                throw IOException("cannot recognise object in stream")
            }

        }
    }
}