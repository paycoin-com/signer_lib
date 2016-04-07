package org.bouncycastle.asn1

import java.io.IOException
import java.io.OutputStream

/**
 * A stream generator for DER SEQUENCEs
 */
class BERSequenceGenerator : BERGenerator {
    /**
     * Use the passed in stream as the target for the generator, writing out the header tag
     * for a constructed SEQUENCE.

     * @param out target stream
     * *
     * @throws IOException if the target stream cannot be written to.
     */
    @Throws(IOException::class)
    constructor(
            out: OutputStream) : super(out) {

        writeBERHeader(BERTags.CONSTRUCTED or BERTags.SEQUENCE)
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

        writeBERHeader(BERTags.CONSTRUCTED or BERTags.SEQUENCE)
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
        `object`.toASN1Primitive().encode(BEROutputStream(_out))
    }

    /**
     * Close of the generator, writing out the BER end tag.

     * @throws IOException if the target stream cannot be written.
     */
    @Throws(IOException::class)
    fun close() {
        writeBEREnd()
    }
}