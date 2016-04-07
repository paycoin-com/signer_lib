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

class GOST3410ParamSetParameters : ASN1Object {

    var lKeySize: Int = 0
        internal set
    internal var p: ASN1Integer
    internal var q: ASN1Integer
    internal var a: ASN1Integer

    constructor(
            keySize: Int,
            p: BigInteger,
            q: BigInteger,
            a: BigInteger) {
        this.lKeySize = keySize
        this.p = ASN1Integer(p)
        this.q = ASN1Integer(q)
        this.a = ASN1Integer(a)
    }

    constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        lKeySize = (e.nextElement() as ASN1Integer).value.toInt()
        p = e.nextElement() as ASN1Integer
        q = e.nextElement() as ASN1Integer
        a = e.nextElement() as ASN1Integer
    }

    val keySize: Int
        get() = lKeySize

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

        v.add(ASN1Integer(lKeySize.toLong()))
        v.add(p)
        v.add(q)
        v.add(a)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): GOST3410ParamSetParameters {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): GOST3410ParamSetParameters {
            if (obj == null || obj is GOST3410ParamSetParameters) {
                return obj as GOST3410ParamSetParameters?
            }

            if (obj is ASN1Sequence) {
                return GOST3410ParamSetParameters(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("Invalid GOST3410Parameter: " + obj.javaClass.name)
        }
    }
}
