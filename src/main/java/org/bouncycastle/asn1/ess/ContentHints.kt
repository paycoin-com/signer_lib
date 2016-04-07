package org.bouncycastle.asn1.ess

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERUTF8String

class ContentHints : ASN1Object {
    var contentDescription: DERUTF8String? = null
        private set
    var contentType: ASN1ObjectIdentifier? = null
        private set

    /**
     * constructor
     */
    private constructor(seq: ASN1Sequence) {
        val field = seq.getObjectAt(0)
        if (field.toASN1Primitive() is DERUTF8String) {
            contentDescription = DERUTF8String.getInstance(field)
            contentType = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(1))
        } else {
            contentType = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
        }
    }

    constructor(
            contentType: ASN1ObjectIdentifier) {
        this.contentType = contentType
        this.contentDescription = null
    }

    constructor(
            contentType: ASN1ObjectIdentifier,
            contentDescription: DERUTF8String) {
        this.contentType = contentType
        this.contentDescription = contentDescription
    }

    /**
     *
     * ContentHints ::= SEQUENCE {
     * contentDescription UTF8String (SIZE (1..MAX)) OPTIONAL,
     * contentType ContentType }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (contentDescription != null) {
            v.add(contentDescription)
        }

        v.add(contentType)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): ContentHints? {
            if (o is ContentHints) {
                return o
            } else if (o != null) {
                return ContentHints(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
