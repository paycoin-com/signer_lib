package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class SignaturePolicyId : ASN1Object {
    private var sigPolicyId: ASN1ObjectIdentifier? = null
    var sigPolicyHash: OtherHashAlgAndValue? = null
        private set
    var sigPolicyQualifiers: SigPolicyQualifiers? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 2 && seq.size() != 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        sigPolicyId = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
        sigPolicyHash = OtherHashAlgAndValue.getInstance(seq.getObjectAt(1))

        if (seq.size() == 3) {
            sigPolicyQualifiers = SigPolicyQualifiers.getInstance(seq.getObjectAt(2))
        }
    }

    constructor(
            sigPolicyIdentifier: ASN1ObjectIdentifier,
            sigPolicyHash: OtherHashAlgAndValue) : this(sigPolicyIdentifier, sigPolicyHash, null) {
    }

    constructor(
            sigPolicyId: ASN1ObjectIdentifier,
            sigPolicyHash: OtherHashAlgAndValue,
            sigPolicyQualifiers: SigPolicyQualifiers?) {
        this.sigPolicyId = sigPolicyId
        this.sigPolicyHash = sigPolicyHash
        this.sigPolicyQualifiers = sigPolicyQualifiers
    }

    fun getSigPolicyId(): ASN1ObjectIdentifier {
        return ASN1ObjectIdentifier(sigPolicyId!!.id)
    }

    /**
     *
     * SignaturePolicyId ::= SEQUENCE {
     * sigPolicyId SigPolicyId,
     * sigPolicyHash SigPolicyHash,
     * sigPolicyQualifiers SEQUENCE SIZE (1..MAX) OF SigPolicyQualifierInfo OPTIONAL}
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(sigPolicyId)
        v.add(sigPolicyHash)
        if (sigPolicyQualifiers != null) {
            v.add(sigPolicyQualifiers)
        }

        return DERSequence(v)
    }

    companion object {


        fun getInstance(
                obj: Any?): SignaturePolicyId? {
            if (obj is SignaturePolicyId) {
                return obj
            } else if (obj != null) {
                return SignaturePolicyId(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
