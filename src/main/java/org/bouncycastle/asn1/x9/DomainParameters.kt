package org.bouncycastle.asn1.x9

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

/**
 * X9.44 Diffie-Hellman domain parameters.
 *
 * DomainParameters ::= SEQUENCE {
 * p                INTEGER,           -- odd prime, p=jq +1
 * g                INTEGER,           -- generator, g
 * q                INTEGER,           -- factor of p-1
 * j                INTEGER OPTIONAL,  -- subgroup factor, j>= 2
 * validationParams  ValidationParams OPTIONAL
 * }
 *
 */
class DomainParameters : ASN1Object {
    private val p: ASN1Integer
    private val g: ASN1Integer
    private val q: ASN1Integer
    private val j: ASN1Integer?
    /**
     * Return the validation parameters for this set (if present).

     * @return validation parameters, or null if absent.
     */
    val validationParams: ValidationParams?

    /**
     * Base constructor - the full domain parameter set.

     * @param p the prime p defining the Galois field.
     * *
     * @param g the generator of the multiplicative subgroup of order g.
     * *
     * @param q specifies the prime factor of p - 1
     * *
     * @param j optionally specifies the value that satisfies the equation p = jq+1
     * *
     * @param validationParams parameters for validating these domain parameters.
     */
    constructor(p: BigInteger?, g: BigInteger?, q: BigInteger?, j: BigInteger?,
                validationParams: ValidationParams) {
        if (p == null) {
            throw IllegalArgumentException("'p' cannot be null")
        }
        if (g == null) {
            throw IllegalArgumentException("'g' cannot be null")
        }
        if (q == null) {
            throw IllegalArgumentException("'q' cannot be null")
        }

        this.p = ASN1Integer(p)
        this.g = ASN1Integer(g)
        this.q = ASN1Integer(q)

        if (j != null) {
            this.j = ASN1Integer(j)
        } else {
            this.j = null
        }
        this.validationParams = validationParams
    }

    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 3 || seq.size() > 5) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val e = seq.objects
        this.p = ASN1Integer.getInstance(e.nextElement())
        this.g = ASN1Integer.getInstance(e.nextElement())
        this.q = ASN1Integer.getInstance(e.nextElement())

        var next = getNext(e)

        if (next != null && next is ASN1Integer) {
            this.j = ASN1Integer.getInstance(next)
            next = getNext(e)
        } else {
            this.j = null
        }

        if (next != null) {
            this.validationParams = ValidationParams.getInstance(next.toASN1Primitive())
        } else {
            this.validationParams = null
        }
    }

    /**
     * Return the prime p defining the Galois field.

     * @return the prime p.
     */
    fun getP(): BigInteger {
        return this.p.positiveValue
    }

    /**
     * Return the generator of the multiplicative subgroup of order g.

     * @return the generator g.
     */
    fun getG(): BigInteger {
        return this.g.positiveValue
    }

    /**
     * Return q, the prime factor of p - 1

     * @return q value
     */
    fun getQ(): BigInteger {
        return this.q.positiveValue
    }

    /**
     * Return the value that satisfies the equation p = jq+1 (if present).

     * @return j value or null.
     */
    fun getJ(): BigInteger? {
        if (this.j == null) {
            return null
        }

        return this.j.positiveValue
    }

    /**
     * Return an ASN.1 primitive representation of this object.

     * @return a DERSequence containing the parameter values.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.p)
        v.add(this.g)
        v.add(this.q)

        if (this.j != null) {
            v.add(this.j)
        }

        if (this.validationParams != null) {
            v.add(this.validationParams)
        }

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a DomainParameters object from the passed in tagged object.

         * @param obj a tagged object.
         * *
         * @param explicit true if the contents of the object is explictly tagged, false otherwise.
         * *
         * @return a DomainParameters
         */
        fun getInstance(obj: ASN1TaggedObject, explicit: Boolean): DomainParameters {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return a DomainParameters object from the passed in object.

         * @param obj an object for conversion or a byte[].
         * *
         * @return a DomainParameters
         */
        fun getInstance(obj: Any?): DomainParameters? {
            if (obj is DomainParameters) {
                return obj
            } else if (obj != null) {
                return DomainParameters(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        private fun getNext(e: Enumeration<Any>): ASN1Encodable? {
            return if (e.hasMoreElements()) e.nextElement() as ASN1Encodable else null
        }
    }
}