package org.bouncycastle.asn1.pkcs

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class DHParameter : ASN1Object {
    internal var p: ASN1Integer
    internal var g: ASN1Integer
    internal var l: ASN1Integer? = null

    constructor(
            p: BigInteger,
            g: BigInteger,
            l: Int) {
        this.p = ASN1Integer(p)
        this.g = ASN1Integer(g)

        if (l != 0) {
            this.l = ASN1Integer(l.toLong())
        } else {
            this.l = null
        }
    }

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        p = ASN1Integer.getInstance(e.nextElement())
        g = ASN1Integer.getInstance(e.nextElement())

        if (e.hasMoreElements()) {
            l = e.nextElement() as ASN1Integer
        } else {
            l = null
        }
    }

    fun getP(): BigInteger {
        return p.positiveValue
    }

    fun getG(): BigInteger {
        return g.positiveValue
    }

    fun getL(): BigInteger? {
        if (l == null) {
            return null
        }

        return l!!.positiveValue
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(p)
        v.add(g)

        if (this.getL() != null) {
            v.add(l)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): DHParameter? {
            if (obj is DHParameter) {
                return obj
            }

            if (obj != null) {
                return DHParameter(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
