package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 *
 * CrlValidatedID ::= SEQUENCE {
 * crlHash OtherHash,
 * crlIdentifier CrlIdentifier OPTIONAL }
 *
 */
class CrlValidatedID : ASN1Object {

    var crlHash: OtherHash? = null
        private set
    var crlIdentifier: CrlIdentifier? = null
        private set

    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        this.crlHash = OtherHash.getInstance(seq.getObjectAt(0))
        if (seq.size() > 1) {
            this.crlIdentifier = CrlIdentifier.getInstance(seq.getObjectAt(1))
        }
    }

    @JvmOverloads constructor(crlHash: OtherHash, crlIdentifier: CrlIdentifier? = null) {
        this.crlHash = crlHash
        this.crlIdentifier = crlIdentifier
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.crlHash!!.toASN1Primitive())
        if (null != this.crlIdentifier) {
            v.add(this.crlIdentifier!!.toASN1Primitive())
        }
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): CrlValidatedID? {
            if (obj is CrlValidatedID) {
                return obj
            } else if (obj != null) {
                return CrlValidatedID(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
