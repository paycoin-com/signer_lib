package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class AlgorithmIdentifier : ASN1Object {
    var algorithm: ASN1ObjectIdentifier? = null
        private set
    var parameters: ASN1Encodable? = null
        private set

    constructor(
            algorithm: ASN1ObjectIdentifier) {
        this.algorithm = algorithm
    }

    constructor(
            algorithm: ASN1ObjectIdentifier,
            parameters: ASN1Encodable) {
        this.algorithm = algorithm
        this.parameters = parameters
    }

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        algorithm = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))

        if (seq.size() == 2) {
            parameters = seq.getObjectAt(1)
        } else {
            parameters = null
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * AlgorithmIdentifier ::= SEQUENCE {
     * algorithm OBJECT IDENTIFIER,
     * parameters ANY DEFINED BY algorithm OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(algorithm)

        if (parameters != null) {
            v.add(parameters)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): AlgorithmIdentifier {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): AlgorithmIdentifier? {
            if (obj is AlgorithmIdentifier) {
                return obj
            } else if (obj != null) {
                return AlgorithmIdentifier(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
