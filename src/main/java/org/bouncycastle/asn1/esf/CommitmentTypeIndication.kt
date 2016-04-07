package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class CommitmentTypeIndication : ASN1Object {
    var commitmentTypeId: ASN1ObjectIdentifier? = null
        private set
    var commitmentTypeQualifier: ASN1Sequence? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        commitmentTypeId = seq.getObjectAt(0) as ASN1ObjectIdentifier

        if (seq.size() > 1) {
            commitmentTypeQualifier = seq.getObjectAt(1) as ASN1Sequence
        }
    }

    constructor(
            commitmentTypeId: ASN1ObjectIdentifier) {
        this.commitmentTypeId = commitmentTypeId
    }

    constructor(
            commitmentTypeId: ASN1ObjectIdentifier,
            commitmentTypeQualifier: ASN1Sequence) {
        this.commitmentTypeId = commitmentTypeId
        this.commitmentTypeQualifier = commitmentTypeQualifier
    }

    /**
     *
     * CommitmentTypeIndication ::= SEQUENCE {
     * commitmentTypeId   CommitmentTypeIdentifier,
     * commitmentTypeQualifier   SEQUENCE SIZE (1..MAX) OF
     * CommitmentTypeQualifier OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(commitmentTypeId)

        if (commitmentTypeQualifier != null) {
            v.add(commitmentTypeQualifier)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): CommitmentTypeIndication {
            if (obj == null || obj is CommitmentTypeIndication) {
                return obj as CommitmentTypeIndication?
            }

            return CommitmentTypeIndication(ASN1Sequence.getInstance(obj))
        }
    }
}
