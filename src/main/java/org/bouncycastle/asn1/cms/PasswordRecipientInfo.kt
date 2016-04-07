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
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-10.2.7):
 * Content encryption key delivery mechanisms.
 *
 * PasswordRecipientInfo ::= SEQUENCE {
 * version       CMSVersion,   -- Always set to 0
 * keyDerivationAlgorithm [0] KeyDerivationAlgorithmIdentifier
 * OPTIONAL,
 * keyEncryptionAlgorithm KeyEncryptionAlgorithmIdentifier,
 * encryptedKey  EncryptedKey }
 *
 */
class PasswordRecipientInfo : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var keyDerivationAlgorithm: AlgorithmIdentifier? = null
        private set
    var keyEncryptionAlgorithm: AlgorithmIdentifier? = null
        private set
    var encryptedKey: ASN1OctetString? = null
        private set

    constructor(
            keyEncryptionAlgorithm: AlgorithmIdentifier,
            encryptedKey: ASN1OctetString) {
        this.version = ASN1Integer(0)
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm
        this.encryptedKey = encryptedKey
    }

    constructor(
            keyDerivationAlgorithm: AlgorithmIdentifier,
            keyEncryptionAlgorithm: AlgorithmIdentifier,
            encryptedKey: ASN1OctetString) {
        this.version = ASN1Integer(0)
        this.keyDerivationAlgorithm = keyDerivationAlgorithm
        this.keyEncryptionAlgorithm = keyEncryptionAlgorithm
        this.encryptedKey = encryptedKey
    }


    @Deprecated("use getInstance() method.")
    constructor(
            seq: ASN1Sequence) {
        version = seq.getObjectAt(0) as ASN1Integer
        if (seq.getObjectAt(1) is ASN1TaggedObject) {
            keyDerivationAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1) as ASN1TaggedObject, false)
            keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(2))
            encryptedKey = seq.getObjectAt(3) as ASN1OctetString
        } else {
            keyEncryptionAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
            encryptedKey = seq.getObjectAt(2) as ASN1OctetString
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)

        if (keyDerivationAlgorithm != null) {
            v.add(DERTaggedObject(false, 0, keyDerivationAlgorithm))
        }
        v.add(keyEncryptionAlgorithm)
        v.add(encryptedKey)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a PasswordRecipientInfo object from a tagged object.

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
                explicit: Boolean): PasswordRecipientInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return a PasswordRecipientInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [PasswordRecipientInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with PasswordRecipientInfo structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): PasswordRecipientInfo? {
            if (obj is PasswordRecipientInfo) {
                return obj
            }

            if (obj != null) {
                return PasswordRecipientInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
