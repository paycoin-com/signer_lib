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
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.3):
 * Content encryption key delivery mechanisms.
 *
 *
 *
 * KEKRecipientInfo ::= SEQUENCE {
 * version CMSVersion,  -- always set to 4
 * kekid KEKIdentifier,
 * keyEncryptionAlgorithm KeyEncryptionAlgorithmIdentifier,
 * encryptedKey EncryptedKey
 * }
 *
 */
class KEKRecipientInfo : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var kekid: KEKIdentifier? = null
        private set
    var keyEncryptionAlgorithm: AlgorithmIdentifier? = null
        private set
    var encryptedKey: ASN1OctetString? = null
        private set

    constructor(
            kekid: KEKIdentifier,
            keyEncryptionAlgorithm: AlgorithmIdentifier,
            encryptedKey: ASN1OctetString) {
        this.version = ASN1Integer(4)
        this.kekid = kekid
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm
        this.encryptedKey = encryptedKey
    }

    constructor(
            seq: ASN1Sequence) {
        version = seq.getObjectAt(0) as ASN1Integer
        kekid = KEKIdentifier.getInstance(seq.getObjectAt(1))
        keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(2))
        encryptedKey = seq.getObjectAt(3) as ASN1OctetString
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(kekid)
        v.add(keyEncryptionAlgorithm)
        v.add(encryptedKey)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a KEKRecipientInfo object from a tagged object.

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
                explicit: Boolean): KEKRecipientInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return a KEKRecipientInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [KEKRecipientInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with KEKRecipientInfo structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): KEKRecipientInfo? {
            if (obj is KEKRecipientInfo) {
                return obj
            }

            if (obj != null) {
                return KEKRecipientInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
