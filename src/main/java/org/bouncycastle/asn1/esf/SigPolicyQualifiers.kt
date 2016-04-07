package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class SigPolicyQualifiers : ASN1Object {
    internal var qualifiers: ASN1Sequence

    private constructor(
            seq: ASN1Sequence) {
        qualifiers = seq
    }

    constructor(
            qualifierInfos: Array<SigPolicyQualifierInfo>) {
        val v = ASN1EncodableVector()
        for (i in qualifierInfos.indices) {
            v.add(qualifierInfos[i])
        }
        qualifiers = DERSequence(v)
    }

    /**
     * Return the number of qualifier info elements present.

     * @return number of elements present.
     */
    fun size(): Int {
        return qualifiers.size()
    }

    /**
     * Return the SigPolicyQualifierInfo at index i.

     * @param i index of the info of interest
     * *
     * @return the info at index i.
     */
    fun getInfoAt(
            i: Int): SigPolicyQualifierInfo {
        return SigPolicyQualifierInfo.getInstance(qualifiers.getObjectAt(i))
    }

    /**
     *
     * SigPolicyQualifiers ::= SEQUENCE SIZE (1..MAX) OF SigPolicyQualifierInfo
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return qualifiers
    }

    companion object {

        fun getInstance(
                obj: Any): SigPolicyQualifiers? {
            if (obj is SigPolicyQualifiers) {
                return obj
            } else if (obj is ASN1Sequence) {
                return SigPolicyQualifiers(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
