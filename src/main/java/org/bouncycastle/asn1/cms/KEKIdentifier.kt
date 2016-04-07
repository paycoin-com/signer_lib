package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.3):
 * Content encryption key delivery mechanisms.
 *
 *
 *
 * KEKIdentifier ::= SEQUENCE {
 * keyIdentifier OCTET STRING,
 * date GeneralizedTime OPTIONAL,
 * other OtherKeyAttribute OPTIONAL
 * }
 *
 */
class KEKIdentifier : ASN1Object {
    var keyIdentifier: ASN1OctetString? = null
        private set
    var date: ASN1GeneralizedTime? = null
        private set
    var other: OtherKeyAttribute? = null
        private set

    constructor(
            keyIdentifier: ByteArray,
            date: ASN1GeneralizedTime,
            other: OtherKeyAttribute) {
        this.keyIdentifier = DEROctetString(keyIdentifier)
        this.date = date
        this.other = other
    }

    private constructor(
            seq: ASN1Sequence) {
        keyIdentifier = seq.getObjectAt(0) as ASN1OctetString

        when (seq.size()) {
            1 -> {
            }
            2 -> if (seq.getObjectAt(1) is ASN1GeneralizedTime) {
                date = seq.getObjectAt(1) as ASN1GeneralizedTime
            } else {
                other = OtherKeyAttribute.getInstance(seq.getObjectAt(1))
            }
            3 -> {
                date = seq.getObjectAt(1) as ASN1GeneralizedTime
                other = OtherKeyAttribute.getInstance(seq.getObjectAt(2))
            }
            else -> throw IllegalArgumentException("Invalid KEKIdentifier")
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(keyIdentifier)

        if (date != null) {
            v.add(date)
        }

        if (other != null) {
            v.add(other)
        }

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a KEKIdentifier object from a tagged object.

         * @param obj the tagged object holding the object we want.
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the object held by the
         * *          tagged object cannot be converted.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): KEKIdentifier {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return a KEKIdentifier object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [KEKIdentifier] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with KEKIdentifier structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): KEKIdentifier {
            if (obj == null || obj is KEKIdentifier) {
                return obj as KEKIdentifier?
            }

            if (obj is ASN1Sequence) {
                return KEKIdentifier(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("Invalid KEKIdentifier: " + obj.javaClass.name)
        }
    }
}
