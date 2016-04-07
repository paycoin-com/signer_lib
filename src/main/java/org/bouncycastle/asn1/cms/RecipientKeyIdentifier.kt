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
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.2):
 * Content encryption key delivery mechanisms.
 *
 *
 *
 * RecipientKeyIdentifier ::= SEQUENCE {
 * subjectKeyIdentifier SubjectKeyIdentifier,
 * date GeneralizedTime OPTIONAL,
 * other OtherKeyAttribute OPTIONAL
 * }

 * SubjectKeyIdentifier ::= OCTET STRING
 *
 */
class RecipientKeyIdentifier : ASN1Object {
    var subjectKeyIdentifier: ASN1OctetString? = null
        private set
    var date: ASN1GeneralizedTime? = null
        private set
    var otherKeyAttribute: OtherKeyAttribute? = null
        private set

    constructor(
            subjectKeyIdentifier: ASN1OctetString,
            date: ASN1GeneralizedTime,
            other: OtherKeyAttribute) {
        this.subjectKeyIdentifier = subjectKeyIdentifier
        this.date = date
        this.otherKeyAttribute = other
    }

    @JvmOverloads constructor(
            subjectKeyIdentifier: ByteArray,
            date: ASN1GeneralizedTime? = null,
            other: OtherKeyAttribute? = null) {
        this.subjectKeyIdentifier = DEROctetString(subjectKeyIdentifier)
        this.date = date
        this.otherKeyAttribute = other
    }


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        subjectKeyIdentifier = ASN1OctetString.getInstance(
                seq.getObjectAt(0))

        when (seq.size()) {
            1 -> {
            }
            2 -> if (seq.getObjectAt(1) is ASN1GeneralizedTime) {
                date = ASN1GeneralizedTime.getInstance(seq.getObjectAt(1))
            } else {
                otherKeyAttribute = OtherKeyAttribute.getInstance(seq.getObjectAt(2))
            }
            3 -> {
                date = ASN1GeneralizedTime.getInstance(seq.getObjectAt(1))
                otherKeyAttribute = OtherKeyAttribute.getInstance(seq.getObjectAt(2))
            }
            else -> throw IllegalArgumentException("Invalid RecipientKeyIdentifier")
        }
    }


    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(subjectKeyIdentifier)

        if (date != null) {
            v.add(date)
        }

        if (otherKeyAttribute != null) {
            v.add(otherKeyAttribute)
        }

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a RecipientKeyIdentifier object from a tagged object.

         * @param ato the tagged object holding the object we want.
         * *
         * @param isExplicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the object held by the
         * *          tagged object cannot be converted.
         */
        fun getInstance(ato: ASN1TaggedObject, isExplicit: Boolean): RecipientKeyIdentifier {
            return getInstance(ASN1Sequence.getInstance(ato, isExplicit))
        }

        /**
         * Return a RecipientKeyIdentifier object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [RecipientKeyIdentifier] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with RecipientKeyIdentifier structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(obj: Any?): RecipientKeyIdentifier? {
            if (obj is RecipientKeyIdentifier) {
                return obj
            }

            if (obj != null) {
                return RecipientKeyIdentifier(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
