package org.bouncycastle.asn1.cryptopro

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class ECGOST3410ParamSetParameters : ASN1Object {
    internal var p: ASN1Integer
    internal var q: ASN1Integer
    internal var a: ASN1Integer
    internal var b: ASN1Integer
    internal var x: ASN1Integer
    internal var y: ASN1Integer

    constructor(
            a: BigInteger,
            b: BigInteger,
            p: BigInteger,
            q: BigInteger,
            x: Int,
            y: BigInteger) {
        this.a = ASN1Integer(a)
        this.b = ASN1Integer(b)
        this.p = ASN1Integer(p)
        this.q = ASN1Integer(q)
        this.x = ASN1Integer(x.toLong())
        this.y = ASN1Integer(y)
    }

    constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        a = e.nextElement() as ASN1Integer
        b = e.nextElement() as ASN1Integer
        p = e.nextElement() as ASN1Integer
        q = e.nextElement() as ASN1Integer
        x = e.nextElement() as ASN1Integer
        y = e.nextElement() as ASN1Integer
    }

    fun getP(): BigInteger {
        return p.positiveValue
    }

    fun getQ(): BigInteger {
        return q.positiveValue
    }

    fun getA(): BigInteger {
        return a.positiveValue
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(a)
        v.add(b)
        v.add(p)
        v.add(q)
        v.add(x)
        v.add(y)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ECGOST3410ParamSetParameters {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): ECGOST3410ParamSetParameters {
            if (obj == null || obj is ECGOST3410ParamSetParameters) {
                return obj as ECGOST3410ParamSetParameters?
            }

            if (obj is ASN1Sequence) {
                return ECGOST3410ParamSetParameters(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("Invalid GOST3410Parameter: " + obj.javaClass.name)
        }
    }
}
