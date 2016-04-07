package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-7) DigestedData object.
 *
 * DigestedData ::= SEQUENCE {
 * version CMSVersion,
 * digestAlgorithm DigestAlgorithmIdentifier,
 * encapContentInfo EncapsulatedContentInfo,
 * digest Digest }
 *
 */
class DigestedData : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var digestAlgorithm: AlgorithmIdentifier? = null
        private set
    var encapContentInfo: ContentInfo? = null
        private set
    private var digest: ASN1OctetString? = null

    constructor(
            digestAlgorithm: AlgorithmIdentifier,
            encapContentInfo: ContentInfo,
            digest: ByteArray) {
        this.version = ASN1Integer(0)
        this.digestAlgorithm = digestAlgorithm
        this.encapContentInfo = encapContentInfo
        this.digest = DEROctetString(digest)
    }

    private constructor(
            seq: ASN1Sequence) {
        this.version = seq.getObjectAt(0) as ASN1Integer
        this.digestAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
        this.encapContentInfo = ContentInfo.getInstance(seq.getObjectAt(2))
        this.digest = ASN1OctetString.getInstance(seq.getObjectAt(3))
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(digestAlgorithm)
        v.add(encapContentInfo)
        v.add(digest)

        return BERSequence(v)
    }

    fun getDigest(): ByteArray {
        return digest!!.octets
    }

    companion object {

        /**
         * Return a DigestedData object from a tagged object.

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
                isExplicit: Boolean): DigestedData {
            return getInstance(ASN1Sequence.getInstance(ato, isExplicit))
        }

        /**
         * Return a DigestedData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [DigestedData] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): DigestedData? {
            if (obj is DigestedData) {
                return obj
            }

            if (obj != null) {
                return DigestedData(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
