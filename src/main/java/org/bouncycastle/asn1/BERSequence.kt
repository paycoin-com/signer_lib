package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration

/**
 * Carrier class for an indefinite-length SEQUENCE.
 */
class BERSequence : ASN1Sequence {
    /**
     * Create an empty sequence
     */
    constructor() {
    }

    /**
     * Create a sequence containing one object
     */
    constructor(
            obj: ASN1Encodable) : super(obj) {
    }

    /**
     * Create a sequence containing a vector of objects.
     */
    constructor(
            v: ASN1EncodableVector) : super(v) {
    }

    /**
     * Create a sequence containing an array of objects.
     */
    constructor(
            array: Array<ASN1Encodable>) : super(array) {
    }

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        var length = 0
        val e = objects
        while (e.hasMoreElements()) {
            length += (e.nextElement() as ASN1Encodable).toASN1Primitive().encodedLength()
        }

        return 2 + length + 2
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.write(BERTags.SEQUENCE or BERTags.CONSTRUCTED)
        out.write(0x80)

        val e = objects
        while (e.hasMoreElements()) {
            out.writeObject(e.nextElement() as ASN1Encodable)
        }

        out.write(0x00)
        out.write(0x00)
    }
}
