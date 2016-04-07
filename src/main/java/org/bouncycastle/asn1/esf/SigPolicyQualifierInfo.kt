package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class SigPolicyQualifierInfo : ASN1Object {
    private var sigPolicyQualifierId: ASN1ObjectIdentifier? = null
    var sigQualifier: ASN1Encodable? = null
        private set

    constructor(
            sigPolicyQualifierId: ASN1ObjectIdentifier,
            sigQualifier: ASN1Encodable) {
        this.sigPolicyQualifierId = sigPolicyQualifierId
        this.sigQualifier = sigQualifier
    }

    private constructor(
            seq: ASN1Sequence) {
        sigPolicyQualifierId = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
        sigQualifier = seq.getObjectAt(1)
    }

    fun getSigPolicyQualifierId(): ASN1ObjectIdentifier {
        return ASN1ObjectIdentifier(sigPolicyQualifierId!!.id)
    }

    /**
     *
     * SigPolicyQualifierInfo ::= SEQUENCE {
     * sigPolicyQualifierId SigPolicyQualifierId,
     * sigQualifier ANY DEFINED BY sigPolicyQualifierId }

     * SigPolicyQualifierId ::= OBJECT IDENTIFIER
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(sigPolicyQualifierId)
        v.add(sigQualifier)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): SigPolicyQualifierInfo? {
            if (obj is SigPolicyQualifierInfo) {
                return obj
            } else if (obj != null) {
                return SigPolicyQualifierInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
