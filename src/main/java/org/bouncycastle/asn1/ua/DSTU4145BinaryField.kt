package org.bouncycastle.asn1.ua

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class DSTU4145BinaryField : ASN1Object {

    var m: Int = 0
        private set
    var k1: Int = 0
        private set
    var k2: Int = 0
        private set
    var k3: Int = 0
        private set

    private constructor(seq: ASN1Sequence) {
        m = ASN1Integer.getInstance(seq.getObjectAt(0)).positiveValue.toInt()

        if (seq.getObjectAt(1) is ASN1Integer) {
            k1 = (seq.getObjectAt(1) as ASN1Integer).positiveValue.toInt()
        } else if (seq.getObjectAt(1) is ASN1Sequence) {
            val coefs = ASN1Sequence.getInstance(seq.getObjectAt(1))

            k1 = ASN1Integer.getInstance(coefs.getObjectAt(0)).positiveValue.toInt()
            k2 = ASN1Integer.getInstance(coefs.getObjectAt(1)).positiveValue.toInt()
            k3 = ASN1Integer.getInstance(coefs.getObjectAt(2)).positiveValue.toInt()
        } else {
            throw IllegalArgumentException("object parse error")
        }
    }

    constructor(m: Int, k1: Int, k2: Int, k3: Int) {
        this.m = m
        this.k1 = k1
        this.k2 = k2
        this.k3 = k3
    }

    constructor(m: Int, k: Int) : this(m, k, 0, 0) {
    }

    /**
     * BinaryField ::= SEQUENCE {
     * M INTEGER,
     * CHOICE {Trinomial,    Pentanomial}
     * Trinomial::= INTEGER
     * Pentanomial::= SEQUENCE {
     * k INTEGER,
     * j INTEGER,
     * l INTEGER}
     */
    override fun toASN1Primitive(): ASN1Primitive {

        val v = ASN1EncodableVector()

        v.add(ASN1Integer(m.toLong()))
        if (k2 == 0)
        //Trinomial
        {
            v.add(ASN1Integer(k1.toLong()))
        } else {
            val coefs = ASN1EncodableVector()
            coefs.add(ASN1Integer(k1.toLong()))
            coefs.add(ASN1Integer(k2.toLong()))
            coefs.add(ASN1Integer(k3.toLong()))

            v.add(DERSequence(coefs))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): DSTU4145BinaryField? {
            if (obj is DSTU4145BinaryField) {
                return obj
            }

            if (obj != null) {
                return DSTU4145BinaryField(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }

}
