package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class PolicyInformation : ASN1Object {
    var policyIdentifier: ASN1ObjectIdentifier? = null
        private set
    var policyQualifiers: ASN1Sequence? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        policyIdentifier = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))

        if (seq.size() > 1) {
            policyQualifiers = ASN1Sequence.getInstance(seq.getObjectAt(1))
        }
    }

    constructor(
            policyIdentifier: ASN1ObjectIdentifier) {
        this.policyIdentifier = policyIdentifier
    }

    constructor(
            policyIdentifier: ASN1ObjectIdentifier,
            policyQualifiers: ASN1Sequence) {
        this.policyIdentifier = policyIdentifier
        this.policyQualifiers = policyQualifiers
    }

    /*
     * <pre>
     * PolicyInformation ::= SEQUENCE {
     *      policyIdentifier   CertPolicyId,
     *      policyQualifiers   SEQUENCE SIZE (1..MAX) OF
     *              PolicyQualifierInfo OPTIONAL }
     * </pre>
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(policyIdentifier)

        if (policyQualifiers != null) {
            v.add(policyQualifiers)
        }

        return DERSequence(v)
    }

    override fun toString(): String {
        val sb = StringBuffer()

        sb.append("Policy information: ")
        sb.append(policyIdentifier)

        if (policyQualifiers != null) {
            val p = StringBuffer()
            for (i in 0..policyQualifiers!!.size() - 1) {
                if (p.length != 0) {
                    p.append(", ")
                }
                p.append(PolicyQualifierInfo.getInstance(policyQualifiers!!.getObjectAt(i)))
            }

            sb.append("[")
            sb.append(p)
            sb.append("]")
        }

        return sb.toString()
    }

    companion object {

        fun getInstance(
                obj: Any?): PolicyInformation {
            if (obj == null || obj is PolicyInformation) {
                return obj as PolicyInformation?
            }

            return PolicyInformation(ASN1Sequence.getInstance(obj))
        }
    }
}
