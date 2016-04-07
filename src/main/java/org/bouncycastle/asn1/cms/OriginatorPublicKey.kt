package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.2):
 * Content encryption key delivery mechanisms.
 *
 *
 *
 * OriginatorPublicKey ::= SEQUENCE {
 * algorithm AlgorithmIdentifier,
 * publicKey BIT STRING
 * }
 *
 */
class OriginatorPublicKey : ASN1Object {
    var algorithm: AlgorithmIdentifier? = null
        private set
    var publicKey: DERBitString? = null
        private set

    constructor(
            algorithm: AlgorithmIdentifier,
            publicKey: ByteArray) {
        this.algorithm = algorithm
        this.publicKey = DERBitString(publicKey)
    }


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        algorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(0))
        publicKey = seq.getObjectAt(1) as DERBitString
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(algorithm)
        v.add(publicKey)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an OriginatorPublicKey object from a tagged object.

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
                explicit: Boolean): OriginatorPublicKey {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return an OriginatorPublicKey object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [OriginatorPublicKey] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with OriginatorPublicKey structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): OriginatorPublicKey? {
            if (obj is OriginatorPublicKey) {
                return obj
            }

            if (obj != null) {
                return OriginatorPublicKey(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
