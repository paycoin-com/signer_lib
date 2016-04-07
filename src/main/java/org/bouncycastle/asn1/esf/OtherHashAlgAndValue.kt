package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class OtherHashAlgAndValue : ASN1Object {
    var hashAlgorithm: AlgorithmIdentifier? = null
        private set
    var hashValue: ASN1OctetString? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        hashAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(0))
        hashValue = ASN1OctetString.getInstance(seq.getObjectAt(1))
    }

    constructor(
            hashAlgorithm: AlgorithmIdentifier,
            hashValue: ASN1OctetString) {
        this.hashAlgorithm = hashAlgorithm
        this.hashValue = hashValue
    }

    /**
     *
     * OtherHashAlgAndValue ::= SEQUENCE {
     * hashAlgorithm AlgorithmIdentifier,
     * hashValue OtherHashValue }

     * OtherHashValue ::= OCTET STRING
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(hashAlgorithm)
        v.add(hashValue)

        return DERSequence(v)
    }

    companion object {


        fun getInstance(
                obj: Any?): OtherHashAlgAndValue? {
            if (obj is OtherHashAlgAndValue) {
                return obj
            } else if (obj != null) {
                return OtherHashAlgAndValue(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
