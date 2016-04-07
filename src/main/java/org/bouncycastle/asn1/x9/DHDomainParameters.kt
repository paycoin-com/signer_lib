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


@Deprecated("use DomainParameters")
class DHDomainParameters : ASN1Object {
    var p: ASN1Integer? = null
        private set
    var g: ASN1Integer? = null
        private set
    var q: ASN1Integer? = null
        private set
    var j: ASN1Integer? = null
        private set
    var validationParms: DHValidationParms? = null
        private set

    constructor(p: BigInteger?, g: BigInteger?, q: BigInteger?, j: BigInteger,
                validationParms: DHValidationParms) {
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
        this.j = ASN1Integer(j)
        this.validationParms = validationParms
    }

    constructor(p: ASN1Integer?, g: ASN1Integer?, q: ASN1Integer?, j: ASN1Integer,
                validationParms: DHValidationParms) {
        if (p == null) {
            throw IllegalArgumentException("'p' cannot be null")
        }
        if (g == null) {
            throw IllegalArgumentException("'g' cannot be null")
        }
        if (q == null) {
            throw IllegalArgumentException("'q' cannot be null")
        }

        this.p = p
        this.g = g
        this.q = q
        this.j = j
        this.validationParms = validationParms
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
        }

        if (next != null) {
            this.validationParms = DHValidationParms.getInstance(next.toASN1Primitive())
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.p)
        v.add(this.g)
        v.add(this.q)

        if (this.j != null) {
            v.add(this.j)
        }

        if (this.validationParms != null) {
            v.add(this.validationParms)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: ASN1TaggedObject, explicit: Boolean): DHDomainParameters {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(obj: Any?): DHDomainParameters {
            if (obj == null || obj is DHDomainParameters) {
                return obj as DHDomainParameters?
            }

            if (obj is ASN1Sequence) {
                return DHDomainParameters(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("Invalid DHDomainParameters: " + obj.javaClass.name)
        }

        private fun getNext(e: Enumeration<Any>): ASN1Encodable? {
            return if (e.hasMoreElements()) e.nextElement() as ASN1Encodable else null
        }
    }
}
