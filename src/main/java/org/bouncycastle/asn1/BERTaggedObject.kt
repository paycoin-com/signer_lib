package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration

/**
 * BER TaggedObject - in ASN.1 notation this is any object preceded by
 * a [n] where n is some number - these are assumed to follow the construction
 * rules (as with sequences).
 */
class BERTaggedObject : ASN1TaggedObject {
    /**
     * @param tagNo the tag number for this object.
     * *
     * @param obj the tagged object.
     */
    constructor(
            tagNo: Int,
            obj: ASN1Encodable) : super(true, tagNo, obj) {
    }

    /**
     * @param explicit true if an explicitly tagged object.
     * *
     * @param tagNo the tag number for this object.
     * *
     * @param obj the tagged object.
     */
    constructor(
            explicit: Boolean,
            tagNo: Int,
            obj: ASN1Encodable) : super(explicit, tagNo, obj) {
    }

    /**
     * create an implicitly tagged object that contains a zero
     * length sequence.
     */
    constructor(
            tagNo: Int) : super(false, tagNo, BERSequence()) {
    }

    internal override val isConstructed: Boolean
        get() {
            if (!isEmpty) {
                if (isExplicit) {
                    return true
                } else {
                    val primitive = obj!!.toASN1Primitive().toDERObject()

                    return primitive.isConstructed
                }
            } else {
                return true
            }
        }

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        if (!isEmpty) {
            val primitive = obj!!.toASN1Primitive()
            var length = primitive.encodedLength()

            if (isExplicit) {
                return StreamUtil.calculateTagLength(tagNo) + StreamUtil.calculateBodyLength(length) + length
            } else {
                // header length already in calculation
                length = length - 1

                return StreamUtil.calculateTagLength(tagNo) + length
            }
        } else {
            return StreamUtil.calculateTagLength(tagNo) + 1
        }
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.writeTag(BERTags.CONSTRUCTED or BERTags.TAGGED, tagNo)
        out.write(0x80)

        if (!isEmpty) {
            if (!isExplicit) {
                val e: Enumeration<Any>
                if (obj is ASN1OctetString) {
                    if (obj is BEROctetString) {
                        e = (obj as BEROctetString).objects
                    } else {
                        val octs = obj as ASN1OctetString?
                        val berO = BEROctetString(octs.octets)
                        e = berO.objects
                    }
                } else if (obj is ASN1Sequence) {
                    e = (obj as ASN1Sequence).objects
                } else if (obj is ASN1Set) {
                    e = (obj as ASN1Set).objects
                } else {
                    throw RuntimeException("not implemented: " + obj!!.javaClass.name)
                }

                while (e.hasMoreElements()) {
                    out.writeObject(e.nextElement() as ASN1Encodable)
                }
            } else {
                out.writeObject(obj)
            }
        }

        out.write(0x00)
        out.write(0x00)
    }
}
