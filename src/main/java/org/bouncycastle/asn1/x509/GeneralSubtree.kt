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
 * Class for containing a restriction object subtrees in NameConstraints. See
 * RFC 3280.

 *

 * GeneralSubtree ::= SEQUENCE
 * {
 * base                    GeneralName,
 * minimum         [0]     BaseDistance DEFAULT 0,
 * maximum         [1]     BaseDistance OPTIONAL
 * }
 *

 * @see org.bouncycastle.asn1.x509.NameConstraints
 */
class GeneralSubtree : ASN1Object {

    var base: GeneralName? = null
        private set

    private var minimum: ASN1Integer? = null

    private var maximum: ASN1Integer? = null

    private constructor(
            seq: ASN1Sequence) {
        base = GeneralName.getInstance(seq.getObjectAt(0))

        when (seq.size()) {
            1 -> {
            }
            2 -> {
                val o = ASN1TaggedObject.getInstance(seq.getObjectAt(1))
                when (o.tagNo) {
                    0 -> minimum = ASN1Integer.getInstance(o, false)
                    1 -> maximum = ASN1Integer.getInstance(o, false)
                    else -> throw IllegalArgumentException("Bad tag number: " + o.tagNo)
                }
            }
            3 -> {
                run {
                    val oMin = ASN1TaggedObject.getInstance(seq.getObjectAt(1))
                    if (oMin.tagNo != 0) {
                        throw IllegalArgumentException("Bad tag number for 'minimum': " + oMin.tagNo)
                    }
                    minimum = ASN1Integer.getInstance(oMin, false)
                }

                run {
                    val oMax = ASN1TaggedObject.getInstance(seq.getObjectAt(2))
                    if (oMax.tagNo != 1) {
                        throw IllegalArgumentException("Bad tag number for 'maximum': " + oMax.tagNo)
                    }
                    maximum = ASN1Integer.getInstance(oMax, false)
                }
            }
            else -> throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
    }

    /**
     * Constructor from a given details.

     * According RFC 3280, the minimum and maximum fields are not used with any
     * name forms, thus minimum MUST be zero, and maximum MUST be absent.
     *
     *
     * If minimum is `null`, zero is assumed, if
     * maximum is `null`, maximum is absent.

     * @param base
     * *            A restriction.
     * *
     * @param minimum
     * *            Minimum
     * *
     * *
     * @param maximum
     * *            Maximum
     */
    @JvmOverloads constructor(
            base: GeneralName,
            minimum: BigInteger? = null,
            maximum: BigInteger? = null) {
        this.base = base
        if (maximum != null) {
            this.maximum = ASN1Integer(maximum)
        }
        if (minimum == null) {
            this.minimum = null
        } else {
            this.minimum = ASN1Integer(minimum)
        }
    }

    fun getMinimum(): BigInteger {
        if (minimum == null) {
            return ZERO
        }

        return minimum!!.value
    }

    fun getMaximum(): BigInteger? {
        if (maximum == null) {
            return null
        }

        return maximum!!.value
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.

     * Returns:

     *
     * GeneralSubtree ::= SEQUENCE
     * {
     * base                    GeneralName,
     * minimum         [0]     BaseDistance DEFAULT 0,
     * maximum         [1]     BaseDistance OPTIONAL
     * }
     *

     * @return a ASN1Primitive
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(base)

        if (minimum != null && minimum!!.value != ZERO) {
            v.add(DERTaggedObject(false, 0, minimum))
        }

        if (maximum != null) {
            v.add(DERTaggedObject(false, 1, maximum))
        }

        return DERSequence(v)
    }

    companion object {
        private val ZERO = BigInteger.valueOf(0)

        fun getInstance(
                o: ASN1TaggedObject,
                explicit: Boolean): GeneralSubtree {
            return GeneralSubtree(ASN1Sequence.getInstance(o, explicit))
        }

        fun getInstance(
                obj: Any?): GeneralSubtree? {
            if (obj == null) {
                return null
            }

            if (obj is GeneralSubtree) {
                return obj
            }

            return GeneralSubtree(ASN1Sequence.getInstance(obj))
        }
    }
}
