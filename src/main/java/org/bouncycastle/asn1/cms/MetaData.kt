package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERUTF8String

/**
 * [RFC 5544](http://tools.ietf.org/html/rfc5544):
 * Binding Documents with Time-Stamps; MetaData object.
 *
 *
 *
 * MetaData ::= SEQUENCE {
 * hashProtected        BOOLEAN,
 * fileName             UTF8String OPTIONAL,
 * mediaType            IA5String OPTIONAL,
 * otherMetaData        Attributes OPTIONAL
 * }
 *
 */
class MetaData : ASN1Object {
    private var hashProtected: ASN1Boolean? = null
    var fileName: DERUTF8String? = null
        private set
    var mediaType: DERIA5String? = null
        private set
    var otherMetaData: Attributes? = null
        private set

    constructor(
            hashProtected: ASN1Boolean,
            fileName: DERUTF8String,
            mediaType: DERIA5String,
            otherMetaData: Attributes) {
        this.hashProtected = hashProtected
        this.fileName = fileName
        this.mediaType = mediaType
        this.otherMetaData = otherMetaData
    }

    private constructor(seq: ASN1Sequence) {
        this.hashProtected = ASN1Boolean.getInstance(seq.getObjectAt(0))

        var index = 1

        if (index < seq.size() && seq.getObjectAt(index) is DERUTF8String) {
            this.fileName = DERUTF8String.getInstance(seq.getObjectAt(index++))
        }
        if (index < seq.size() && seq.getObjectAt(index) is DERIA5String) {
            this.mediaType = DERIA5String.getInstance(seq.getObjectAt(index++))
        }
        if (index < seq.size()) {
            this.otherMetaData = Attributes.getInstance(seq.getObjectAt(index++))
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(hashProtected)

        if (fileName != null) {
            v.add(fileName)
        }

        if (mediaType != null) {
            v.add(mediaType)
        }

        if (otherMetaData != null) {
            v.add(otherMetaData)
        }

        return DERSequence(v)
    }

    val isHashProtected: Boolean
        get() = hashProtected!!.isTrue

    companion object {

        /**
         * Return a MetaData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [MetaData] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with MetaData structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(obj: Any?): MetaData? {
            if (obj is MetaData) {
                return obj
            } else if (obj != null) {
                return MetaData(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
