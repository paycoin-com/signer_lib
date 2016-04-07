package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.2):
 * Content encryption key delivery mechanisms.
 *
 *
 *
 * KeyAgreeRecipientInfo ::= SEQUENCE {
 * version CMSVersion,  -- always set to 3
 * originator [0] EXPLICIT OriginatorIdentifierOrKey,
 * ukm [1] EXPLICIT UserKeyingMaterial OPTIONAL,
 * keyEncryptionAlgorithm KeyEncryptionAlgorithmIdentifier,
 * recipientEncryptedKeys RecipientEncryptedKeys
 * }

 * UserKeyingMaterial ::= OCTET STRING
 *
 */
class KeyAgreeRecipientInfo : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var originator: OriginatorIdentifierOrKey? = null
        private set
    var userKeyingMaterial: ASN1OctetString? = null
        private set
    var keyEncryptionAlgorithm: AlgorithmIdentifier? = null
        private set
    var recipientEncryptedKeys: ASN1Sequence? = null
        private set

    constructor(
            originator: OriginatorIdentifierOrKey,
            ukm: ASN1OctetString,
            keyEncryptionAlgorithm: AlgorithmIdentifier,
            recipientEncryptedKeys: ASN1Sequence) {
        this.version = ASN1Integer(3)
        this.originator = originator
        this.userKeyingMaterial = ukm
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm
        this.recipientEncryptedKeys = recipientEncryptedKeys
    }


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        var index = 0

        version = seq.getObjectAt(index++) as ASN1Integer
        originator = OriginatorIdentifierOrKey.getInstance(
                seq.getObjectAt(index++) as ASN1TaggedObject, true)

        if (seq.getObjectAt(index) is ASN1TaggedObject) {
            userKeyingMaterial = ASN1OctetString.getInstance(
                    seq.getObjectAt(index++) as ASN1TaggedObject, true)
        }

        keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(
                seq.getObjectAt(index++))

        recipientEncryptedKeys = seq.getObjectAt(index++) as ASN1Sequence
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(DERTaggedObject(true, 0, originator))

        if (userKeyingMaterial != null) {
            v.add(DERTaggedObject(true, 1, userKeyingMaterial))
        }

        v.add(keyEncryptionAlgorithm)
        v.add(recipientEncryptedKeys)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a KeyAgreeRecipientInfo object from a tagged object.

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
                explicit: Boolean): KeyAgreeRecipientInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return a KeyAgreeRecipientInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [KeyAgreeRecipientInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with KeyAgreeRecipientInfo structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): KeyAgreeRecipientInfo? {
            if (obj is KeyAgreeRecipientInfo) {
                return obj
            }

            if (obj != null) {
                return KeyAgreeRecipientInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
