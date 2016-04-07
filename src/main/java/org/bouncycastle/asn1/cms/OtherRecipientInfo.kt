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
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.5):
 * Content encryption key delivery mechanisms.
 *
 * OtherRecipientInfo ::= SEQUENCE {
 * oriType OBJECT IDENTIFIER,
 * oriValue ANY DEFINED BY oriType }
 *
 */
class OtherRecipientInfo : ASN1Object {
    var type: ASN1ObjectIdentifier? = null
        private set
    var value: ASN1Encodable? = null
        private set

    constructor(
            oriType: ASN1ObjectIdentifier,
            oriValue: ASN1Encodable) {
        this.type = oriType
        this.value = oriValue
    }


    @Deprecated("use getInstance().")
    constructor(
            seq: ASN1Sequence) {
        type = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
        value = seq.getObjectAt(1)
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(type)
        v.add(value)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a OtherRecipientInfo object from a tagged object.

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
                explicit: Boolean): OtherRecipientInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return a OtherRecipientInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [PasswordRecipientInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with OtherRecipientInfo structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): OtherRecipientInfo? {
            if (obj is OtherRecipientInfo) {
                return obj
            }

            if (obj != null) {
                return OtherRecipientInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
