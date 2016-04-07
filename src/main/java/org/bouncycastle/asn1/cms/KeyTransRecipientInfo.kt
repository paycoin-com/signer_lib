package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.1):
 * Content encryption key delivery mechanisms.
 *
 * KeyTransRecipientInfo ::= SEQUENCE {
 * version CMSVersion,  -- always set to 0 or 2
 * rid RecipientIdentifier,
 * keyEncryptionAlgorithm KeyEncryptionAlgorithmIdentifier,
 * encryptedKey EncryptedKey
 * }
 *
 */
class KeyTransRecipientInfo : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var recipientIdentifier: RecipientIdentifier? = null
        private set
    var keyEncryptionAlgorithm: AlgorithmIdentifier? = null
        private set
    var encryptedKey: ASN1OctetString? = null
        private set

    constructor(
            rid: RecipientIdentifier,
            keyEncryptionAlgorithm: AlgorithmIdentifier,
            encryptedKey: ASN1OctetString) {
        if (rid.toASN1Primitive() is ASN1TaggedObject) {
            this.version = ASN1Integer(2)
        } else {
            this.version = ASN1Integer(0)
        }

        this.recipientIdentifier = rid
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm
        this.encryptedKey = encryptedKey
    }


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        this.version = seq.getObjectAt(0) as ASN1Integer
        this.recipientIdentifier = RecipientIdentifier.getInstance(seq.getObjectAt(1))
        this.keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(2))
        this.encryptedKey = seq.getObjectAt(3) as ASN1OctetString
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(recipientIdentifier)
        v.add(keyEncryptionAlgorithm)
        v.add(encryptedKey)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a KeyTransRecipientInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [KeyTransRecipientInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with KeyTransRecipientInfo structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): KeyTransRecipientInfo? {
            if (obj is KeyTransRecipientInfo) {
                return obj
            }

            if (obj != null) {
                return KeyTransRecipientInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
