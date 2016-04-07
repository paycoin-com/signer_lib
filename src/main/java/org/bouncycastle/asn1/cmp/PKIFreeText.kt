package org.bouncycastle.asn1.cmp

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERUTF8String

class PKIFreeText : ASN1Object {
    internal var strings: ASN1Sequence

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects
        while (e.hasMoreElements()) {
            if (e.nextElement() !is DERUTF8String) {
                throw IllegalArgumentException("attempt to insert non UTF8 STRING into PKIFreeText")
            }
        }

        strings = seq
    }

    constructor(
            p: DERUTF8String) {
        strings = DERSequence(p)
    }

    constructor(
            p: String) : this(DERUTF8String(p)) {
    }

    constructor(
            strs: Array<DERUTF8String>) {
        strings = DERSequence(strs)
    }

    constructor(
            strs: Array<String>) {
        val v = ASN1EncodableVector()
        for (i in strs.indices) {
            v.add(DERUTF8String(strs[i]))
        }
        strings = DERSequence(v)
    }

    /**
     * Return the number of string elements present.

     * @return number of elements present.
     */
    fun size(): Int {
        return strings.size()
    }

    /**
     * Return the UTF8STRING at index i.

     * @param i index of the string of interest
     * *
     * @return the string at index i.
     */
    fun getStringAt(
            i: Int): DERUTF8String {
        return strings.getObjectAt(i) as DERUTF8String
    }

    /**
     *
     * PKIFreeText ::= SEQUENCE SIZE (1..MAX) OF UTF8String
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return strings
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): PKIFreeText {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): PKIFreeText? {
            if (obj is PKIFreeText) {
                return obj
            } else if (obj != null) {
                return PKIFreeText(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
