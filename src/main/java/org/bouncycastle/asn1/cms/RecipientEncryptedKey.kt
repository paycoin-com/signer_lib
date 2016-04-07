package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.2):
 * Content encryption key delivery mechanisms.
 *
 * RecipientEncryptedKey ::= SEQUENCE {
 * rid KeyAgreeRecipientIdentifier,
 * encryptedKey EncryptedKey
 * }
 *
 */
class RecipientEncryptedKey : ASN1Object {
    var identifier: KeyAgreeRecipientIdentifier? = null
        private set
    var encryptedKey: ASN1OctetString? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        identifier = KeyAgreeRecipientIdentifier.getInstance(seq.getObjectAt(0))
        encryptedKey = seq.getObjectAt(1) as ASN1OctetString
    }

    constructor(
            id: KeyAgreeRecipientIdentifier,
            encryptedKey: ASN1OctetString) {
        this.identifier = id
        this.encryptedKey = encryptedKey
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(identifier)
        v.add(encryptedKey)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an RecipientEncryptedKey object from a tagged object.

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
                explicit: Boolean): RecipientEncryptedKey {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return a RecipientEncryptedKey object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [RecipientEncryptedKey] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with RecipientEncryptedKey structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): RecipientEncryptedKey? {
            if (obj is RecipientEncryptedKey) {
                return obj
            }

            if (obj != null) {
                return RecipientEncryptedKey(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
