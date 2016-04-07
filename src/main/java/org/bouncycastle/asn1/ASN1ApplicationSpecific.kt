package org.bouncycastle.asn1

import java.io.IOException

import org.bouncycastle.util.Arrays

/**
 * Base class for an application specific object
 */
abstract class ASN1ApplicationSpecific internal constructor(
        /**
         * Return true if the object is marked as constructed, false otherwise.

         * @return true if constructed, otherwise false.
         */
        override val isConstructed: Boolean,
        /**
         * Return the tag number associated with this object,

         * @return the application tag number.
         */
        val applicationTag: Int,
        /**
         * Return the contents of this object as a byte[]

         * @return the encoded contents of the object.
         */
        val contents: ByteArray) : ASN1Primitive() {

    /**
     * Return the enclosed object assuming explicit tagging.

     * @return  the resulting object
     * *
     * @throws IOException if reconstruction fails.
     */
    val `object`: ASN1Primitive
        @Throws(IOException::class)
        get() = ASN1InputStream(contents).readObject()

    /**
     * Return the enclosed object assuming implicit tagging.

     * @param derTagNo the type tag that should be applied to the object's contents.
     * *
     * @return  the resulting object
     * *
     * @throws IOException if reconstruction fails.
     */
    @Throws(IOException::class)
    fun getObject(derTagNo: Int): ASN1Primitive {
        if (derTagNo >= 0x1f) {
            throw IOException("unsupported tag number")
        }

        val orig = this.encoded
        val tmp = replaceTagNumber(derTagNo, orig)

        if (orig[0] and BERTags.CONSTRUCTED != 0) {
            tmp[0] = tmp[0] or BERTags.CONSTRUCTED.toByte()
        }

        return ASN1InputStream(tmp).readObject()
    }

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        return StreamUtil.calculateTagLength(applicationTag) + StreamUtil.calculateBodyLength(contents.size) + contents.size
    }

    /* (non-Javadoc)
     * @see org.bouncycastle.asn1.ASN1Primitive#encode(org.bouncycastle.asn1.DEROutputStream)
     */
    @Throws(IOException::class)
    internal override fun encode(out: ASN1OutputStream) {
        var classBits = BERTags.APPLICATION
        if (isConstructed) {
            classBits = classBits or BERTags.CONSTRUCTED
        }

        out.writeEncoded(classBits, applicationTag, contents)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1ApplicationSpecific) {
            return false
        }

        return isConstructed == o.isConstructed
                && applicationTag == o.applicationTag
                && Arrays.areEqual(contents, o.contents)
    }

    override fun hashCode(): Int {
        return (if (isConstructed) 1 else 0) xor applicationTag xor Arrays.hashCode(contents)
    }

    @Throws(IOException::class)
    private fun replaceTagNumber(newTag: Int, input: ByteArray): ByteArray {
        var tagNo = input[0] and 0x1f
        var index = 1
        //
        // with tagged object tag number is bottom 5 bits, or stored at the start of the content
        //
        if (tagNo == 0x1f) {
            tagNo = 0

            var b = input[index++] and 0xff

            // X.690-0207 8.1.2.4.2
            // "c) bits 7 to 1 of the first subsequent octet shall not all be zero."
            if (b and 0x7f == 0)
            // Note: -1 will pass
            {
                throw ASN1ParsingException("corrupted stream - invalid high tag number found")
            }

            while (b >= 0 && b and 0x80 != 0) {
                tagNo = tagNo or (b and 0x7f)
                tagNo = tagNo shl 7
                b = input[index++] and 0xff
            }

            //            tagNo |= (b & 0x7f);
        }

        val tmp = ByteArray(input.size - index + 1)

        System.arraycopy(input, index, tmp, 1, tmp.size - 1)

        tmp[0] = newTag.toByte()

        return tmp
    }

    companion object {

        /**
         * Return an ASN1ApplicationSpecific from the passed in object, which may be a byte array, or null.

         * @param obj the object to be converted.
         * *
         * @return obj's representation as an ASN1ApplicationSpecific object.
         */
        fun getInstance(obj: Any?): ASN1ApplicationSpecific {
            if (obj == null || obj is ASN1ApplicationSpecific) {
                return obj as ASN1ApplicationSpecific?
            } else if (obj is ByteArray) {
                try {
                    return ASN1ApplicationSpecific.getInstance(ASN1Primitive.fromByteArray(obj as ByteArray?))
                } catch (e: IOException) {
                    throw IllegalArgumentException("Failed to construct object from byte[]: " + e.message)
                }

            }

            throw IllegalArgumentException("unknown object in getInstance: " + obj.javaClass.name)
        }

        protected fun getLengthOfHeader(data: ByteArray): Int {
            val length = data[1] and 0xff // TODO: assumes 1 byte tag

            if (length == 0x80) {
                return 2      // indefinite-length encoding
            }

            if (length > 127) {
                val size = length and 0x7f

                // Note: The invalid long form "0xff" (see X.690 8.1.3.5c) will be caught here
                if (size > 4) {
                    throw IllegalStateException("DER length more than 4 bytes: " + size)
                }

                return size + 2
            }

            return 2
        }
    }
}
