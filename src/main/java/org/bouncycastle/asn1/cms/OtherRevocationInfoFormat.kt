package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-10.2.1): OtherRevocationInfoFormat object.
 *
 *
 *
 * OtherRevocationInfoFormat ::= SEQUENCE {
 * otherRevInfoFormat OBJECT IDENTIFIER,
 * otherRevInfo ANY DEFINED BY otherRevInfoFormat }
 *
 */
class OtherRevocationInfoFormat : ASN1Object {
    var infoFormat: ASN1ObjectIdentifier? = null
        private set
    var info: ASN1Encodable? = null
        private set

    constructor(
            otherRevInfoFormat: ASN1ObjectIdentifier,
            otherRevInfo: ASN1Encodable) {
        this.infoFormat = otherRevInfoFormat
        this.info = otherRevInfo
    }

    private constructor(
            seq: ASN1Sequence) {
        infoFormat = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
        info = seq.getObjectAt(1)
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(infoFormat)
        v.add(info)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a OtherRevocationInfoFormat object from a tagged object.

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
                explicit: Boolean): OtherRevocationInfoFormat {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return a OtherRevocationInfoFormat object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [OtherRevocationInfoFormat] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with OtherRevocationInfoFormat structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): OtherRevocationInfoFormat? {
            if (obj is OtherRevocationInfoFormat) {
                return obj
            }

            if (obj != null) {
                return OtherRevocationInfoFormat(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
