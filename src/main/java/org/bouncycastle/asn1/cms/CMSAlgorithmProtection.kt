package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * From RFC 6211
 *
 * CMSAlgorithmProtection ::= SEQUENCE {
 * digestAlgorithm         DigestAlgorithmIdentifier,
 * signatureAlgorithm  [1] SignatureAlgorithmIdentifier OPTIONAL,
 * macAlgorithm        [2] MessageAuthenticationCodeAlgorithm
 * OPTIONAL
 * }
 * (WITH COMPONENTS { signatureAlgorithm PRESENT,
 * macAlgorithm ABSENT } |
 * WITH COMPONENTS { signatureAlgorithm ABSENT,
 * macAlgorithm PRESENT })
 *
 */
class CMSAlgorithmProtection : ASN1Object {

    val digestAlgorithm: AlgorithmIdentifier
    val signatureAlgorithm: AlgorithmIdentifier?
    val macAlgorithm: AlgorithmIdentifier?

    constructor(digestAlgorithm: AlgorithmIdentifier?, type: Int, algorithmIdentifier: AlgorithmIdentifier?) {
        if (digestAlgorithm == null || algorithmIdentifier == null) {
            throw NullPointerException("AlgorithmIdentifiers cannot be null")
        }

        this.digestAlgorithm = digestAlgorithm

        if (type == 1) {
            this.signatureAlgorithm = algorithmIdentifier
            this.macAlgorithm = null
        } else if (type == 2) {
            this.signatureAlgorithm = null
            this.macAlgorithm = algorithmIdentifier
        } else {
            throw IllegalArgumentException("Unknown type: " + type)
        }
    }

    private constructor(sequence: ASN1Sequence) {
        if (sequence.size() != 2) {
            throw IllegalArgumentException("Sequence wrong size: One of signatureAlgorithm or macAlgorithm must be present")
        }

        this.digestAlgorithm = AlgorithmIdentifier.getInstance(sequence.getObjectAt(0))

        val tagged = ASN1TaggedObject.getInstance(sequence.getObjectAt(1))
        if (tagged.tagNo == 1) {
            this.signatureAlgorithm = AlgorithmIdentifier.getInstance(tagged, false)
            this.macAlgorithm = null
        } else if (tagged.tagNo == 2) {
            this.signatureAlgorithm = null

            this.macAlgorithm = AlgorithmIdentifier.getInstance(tagged, false)
        } else {
            throw IllegalArgumentException("Unknown tag found: " + tagged.tagNo)
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(digestAlgorithm)
        if (signatureAlgorithm != null) {
            v.add(DERTaggedObject(false, 1, signatureAlgorithm))
        }
        if (macAlgorithm != null) {
            v.add(DERTaggedObject(false, 2, macAlgorithm))
        }

        return DERSequence(v)
    }

    companion object {
        val SIGNATURE = 1
        val MAC = 2

        fun getInstance(
                obj: Any?): CMSAlgorithmProtection? {
            if (obj is CMSAlgorithmProtection) {
                return obj
            } else if (obj != null) {
                return CMSAlgorithmProtection(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
