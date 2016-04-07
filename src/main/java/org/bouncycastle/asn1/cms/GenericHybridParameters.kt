package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * RFC 5990 GenericHybridParameters class.
 *
 * GenericHybridParameters ::= SEQUENCE {
 * kem  KeyEncapsulationMechanism,
 * dem  DataEncapsulationMechanism
 * }

 * KeyEncapsulationMechanism ::= AlgorithmIdentifier {{KEMAlgorithms}}
 * DataEncapsulationMechanism ::= AlgorithmIdentifier {{DEMAlgorithms}}
 *
 */
class GenericHybridParameters : ASN1Object {
    val kem: AlgorithmIdentifier
    val dem: AlgorithmIdentifier

    private constructor(sequence: ASN1Sequence) {
        if (sequence.size() != 2) {
            throw IllegalArgumentException("ASN.1 SEQUENCE should be of length 2")
        }

        this.kem = AlgorithmIdentifier.getInstance(sequence.getObjectAt(0))
        this.dem = AlgorithmIdentifier.getInstance(sequence.getObjectAt(1))
    }

    constructor(kem: AlgorithmIdentifier, dem: AlgorithmIdentifier) {
        this.kem = kem
        this.dem = dem
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(kem)
        v.add(dem)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                o: Any?): GenericHybridParameters? {
            if (o is GenericHybridParameters) {
                return o
            } else if (o != null) {
                return GenericHybridParameters(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
