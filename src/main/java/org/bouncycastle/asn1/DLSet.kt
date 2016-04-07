package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration

/**
 * The DLSet encodes ASN.1 SET value without element ordering,
 * and always using definite length form.
 *
 * X.690
 * 8: Basic encoding rules
 * 8.11 Encoding of a set value
 * **8.11.1** The encoding of a set value shall be constructed
 *
 *
 * **8.11.2** The contents octets shall consist of the complete
 * encoding of a data value from each of the types listed in the
 * ASN.1 definition of the set type, in an order chosen by the sender,
 * unless the type was referenced with the keyword
 * **OPTIONAL** or the keyword **DEFAULT**.
 *
 *
 * **8.11.3** The encoding of a data value may, but need not,
 * be present for a type which was referenced with the keyword
 * **OPTIONAL** or the keyword **DEFAULT**.
 *
 * NOTE  The order of data values in a set value is not significant,
 * and places no constraints on the order during transfer
 *
 * 9: Canonical encoding rules
 * 9.3 Set components
 * The encodings of the component values of a set value shall
 * appear in an order determined by their tags as specified
 * in 8.6 of ITU-T Rec. X.680 | ISO/IEC 8824-1.
 * Additionally, for the purposes of determining the order in which
 * components are encoded when one or more component is an untagged
 * choice type, each untagged choice type is ordered as though it
 * has a tag equal to that of the smallest tag in that choice type
 * or any untagged choice types nested within.
 * 10: Distinguished encoding rules
 * 10.3 Set components
 * The encodings of the component values of a set value shall appear
 * in an order determined by their tags as specified
 * in 8.6 of ITU-T Rec. X.680 | ISO/IEC 8824-1.
 *
 * NOTE  Where a component of the set is an untagged choice type,
 * the location of that component in the ordering will depend on
 * the tag of the choice component being encoded.
 *
 * 11: Restrictions on BER employed by both CER and DER
 * 11.5 Set and sequence components with default value
 * The encoding of a set value or sequence value shall not include
 * an encoding for any component value which is equal to
 * its default value.
 */
class DLSet : ASN1Set {
    private var bodyLength = -1

    /**
     * create an empty set
     */
    constructor() {
    }

    /**
     * @param obj - a single object that makes up the set.
     */
    constructor(
            obj: ASN1Encodable) : super(obj) {
    }

    /**
     * @param v - a vector of objects making up the set.
     */
    constructor(
            v: ASN1EncodableVector) : super(v, false) {
    }

    /**
     * create a set from an array of objects.
     */
    constructor(
            a: Array<ASN1Encodable>) : super(a, false) {
    }

    @Throws(IOException::class)
    private fun getBodyLength(): Int {
        if (bodyLength < 0) {
            var length = 0

            val e = this.objects
            while (e.hasMoreElements()) {
                val obj = e.nextElement()

                length += (obj as ASN1Encodable).toASN1Primitive().toDLObject().encodedLength()
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

    /**
     * A note on the implementation:
     *
     *
     * As DL requires the constructed, definite-length model to
     * be used for structured types, this varies slightly from the
     * ASN.1 descriptions given. Rather than just outputting SET,
     * we also have to specify CONSTRUCTED, and the objects length.
     */
    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        val dOut = out.dlSubStream
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