package org.bouncycastle.asn1.pkcs

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class RSAPublicKey : ASN1Object {
    var modulus: BigInteger? = null
        private set
    var publicExponent: BigInteger? = null
        private set

    constructor(
            modulus: BigInteger,
            publicExponent: BigInteger) {
        this.modulus = modulus
        this.publicExponent = publicExponent
    }

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val e = seq.objects

        modulus = ASN1Integer.getInstance(e.nextElement()).positiveValue
        publicExponent = ASN1Integer.getInstance(e.nextElement()).positiveValue
    }

    /**
     * This outputs the key in PKCS1v2 format.
     *
     * RSAPublicKey ::= SEQUENCE {
     * modulus INTEGER, -- n
     * publicExponent INTEGER, -- e
     * }
     *
     *
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(ASN1Integer(modulus))
        v.add(ASN1Integer(publicExponent))

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): RSAPublicKey {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): RSAPublicKey? {
            if (obj is RSAPublicKey) {
                return obj
            }

            if (obj != null) {
                return RSAPublicKey(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
