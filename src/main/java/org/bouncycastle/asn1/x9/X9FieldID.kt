package org.bouncycastle.asn1.x9

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * ASN.1 def for Elliptic-Curve Field ID structure. See
 * X9.62, for further details.
 */
class X9FieldID : ASN1Object, X9ObjectIdentifiers {
    var identifier: ASN1ObjectIdentifier? = null
        private set
    var parameters: ASN1Primitive? = null
        private set

    /**
     * Constructor for elliptic curves over prime fields
     * `F2`.
     * @param primeP The prime `p` defining the prime field.
     */
    constructor(primeP: BigInteger) {
        this.identifier = X9ObjectIdentifiers.prime_field
        this.parameters = ASN1Integer(primeP)
    }

    /**
     * Constructor for elliptic curves over binary fields
     * `F2m`.
     * @param m  The exponent `m` of
     * * `F2m`.
     * *
     * @param k1 The integer `k1` where `xm +
     * * xk3 + xk2 + xk1 + 1`
     * * represents the reduction polynomial `f(z)`.
     * *
     * @param k2 The integer `k2` where `xm +
     * * xk3 + xk2 + xk1 + 1`
     * * represents the reduction polynomial `f(z)`.
     * *
     * @param k3 The integer `k3` where `xm +
     * * xk3 + xk2 + xk1 + 1`
     * * represents the reduction polynomial `f(z)`..
     */
    @JvmOverloads constructor(m: Int, k1: Int, k2: Int = 0, k3: Int = 0) {
        this.identifier = X9ObjectIdentifiers.characteristic_two_field
        val fieldIdParams = ASN1EncodableVector()
        fieldIdParams.add(ASN1Integer(m.toLong()))

        if (k2 == 0) {
            if (k3 != 0) {
                throw IllegalArgumentException("inconsistent k values")
            }

            fieldIdParams.add(X9ObjectIdentifiers.tpBasis)
            fieldIdParams.add(ASN1Integer(k1.toLong()))
        } else {
            if (k2 <= k1 || k3 <= k2) {
                throw IllegalArgumentException("inconsistent k values")
            }

            fieldIdParams.add(X9ObjectIdentifiers.ppBasis)
            val pentanomialParams = ASN1EncodableVector()
            pentanomialParams.add(ASN1Integer(k1.toLong()))
            pentanomialParams.add(ASN1Integer(k2.toLong()))
            pentanomialParams.add(ASN1Integer(k3.toLong()))
            fieldIdParams.add(DERSequence(pentanomialParams))
        }

        this.parameters = DERSequence(fieldIdParams)
    }

    private constructor(
            seq: ASN1Sequence) {
        this.identifier = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
        this.parameters = seq.getObjectAt(1).toASN1Primitive()
    }

    /**
     * Produce a DER encoding of the following structure.
     *
     * FieldID ::= SEQUENCE {
     * fieldType       FIELD-ID.&amp;id({IOSet}),
     * parameters      FIELD-ID.&amp;Type({IOSet}{&#64;fieldType})
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(this.identifier)
        v.add(this.parameters)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): X9FieldID? {
            if (obj is X9FieldID) {
                return obj
            }

            if (obj != null) {
                return X9FieldID(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
/**
 * Constructor for elliptic curves over binary fields
 * `F2m`.
 * @param m  The exponent `m` of
 * * `F2m`.
 * *
 * @param k1 The integer `k1` where `xm +
 * * xk1 + 1`
 * * represents the reduction polynomial `f(z)`.
 */
