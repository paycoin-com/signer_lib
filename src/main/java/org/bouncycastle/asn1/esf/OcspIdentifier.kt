package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.ocsp.ResponderID

/**
 *
 * OcspIdentifier ::= SEQUENCE {
 * ocspResponderID ResponderID, -- As in OCSP response data
 * producedAt GeneralizedTime -- As in OCSP response data
 * }
 *
 */
class OcspIdentifier : ASN1Object {
    var ocspResponderID: ResponderID? = null
        private set
    var producedAt: ASN1GeneralizedTime? = null
        private set

    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        this.ocspResponderID = ResponderID.getInstance(seq.getObjectAt(0))
        this.producedAt = seq.getObjectAt(1) as ASN1GeneralizedTime
    }

    constructor(ocspResponderID: ResponderID, producedAt: ASN1GeneralizedTime) {
        this.ocspResponderID = ocspResponderID
        this.producedAt = producedAt
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.ocspResponderID)
        v.add(this.producedAt)
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): OcspIdentifier? {
            if (obj is OcspIdentifier) {
                return obj
            } else if (obj != null) {
                return OcspIdentifier(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
