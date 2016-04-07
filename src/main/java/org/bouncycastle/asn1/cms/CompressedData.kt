package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * [RFC 3274](http://tools.ietf.org/html/rfc3274): CMS Compressed Data.

 *
 * CompressedData ::= SEQUENCE {
 * version CMSVersion,
 * compressionAlgorithm CompressionAlgorithmIdentifier,
 * encapContentInfo EncapsulatedContentInfo
 * }
 *
 */
class CompressedData : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var compressionAlgorithmIdentifier: AlgorithmIdentifier? = null
        private set
    var encapContentInfo: ContentInfo? = null
        private set

    constructor(
            compressionAlgorithm: AlgorithmIdentifier,
            encapContentInfo: ContentInfo) {
        this.version = ASN1Integer(0)
        this.compressionAlgorithmIdentifier = compressionAlgorithm
        this.encapContentInfo = encapContentInfo
    }

    private constructor(
            seq: ASN1Sequence) {
        this.version = seq.getObjectAt(0) as ASN1Integer
        this.compressionAlgorithmIdentifier = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
        this.encapContentInfo = ContentInfo.getInstance(seq.getObjectAt(2))
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(compressionAlgorithmIdentifier)
        v.add(encapContentInfo)

        return BERSequence(v)
    }

    companion object {

        /**
         * Return a CompressedData object from a tagged object.

         * @param ato the tagged object holding the object we want.
         * *
         * @param isExplicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the object held by the
         * *          tagged object cannot be converted.
         */
        fun getInstance(
                ato: ASN1TaggedObject,
                isExplicit: Boolean): CompressedData {
            return getInstance(ASN1Sequence.getInstance(ato, isExplicit))
        }

        /**
         * Return a CompressedData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [CompressedData] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with CompressedData structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): CompressedData? {
            if (obj is CompressedData) {
                return obj
            }

            if (obj != null) {
                return CompressedData(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
