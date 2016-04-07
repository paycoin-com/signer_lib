package org.bouncycastle.asn1.dvcs

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.PolicyInformation

/**
 *
 * PathProcInput ::= SEQUENCE {
 * acceptablePolicySet          SEQUENCE SIZE (1..MAX) OF
 * PolicyInformation,
 * inhibitPolicyMapping         BOOLEAN DEFAULT FALSE,
 * explicitPolicyReqd           [0] BOOLEAN DEFAULT FALSE ,
 * inhibitAnyPolicy             [1] BOOLEAN DEFAULT FALSE
 * }
 *
 */
class PathProcInput : ASN1Object {

    var acceptablePolicySet: Array<PolicyInformation>? = null
        private set
    var isInhibitPolicyMapping = false
        private set(inhibitPolicyMapping) {
            this.isInhibitPolicyMapping = inhibitPolicyMapping
        }
    var isExplicitPolicyReqd = false
        private set(explicitPolicyReqd) {
            this.isExplicitPolicyReqd = explicitPolicyReqd
        }
    var isInhibitAnyPolicy = false
        private set(inhibitAnyPolicy) {
            this.isInhibitAnyPolicy = inhibitAnyPolicy
        }

    constructor(acceptablePolicySet: Array<PolicyInformation>) {
        this.acceptablePolicySet = acceptablePolicySet
    }

    constructor(acceptablePolicySet: Array<PolicyInformation>, inhibitPolicyMapping: Boolean, explicitPolicyReqd: Boolean, inhibitAnyPolicy: Boolean) {
        this.acceptablePolicySet = acceptablePolicySet
        this.isInhibitPolicyMapping = inhibitPolicyMapping
        this.isExplicitPolicyReqd = explicitPolicyReqd
        this.isInhibitAnyPolicy = inhibitAnyPolicy
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        val pV = ASN1EncodableVector()

        for (i in acceptablePolicySet!!.indices) {
            pV.add(acceptablePolicySet!![i])
        }

        v.add(DERSequence(pV))

        if (isInhibitPolicyMapping) {
            v.add(ASN1Boolean(isInhibitPolicyMapping))
        }
        if (isExplicitPolicyReqd) {
            v.add(DERTaggedObject(false, 0, ASN1Boolean(isExplicitPolicyReqd)))
        }
        if (isInhibitAnyPolicy) {
            v.add(DERTaggedObject(false, 1, ASN1Boolean(isInhibitAnyPolicy)))
        }

        return DERSequence(v)
    }

    override fun toString(): String {
        return "PathProcInput: {\nacceptablePolicySet: $acceptablePolicySet\ninhibitPolicyMapping: $isInhibitPolicyMapping\nexplicitPolicyReqd: $isExplicitPolicyReqd\ninhibitAnyPolicy: $isInhibitAnyPolicy\n}\n"
    }

    companion object {

        private fun fromSequence(seq: ASN1Sequence): Array<PolicyInformation> {
            val tmp = arrayOfNulls<PolicyInformation>(seq.size())

            for (i in tmp.indices) {
                tmp[i] = PolicyInformation.getInstance(seq.getObjectAt(i))
            }

            return tmp
        }

        fun getInstance(obj: Any?): PathProcInput? {
            if (obj is PathProcInput) {
                return obj
            } else if (obj != null) {
                val seq = ASN1Sequence.getInstance(obj)
                val policies = ASN1Sequence.getInstance(seq.getObjectAt(0))
                val result = PathProcInput(fromSequence(policies))

                for (i in 1..seq.size() - 1) {
                    val o = seq.getObjectAt(i)

                    if (o is ASN1Boolean) {
                        val x = ASN1Boolean.getInstance(o)
                        result.isInhibitPolicyMapping = x.isTrue
                    } else if (o is ASN1TaggedObject) {
                        val t = ASN1TaggedObject.getInstance(o)
                        val x: ASN1Boolean
                        when (t.tagNo) {
                            0 -> {
                                x = ASN1Boolean.getInstance(t, false)
                                result.isExplicitPolicyReqd = x.isTrue
                            }
                            1 -> {
                                x = ASN1Boolean.getInstance(t, false)
                                result.isInhibitAnyPolicy = x.isTrue
                            }
                        }
                    }
                }
                return result
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): PathProcInput {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
