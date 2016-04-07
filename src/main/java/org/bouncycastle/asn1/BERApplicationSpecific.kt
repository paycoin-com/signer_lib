package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * An indefinite-length encoding version of an application specific object.
 */
class BERApplicationSpecific : ASN1ApplicationSpecific {
    internal constructor(
            isConstructed: Boolean,
            tag: Int,
            octets: ByteArray) : super(isConstructed, tag, octets) {
    }

    /**
     * Create an application specific object with a tagging of explicit/constructed.

     * @param tag the tag number for this object.
     * *
     * @param object the object to be contained.
     */
    @Throws(IOException::class)
    constructor(
            tag: Int,
            `object`: ASN1Encodable) : this(true, tag, `object`) {
    }

    /**
     * Create an application specific object with the tagging style given by the value of constructed.

     * @param constructed true if the object is constructed.
     * *
     * @param tag the tag number for this object.
     * *
     * @param object the object to be contained.
     */
    @Throws(IOException::class)
    constructor(
            constructed: Boolean,
            tag: Int,
            `object`: ASN1Encodable) : super(constructed || `object`.toASN1Primitive().isConstructed, tag, getEncoding(constructed, `object`)) {
    }

    @Throws(IOException::class)
    private fun getEncoding(explicit: Boolean, `object`: ASN1Encodable): ByteArray {
        val data = `object`.toASN1Primitive().getEncoded(ASN1Encoding.BER)

        if (explicit) {
            return data
        } else {
            val lenBytes = ASN1ApplicationSpecific.getLengthOfHeader(data)
            val tmp = ByteArray(data.size - lenBytes)
            System.arraycopy(data, lenBytes, tmp, 0, tmp.size)
            return tmp
        }
    }

    /**
     * Create an application specific object which is marked as constructed

     * @param tagNo the tag number for this object.
     * *
     * @param vec the objects making up the application specific object.
     */
    constructor(tagNo: Int, vec: ASN1EncodableVector) : super(true, tagNo, getEncodedVector(vec)) {
    }

    private fun getEncodedVector(vec: ASN1EncodableVector): ByteArray {
        val bOut = ByteArrayOutputStream()

        for (i in 0..vec.size() - 1) {
            try {
                bOut.write((vec.get(i) as ASN1Object).getEncoded(ASN1Encoding.BER))
            } catch (e: IOException) {
                throw ASN1ParsingException("malformed object: " + e, e)
            }

        }
        return bOut.toByteArray()
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

        out.writeTag(classBits, applicationTag)
        out.write(0x80)
        out.write(contents)
        out.write(0x00)
        out.write(0x00)
    }
}
