package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration

/**
 * A DER encoded set object
 */
class DERSet : ASN1Set {
    private var bodyLength = -1

    /**
     * create an empty set
     */
    constructor() {
    }

    /**
     * create a set containing one object
     * @param obj the object to go in the set
     */
    constructor(
            obj: ASN1Encodable) : super(obj) {
    }

    /**
     * create a set containing a vector of objects.
     * @param v the vector of objects to make up the set.
     */
    constructor(
            v: ASN1EncodableVector) : super(v, true) {
    }

    /**
     * create a set containing an array of objects.
     * @param a the array of objects to make up the set.
     */
    constructor(
            a: Array<ASN1Encodable>) : super(a, true) {
    }

    internal constructor(
            v: ASN1EncodableVector,
            doSort: Boolean) : super(v, doSort) {
    }

    @Throws(IOException::class)
    private fun getBodyLength(): Int {
        if (bodyLength < 0) {
            var length = 0

            val e = this.objects
            while (e.hasMoreElements()) {
                val obj = e.nextElement()

                length += (obj as ASN1Encodable).toASN1Primitive().toDERObject().encodedLength()
            }

            bodyLength = length
        }

        return bodyLength
    }

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        val length = getBodyLength()

        return 1 + StreamUtil.calculateBodyLength(length) + length
    }

    /*
     * A note on the implementation:
     * <p>
     * As DER requires the constructed, definite-length model to
     * be used for structured types, this varies slightly from the
     * ASN.1 descriptions given. Rather than just outputting SET,
     * we also have to specify CONSTRUCTED, and the objects length.
     */
    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        val dOut = out.derSubStream
        val length = getBodyLength()

        out.write(BERTags.SET or BERTags.CONSTRUCTED)
        out.writeLength(length)

        val e = this.objects
        while (e.hasMoreElements()) {
            val obj = e.nextElement()

            dOut.writeObject(obj as ASN1Encodable)
        }
    }
}
