package org.bouncycastle.asn1.x9

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECCurve

/**
 * ASN.1 def for Elliptic-Curve Curve structure. See
 * X9.62, for further details.
 */
class X9Curve : ASN1Object, X9ObjectIdentifiers {
    var curve: ECCurve? = null
        private set
    var seed: ByteArray? = null
        private set
    private var fieldIdentifier: ASN1ObjectIdentifier? = null

    constructor(
            curve: ECCurve) {
        this.curve = curve
        this.seed = null
        setFieldIdentifier()
    }

    constructor(
            curve: ECCurve,
            seed: ByteArray) {
        this.curve = curve
        this.seed = seed
        setFieldIdentifier()
    }

    constructor(
            fieldID: X9FieldID,
            seq: ASN1Sequence) {
        // TODO Is it possible to get the order(n) and cofactor(h) too?

        fieldIdentifier = fieldID.identifier
        if (fieldIdentifier == X9ObjectIdentifiers.prime_field) {
            val p = (fieldID.parameters as ASN1Integer).value
            val x9A = X9FieldElement(p, seq.getObjectAt(0) as ASN1OctetString)
            val x9B = X9FieldElement(p, seq.getObjectAt(1) as ASN1OctetString)
            curve = ECCurve.Fp(p, x9A.value.toBigInteger(), x9B.value.toBigInteger())
        } else if (fieldIdentifier == X9ObjectIdentifiers.characteristic_two_field) {
            // Characteristic two field
            val parameters = ASN1Sequence.getInstance(fieldID.parameters)
            val m = (parameters.getObjectAt(0) as ASN1Integer).value.toInt()
            val representation = parameters.getObjectAt(1) as ASN1ObjectIdentifier

            var k1 = 0
            var k2 = 0
            var k3 = 0

            if (representation == X9ObjectIdentifiers.tpBasis) {
                // Trinomial basis representation
                k1 = ASN1Integer.getInstance(parameters.getObjectAt(2)).value.toInt()
            } else if (representation == X9ObjectIdentifiers.ppBasis) {
                // Pentanomial basis representation
                val pentanomial = ASN1Sequence.getInstance(parameters.getObjectAt(2))
                k1 = ASN1Integer.getInstance(pentanomial.getObjectAt(0)).value.toInt()
                k2 = ASN1Integer.getInstance(pentanomial.getObjectAt(1)).value.toInt()
                k3 = ASN1Integer.getInstance(pentanomial.getObjectAt(2)).value.toInt()
            } else {
                throw IllegalArgumentException("This type of EC basis is not implemented")
            }
            val x9A = X9FieldElement(m, k1, k2, k3, seq.getObjectAt(0) as ASN1OctetString)
            val x9B = X9FieldElement(m, k1, k2, k3, seq.getObjectAt(1) as ASN1OctetString)
            curve = ECCurve.F2m(m, k1, k2, k3, x9A.value.toBigInteger(), x9B.value.toBigInteger())
        } else {
            throw IllegalArgumentException("This type of ECCurve is not implemented")
        }

        if (seq.size() == 3) {
            seed = (seq.getObjectAt(2) as DERBitString).bytes
        }
    }

    private fun setFieldIdentifier() {
        if (ECAlgorithms.isFpCurve(curve)) {
            fieldIdentifier = X9ObjectIdentifiers.prime_field
        } else if (ECAlgorithms.isF2mCurve(curve)) {
            fieldIdentifier = X9ObjectIdentifiers.characteristic_two_field
        } else {
            throw IllegalArgumentException("This type of ECCurve is not implemented")
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * Curve ::= SEQUENCE {
     * a               FieldElement,
     * b               FieldElement,
     * seed            BIT STRING      OPTIONAL
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (fieldIdentifier == X9ObjectIdentifiers.prime_field) {
            v.add(X9FieldElement(curve!!.a).toASN1Primitive())
            v.add(X9FieldElement(curve!!.b).toASN1Primitive())
        } else if (fieldIdentifier == X9ObjectIdentifiers.characteristic_two_field) {
            v.add(X9FieldElement(curve!!.a).toASN1Primitive())
            v.add(X9FieldElement(curve!!.b).toASN1Primitive())
        }

        if (seed != null) {
            v.add(DERBitString(seed))
        }

        return DERSequence(v)
    }
}
