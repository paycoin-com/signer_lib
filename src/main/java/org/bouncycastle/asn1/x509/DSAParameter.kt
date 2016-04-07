package org.bouncycastle.asn1.x509

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class DSAParameter : ASN1Object {
    internal var p: ASN1Integer
    internal var q: ASN1Integer
    internal var g: ASN1Integer

    constructor(
            p: BigInteger,
            q: BigInteger,
            g: BigInteger) {
        this.p = ASN1Integer(p)
        this.q = ASN1Integer(q)
        this.g = ASN1Integer(g)
    }

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val e = seq.objects

        p = ASN1Integer.getInstance(e.nextElement())
        q = ASN1Integer.getInstance(e.nextElement())
        g = ASN1Integer.getInstance(e.nextElement())
    }

    fun getP(): BigInteger {
        return p.positiveValue
    }

    fun getQ(): BigInteger {
        return q.positiveValue
    }

    fun getG(): BigInteger {
        return g.positiveValue
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(p)
        v.add(q)
        v.add(g)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DSAParameter {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): DSAParameter? {
            if (obj is DSAParameter) {
                return obj
            }

            if (obj != null) {
                return DSAParameter(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
