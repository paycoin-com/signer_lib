package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration

/**
 * Carrier class for an indefinite-length SET.
 */
class BERSet : ASN1Set {
    /**
     * Create an empty SET.
     */
    constructor() {
    }

    /**
     * Create a SET containing one object.

     * @param obj - a single object that makes up the set.
     */
    constructor(
            obj: ASN1Encodable) : super(obj) {
    }

    /**
     * Create a SET containing multiple objects.
     * @param v a vector of objects making up the set.
     */
    constructor(
            v: ASN1EncodableVector) : super(v, false) {
    }

    /**
     * Create a SET from an array of objects.
     * @param a an array of ASN.1 objects.
     */
    constructor(
            a: Array<ASN1Encodable>) : super(a, false) {
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
        out.write(BERTags.SET or BERTags.CONSTRUCTED)
        out.write(0x80)

        val e = objects
        while (e.hasMoreElements()) {
            out.writeObject(e.nextElement() as ASN1Encodable)
        }

        out.write(0x00)
        out.write(0x00)
    }
}