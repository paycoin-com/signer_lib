package org.bouncycastle.asn1.x509

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * PKIX RFC 5280
 *
 * id-ce-policyConstraints OBJECT IDENTIFIER ::=  { id-ce 36 }

 * PolicyConstraints ::= SEQUENCE {
 * requireExplicitPolicy           [0] SkipCerts OPTIONAL,
 * inhibitPolicyMapping            [1] SkipCerts OPTIONAL }

 * SkipCerts ::= INTEGER (0..MAX)
 *
 */
class PolicyConstraints : ASN1Object {
    var requireExplicitPolicyMapping: BigInteger? = null
        private set
    var inhibitPolicyMapping: BigInteger? = null
        private set

    constructor(requireExplicitPolicyMapping: BigInteger, inhibitPolicyMapping: BigInteger) {
        this.requireExplicitPolicyMapping = requireExplicitPolicyMapping
        this.inhibitPolicyMapping = inhibitPolicyMapping
    }

    private constructor(seq: ASN1Sequence) {
        for (i in 0..seq.size() - 1) {
            val to = ASN1TaggedObject.getInstance(seq.getObjectAt(i))

            if (to.tagNo == 0) {
                requireExplicitPolicyMapping = ASN1Integer.getInstance(to, false).value
            } else if (to.tagNo == 1) {
                inhibitPolicyMapping = ASN1Integer.getInstance(to, false).value
            } else {
                throw IllegalArgumentException("Unknown tag encountered.")
            }
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (requireExplicitPolicyMapping != null) {
            v.add(DERTaggedObject(0, ASN1Integer(requireExplicitPolicyMapping)))
        }

        if (inhibitPolicyMapping != null) {
            v.add(DERTaggedObject(1, ASN1Integer(inhibitPolicyMapping)))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): PolicyConstraints? {
            if (obj is PolicyConstraints) {
                return obj
            }

            if (obj != null) {
                return PolicyConstraints(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun fromExtensions(extensions: Extensions): PolicyConstraints {
            return PolicyConstraints.getInstance(extensions.getExtensionParsedValue(Extension.policyConstraints))
        }
    }
}
