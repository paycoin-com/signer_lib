package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.BERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-3) ContentInfo, and
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-5.2) EncapsulatedContentInfo objects.

 *
 * ContentInfo ::= SEQUENCE {
 * contentType ContentType,
 * content [0] EXPLICIT ANY DEFINED BY contentType OPTIONAL
 * }

 * EncapsulatedContentInfo ::= SEQUENCE {
 * eContentType ContentType,
 * eContent [0] EXPLICIT OCTET STRING OPTIONAL
 * }
 *
 */
class ContentInfo : ASN1Object, CMSObjectIdentifiers {
    var contentType: ASN1ObjectIdentifier? = null
        private set
    var content: ASN1Encodable? = null
        private set


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        contentType = seq.getObjectAt(0) as ASN1ObjectIdentifier

        if (seq.size() > 1) {
            val tagged = seq.getObjectAt(1) as ASN1TaggedObject
            if (!tagged.isExplicit || tagged.tagNo != 0) {
                throw IllegalArgumentException("Bad tag for 'content'")
            }

            content = tagged.`object`
        }
    }

    constructor(
            contentType: ASN1ObjectIdentifier,
            content: ASN1Encodable) {
        this.contentType = contentType
        this.content = content
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(contentType)

        if (content != null) {
            v.add(BERTaggedObject(0, content))
        }

        return BERSequence(v)
    }

    companion object {

        /**
         * Return an ContentInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [ContentInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with ContentInfo structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): ContentInfo? {
            if (obj is ContentInfo) {
                return obj
            } else if (obj != null) {
                return ContentInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ContentInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
