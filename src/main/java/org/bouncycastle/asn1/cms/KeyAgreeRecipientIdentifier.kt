package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.2):
 * Content encryption key delivery mechanisms.
 *
 *
 *
 * KeyAgreeRecipientIdentifier ::= CHOICE {
 * issuerAndSerialNumber IssuerAndSerialNumber,
 * rKeyId [0] IMPLICIT RecipientKeyIdentifier }
 *
 */
class KeyAgreeRecipientIdentifier : ASN1Object, ASN1Choice {
    var issuerAndSerialNumber: IssuerAndSerialNumber? = null
        private set
    var rKeyID: RecipientKeyIdentifier? = null
        private set

    constructor(
            issuerSerial: IssuerAndSerialNumber) {
        this.issuerAndSerialNumber = issuerSerial
        this.rKeyID = null
    }

    constructor(
            rKeyID: RecipientKeyIdentifier) {
        this.issuerAndSerialNumber = null
        this.rKeyID = rKeyID
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (issuerAndSerialNumber != null) {
            return issuerAndSerialNumber!!.toASN1Primitive()
        }

        return DERTaggedObject(false, 0, rKeyID)
    }

    companion object {

        /**
         * Return an KeyAgreeRecipientIdentifier object from a tagged object.

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
                explicit: Boolean): KeyAgreeRecipientIdentifier {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return an KeyAgreeRecipientIdentifier object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  [KeyAgreeRecipientIdentifier] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with IssuerAndSerialNumber structure inside
         *  *  [ASN1TaggedObject][org.bouncycastle.asn1.ASN1TaggedObject.getInstance] with tag value 0: a KeyAgreeRecipientIdentifier data structure
         *
         *
         *
         * Note: no byte[] input!

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): KeyAgreeRecipientIdentifier {
            if (obj == null || obj is KeyAgreeRecipientIdentifier) {
                return obj as KeyAgreeRecipientIdentifier?
            }

            if (obj is ASN1Sequence) {
                return KeyAgreeRecipientIdentifier(IssuerAndSerialNumber.getInstance(obj))
            }

            if (obj is ASN1TaggedObject && obj.tagNo == 0) {
                return KeyAgreeRecipientIdentifier(RecipientKeyIdentifier.getInstance(
                        obj as ASN1TaggedObject?, false))
            }

            throw IllegalArgumentException("Invalid KeyAgreeRecipientIdentifier: " + obj.javaClass.name)
        }
    }
}
