package org.bouncycastle.asn1.x9

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.field.PolynomialExtensionField

/**
 * ASN.1 def for Elliptic-Curve ECParameters structure. See
 * X9.62, for further details.
 */
class X9ECParameters : ASN1Object, X9ObjectIdentifiers {

    /**
     * Return the ASN.1 entry representing the FieldID.

     * @return the X9FieldID for the FieldID in these parameters.
     */
    var fieldIDEntry: X9FieldID? = null
        private set
    var curve: ECCurve? = null
        private set
    /**
     * Return the ASN.1 entry representing the base point G.

     * @return the X9ECPoint for the base point in these parameters.
     */
    var baseEntry: X9ECPoint? = null
        private set
    var n: BigInteger? = null
        private set
    var h: BigInteger? = null
        private set
    var seed: ByteArray? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.getObjectAt(0) !is ASN1Integer || (seq.getObjectAt(0) as ASN1Integer).value != ONE) {
            throw IllegalArgumentException("bad version in X9ECParameters")
        }

        val x9c = X9Curve(
                X9FieldID.getInstance(seq.getObjectAt(1)),
                ASN1Sequence.getInstance(seq.getObjectAt(2)))

        this.curve = x9c.curve
        val p = seq.getObjectAt(3)

        if (p is X9ECPoint) {
            this.baseEntry = p
        } else {
            this.baseEntry = X9ECPoint(curve, p as ASN1OctetString)
        }

        this.n = (seq.getObjectAt(4) as ASN1Integer).value
        this.seed = x9c.seed

        if (seq.size() == 6) {
            this.h = (seq.getObjectAt(5) as ASN1Integer).value
        }
    }

    @JvmOverloads constructor(
            curve: ECCurve,
            g: ECPoint,
            n: BigInteger,
            h: BigInteger? = null,
            seed: ByteArray? = null) : this(curve, X9ECPoint(g), n, h, seed) {
    }

    @JvmOverloads constructor(
            curve: ECCurve,
            g: X9ECPoint,
            n: BigInteger,
            h: BigInteger,
            seed: ByteArray? = null) {
        this.curve = curve
        this.baseEntry = g
        this.n = n
        this.h = h
        this.seed = seed

        if (ECAlgorithms.isFpCurve(curve)) {
            this.fieldIDEntry = X9FieldID(curve.field.characteristic)
        } else if (ECAlgorithms.isF2mCurve(curve)) {
            val field = curve.field as PolynomialExtensionField
            val exponents = field.minimalPolynomial.exponentsPresent
            if (exponents.size == 3) {
                this.fieldIDEntry = X9FieldID(exponents[2], exponents[1])
            } else if (exponents.size == 5) {
                this.fieldIDEntry = X9FieldID(exponents[4], exponents[1], exponents[2], exponents[3])
            } else {
                throw IllegalArgumentException("Only trinomial and pentomial curves are supported")
            }
        } else {
            throw IllegalArgumentException("'curve' is of an unsupported type")
        }
    }

    val g: ECPoint
        get() = baseEntry!!.point

    /**
     * Return the ASN.1 entry representing the Curve.

     * @return the X9Curve for the curve in these parameters.
     */
    val curveEntry: X9Curve
        get() = X9Curve(curve, seed)

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * ECParameters ::= SEQUENCE {
     * version         INTEGER { ecpVer1(1) } (ecpVer1),
     * fieldID         FieldID {{FieldTypes}},
     * curve           X9Curve,
     * base            X9ECPoint,
     * order           INTEGER,
     * cofactor        INTEGER OPTIONAL
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(ASN1Integer(ONE))
        v.add(fieldIDEntry)
        v.add(X9Curve(curve, seed))
        v.add(baseEntry)
        v.add(ASN1Integer(n))

        if (h != null) {
            v.add(ASN1Integer(h))
        }

        return DERSequence(v)
    }

    companion object {
        private val ONE = BigInteger.valueOf(1)

        fun getInstance(obj: Any?): X9ECParameters? {
            if (obj is X9ECParameters) {
                return obj
            }

            if (obj != null) {
                return X9ECParameters(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
