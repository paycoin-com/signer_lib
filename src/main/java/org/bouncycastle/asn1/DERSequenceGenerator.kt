package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * A stream generator for DER SEQUENCEs
 */
class DERSequenceGenerator : DERGenerator {
    private val _bOut = ByteArrayOutputStream()

    /**
     * Use the passed in stream as the target for the generator.

     * @param out target stream
     * *
     * @throws IOException if the target stream cannot be written to.
     */
    @Throws(IOException::class)
    constructor(
            out: OutputStream) : super(out) {
    }

    /**
     * Use the passed in stream as the target for the generator, writing out the header tag
     * for a tagged constructed SEQUENCE (possibly implicit).

     * @param out target stream
     * *
     * @param tagNo the tag number to introduce
     * *
     * @param isExplicit true if this is an explicitly tagged object, false otherwise.
     * *
     * @throws IOException if the target stream cannot be written to.
     */
    @Throws(IOException::class)
    constructor(
            out: OutputStream,
            tagNo: Int,
            isExplicit: Boolean) : super(out, tagNo, isExplicit) {
    }

    /**
     * Add an object to the SEQUENCE being generated.

     * @param object an ASN.1 encodable object to add.
     * *
     * @throws IOException if the target stream cannot be written to or the object cannot be encoded.
     */
    @Throws(IOException::class)
    fun addObject(
            `object`: ASN1Encodable) {
        `object`.toASN1Primitive().encode(DEROutputStream(_bOut))
    }

    /**
     * Return the target stream for the SEQUENCE.

     * @return  the OutputStream the SEQUENCE is being written to.
     */
    override val rawOutputStream: OutputStream
        get() = _bOut

    /**
     * Close of the generator, writing out the SEQUENCE.

     * @throws IOException if the target stream cannot be written.
     */
    @Throws(IOException::class)
    fun close() {
        writeDEREncoded(BERTags.CONSTRUCTED or BERTags.SEQUENCE, _bOut.toByteArray())
    }
}
