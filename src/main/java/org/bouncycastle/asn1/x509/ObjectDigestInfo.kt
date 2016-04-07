package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence

/**
 * ObjectDigestInfo ASN.1 structure used in v2 attribute certificates.

 *

 * ObjectDigestInfo ::= SEQUENCE {
 * digestedObjectType  ENUMERATED {
 * publicKey            (0),
 * publicKeyCert        (1),
 * otherObjectTypes     (2) },
 * -- otherObjectTypes MUST NOT
 * -- be used in this profile
 * otherObjectTypeID   OBJECT IDENTIFIER OPTIONAL,
 * digestAlgorithm     AlgorithmIdentifier,
 * objectDigest        BIT STRING
 * }

 *

 */
class ObjectDigestInfo : ASN1Object {

    var digestedObjectType: ASN1Enumerated
        internal set

    var otherObjectTypeID: ASN1ObjectIdentifier? = null
        internal set

    var digestAlgorithm: AlgorithmIdentifier
        internal set

    var objectDigest: DERBitString
        internal set

    /**
     * Constructor from given details.
     *
     *
     * If `digestedObjectType` is not [.publicKeyCert] or
     * [.publicKey] `otherObjectTypeID` must be given,
     * otherwise it is ignored.

     * @param digestedObjectType The digest object type.
     * *
     * @param otherObjectTypeID The object type ID for
     * *            `otherObjectDigest`.
     * *
     * @param digestAlgorithm The algorithm identifier for the hash.
     * *
     * @param objectDigest The hash value.
     */
    constructor(
            digestedObjectType: Int,
            otherObjectTypeID: ASN1ObjectIdentifier,
            digestAlgorithm: AlgorithmIdentifier,
            objectDigest: ByteArray) {
        this.digestedObjectType = ASN1Enumerated(digestedObjectType)
        if (digestedObjectType == otherObjectDigest) {
            this.otherObjectTypeID = otherObjectTypeID
        }

        this.digestAlgorithm = digestAlgorithm
        this.objectDigest = DERBitString(objectDigest)
    }

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() > 4 || seq.size() < 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        digestedObjectType = ASN1Enumerated.getInstance(seq.getObjectAt(0))

        var offset = 0

        if (seq.size() == 4) {
            otherObjectTypeID = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(1))
            offset++
        }

        digestAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1 + offset))

        objectDigest = DERBitString.getInstance(seq.getObjectAt(2 + offset))
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.

     *

     * ObjectDigestInfo ::= SEQUENCE {
     * digestedObjectType  ENUMERATED {
     * publicKey            (0),
     * publicKeyCert        (1),
     * otherObjectTypes     (2) },
     * -- otherObjectTypes MUST NOT
     * -- be used in this profile
     * otherObjectTypeID   OBJECT IDENTIFIER OPTIONAL,
     * digestAlgorithm     AlgorithmIdentifier,
     * objectDigest        BIT STRING
     * }

     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(digestedObjectType)

        if (otherObjectTypeID != null) {
            v.add(otherObjectTypeID)
        }

        v.add(digestAlgorithm)
        v.add(objectDigest)

        return DERSequence(v)
    }

    companion object {
        /**
         * The public key is hashed.
         */
        val publicKey = 0

        /**
         * The public key certificate is hashed.
         */
        val publicKeyCert = 1

        /**
         * An other object is hashed.
         */
        val otherObjectDigest = 2

        fun getInstance(
                obj: Any?): ObjectDigestInfo? {
            if (obj is ObjectDigestInfo) {
                return obj
            }

            if (obj != null) {
                return ObjectDigestInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ObjectDigestInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
