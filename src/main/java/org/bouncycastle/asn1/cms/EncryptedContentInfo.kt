package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.BERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.1) EncryptedContentInfo object.

 *
 * EncryptedContentInfo ::= SEQUENCE {
 * contentType ContentType,
 * contentEncryptionAlgorithm ContentEncryptionAlgorithmIdentifier,
 * encryptedContent [0] IMPLICIT EncryptedContent OPTIONAL
 * }
 *
 */
class EncryptedContentInfo : ASN1Object {
    var contentType: ASN1ObjectIdentifier? = null
        private set
    var contentEncryptionAlgorithm: AlgorithmIdentifier? = null
        private set
    var encryptedContent: ASN1OctetString? = null
        private set

    constructor(
            contentType: ASN1ObjectIdentifier,
            contentEncryptionAlgorithm: AlgorithmIdentifier,
            encryptedContent: ASN1OctetString) {
        this.contentType = contentType
        this.contentEncryptionAlgorithm = contentEncryptionAlgorithm
        this.encryptedContent = encryptedContent
    }

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() < 2) {
            throw IllegalArgumentException("Truncated Sequence Found")
        }

        contentType = seq.getObjectAt(0) as ASN1ObjectIdentifier
        contentEncryptionAlgorithm = AlgorithmIdentifier.getInstance(
                seq.getObjectAt(1))
        if (seq.size() > 2) {
            encryptedContent = ASN1OctetString.getInstance(
                    seq.getObjectAt(2) as ASN1TaggedObject, false)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(contentType)
        v.add(contentEncryptionAlgorithm)

        if (encryptedContent != null) {
            v.add(BERTaggedObject(false, 0, encryptedContent))
        }

        return BERSequence(v)
    }

    companion object {

        /**
         * Return an EncryptedContentInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [EncryptedContentInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): EncryptedContentInfo? {
            if (obj is EncryptedContentInfo) {
                return obj
            }
            if (obj != null) {
                return EncryptedContentInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
