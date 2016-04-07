package org.bouncycastle.asn1.cms.ecc

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.util.Arrays

/**
 *
 * ECC-CMS-SharedInfo ::= SEQUENCE {
 * keyInfo AlgorithmIdentifier,
 * entityUInfo [0] EXPLICIT OCTET STRING OPTIONAL,
 * suppPubInfo [2] EXPLICIT OCTET STRING   }
 *
 */
class ECCCMSSharedInfo : ASN1Object {

    private val keyInfo: AlgorithmIdentifier
    private val entityUInfo: ByteArray?
    private val suppPubInfo: ByteArray

    constructor(
            keyInfo: AlgorithmIdentifier,
            entityUInfo: ByteArray,
            suppPubInfo: ByteArray) {
        this.keyInfo = keyInfo
        this.entityUInfo = Arrays.clone(entityUInfo)
        this.suppPubInfo = Arrays.clone(suppPubInfo)
    }

    constructor(
            keyInfo: AlgorithmIdentifier,
            suppPubInfo: ByteArray) {
        this.keyInfo = keyInfo
        this.entityUInfo = null
        this.suppPubInfo = Arrays.clone(suppPubInfo)
    }

    private constructor(
            seq: ASN1Sequence) {
        this.keyInfo = AlgorithmIdentifier.getInstance(seq.getObjectAt(0))

        if (seq.size() == 2) {
            this.entityUInfo = null
            this.suppPubInfo = ASN1OctetString.getInstance(seq.getObjectAt(1) as ASN1TaggedObject, true).octets
        } else {
            this.entityUInfo = ASN1OctetString.getInstance(seq.getObjectAt(1) as ASN1TaggedObject, true).octets
            this.suppPubInfo = ASN1OctetString.getInstance(seq.getObjectAt(2) as ASN1TaggedObject, true).octets
        }
    }


    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(keyInfo)

        if (entityUInfo != null) {
            v.add(DERTaggedObject(true, 0, DEROctetString(entityUInfo)))
        }

        v.add(DERTaggedObject(true, 2, DEROctetString(suppPubInfo)))

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an ECC-CMS-SharedInfo object from a tagged object.

         * @param obj      the tagged object holding the object we want.
         * *
         * @param explicit true if the object is meant to be explicitly
         * *                 tagged false otherwise.
         * *
         * @throws IllegalArgumentException if the object held by the
         * *                                  tagged object cannot be converted.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ECCCMSSharedInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): ECCCMSSharedInfo? {
            if (obj is ECCCMSSharedInfo) {
                return obj
            } else if (obj != null) {
                return ECCCMSSharedInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
