package org.bouncycastle.asn1.pkcs

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.BERTaggedObject
import org.bouncycastle.asn1.DLSequence

class ContentInfo : ASN1Object, PKCSObjectIdentifiers {
    var contentType: ASN1ObjectIdentifier? = null
        private set
    var content: ASN1Encodable? = null
        private set
    private var isBer = true

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        contentType = e.nextElement() as ASN1ObjectIdentifier

        if (e.hasMoreElements()) {
            content = (e.nextElement() as ASN1TaggedObject).`object`
        }

        isBer = seq is BERSequence
    }

    constructor(
            contentType: ASN1ObjectIdentifier,
            content: ASN1Encodable) {
        this.contentType = contentType
        this.content = content
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * ContentInfo ::= SEQUENCE {
     * contentType ContentType,
     * content
     * [0] EXPLICIT ANY DEFINED BY contentType OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(contentType)

        if (content != null) {
            v.add(BERTaggedObject(true, 0, content))
        }

        if (isBer) {
            return BERSequence(v)
        } else {
            return DLSequence(v)
        }
    }

    companion object {

        fun getInstance(
                obj: Any?): ContentInfo? {
            if (obj is ContentInfo) {
                return obj
            }

            if (obj != null) {
                return ContentInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
