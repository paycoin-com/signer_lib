package org.bouncycastle.asn1.cms.ecc

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.cms.OriginatorPublicKey

/**
 * [RFC 5753/3278](http://tools.ietf.org/html/rfc5753): MQVuserKeyingMaterial object.
 *
 * MQVuserKeyingMaterial ::= SEQUENCE {
 * ephemeralPublicKey OriginatorPublicKey,
 * addedukm [0] EXPLICIT UserKeyingMaterial OPTIONAL  }
 *
 */
class MQVuserKeyingMaterial : ASN1Object {
    var ephemeralPublicKey: OriginatorPublicKey? = null
        private set
    var addedukm: ASN1OctetString? = null
        private set

    constructor(
            ephemeralPublicKey: OriginatorPublicKey?,
            addedukm: ASN1OctetString) {
        if (ephemeralPublicKey == null) {
            throw IllegalArgumentException("Ephemeral public key cannot be null")
        }

        this.ephemeralPublicKey = ephemeralPublicKey
        this.addedukm = addedukm
    }

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 1 && seq.size() != 2) {
            throw IllegalArgumentException("Sequence has incorrect number of elements")
        }

        this.ephemeralPublicKey = OriginatorPublicKey.getInstance(
                seq.getObjectAt(0))

        if (seq.size() > 1) {
            this.addedukm = ASN1OctetString.getInstance(
                    seq.getObjectAt(1) as ASN1TaggedObject, true)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(ephemeralPublicKey)

        if (addedukm != null) {
            v.add(DERTaggedObject(true, 0, addedukm))
        }

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an MQVuserKeyingMaterial object from a tagged object.

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
                explicit: Boolean): MQVuserKeyingMaterial {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return an MQVuserKeyingMaterial object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [MQVuserKeyingMaterial] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence] with MQVuserKeyingMaterial inside it.
         *

         * @param obj the object we want converted.
         * *
         * @throws IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): MQVuserKeyingMaterial? {
            if (obj is MQVuserKeyingMaterial) {
                return obj
            } else if (obj != null) {
                return MQVuserKeyingMaterial(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
