package org.bouncycastle.asn1.oiw

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class ElGamalParameter : ASN1Object {
    internal var p: ASN1Integer
    internal var g: ASN1Integer

    constructor(
            p: BigInteger,
            g: BigInteger) {
        this.p = ASN1Integer(p)
        this.g = ASN1Integer(g)
    }

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        p = e.nextElement() as ASN1Integer
        g = e.nextElement() as ASN1Integer
    }

    fun getP(): BigInteger {
        return p.positiveValue
    }

    fun getG(): BigInteger {
        return g.positiveValue
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(p)
        v.add(g)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): ElGamalParameter? {
            if (o is ElGamalParameter) {
                return o
            } else if (o != null) {
                return ElGamalParameter(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
