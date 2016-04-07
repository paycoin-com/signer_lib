package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 *
 * OcspResponsesID ::= SEQUENCE {
 * ocspIdentifier OcspIdentifier,
 * ocspRepHash OtherHash OPTIONAL
 * }
 *
 */
class OcspResponsesID : ASN1Object {

    var ocspIdentifier: OcspIdentifier? = null
        private set
    var ocspRepHash: OtherHash? = null
        private set

    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        this.ocspIdentifier = OcspIdentifier.getInstance(seq.getObjectAt(0))
        if (seq.size() > 1) {
            this.ocspRepHash = OtherHash.getInstance(seq.getObjectAt(1))
        }
    }

    @JvmOverloads constructor(ocspIdentifier: OcspIdentifier, ocspRepHash: OtherHash? = null) {
        this.ocspIdentifier = ocspIdentifier
        this.ocspRepHash = ocspRepHash
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.ocspIdentifier)
        if (null != this.ocspRepHash) {
            v.add(this.ocspRepHash)
        }
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): OcspResponsesID? {
            if (obj is OcspResponsesID) {
                return obj
            } else if (obj != null) {
                return OcspResponsesID(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
