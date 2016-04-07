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

class RSAPrivateKey : ASN1Object {
    var version: BigInteger? = null
        private set
    var modulus: BigInteger? = null
        private set
    var publicExponent: BigInteger? = null
        private set
    var privateExponent: BigInteger? = null
        private set
    var prime1: BigInteger? = null
        private set
    var prime2: BigInteger? = null
        private set
    var exponent1: BigInteger? = null
        private set
    var exponent2: BigInteger? = null
        private set
    var coefficient: BigInteger? = null
        private set
    private var otherPrimeInfos: ASN1Sequence? = null

    constructor(
            modulus: BigInteger,
            publicExponent: BigInteger,
            privateExponent: BigInteger,
            prime1: BigInteger,
            prime2: BigInteger,
            exponent1: BigInteger,
            exponent2: BigInteger,
            coefficient: BigInteger) {
        this.version = BigInteger.valueOf(0)
        this.modulus = modulus
        this.publicExponent = publicExponent
        this.privateExponent = privateExponent
        this.prime1 = prime1
        this.prime2 = prime2
        this.exponent1 = exponent1
        this.exponent2 = exponent2
        this.coefficient = coefficient
    }

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        val v = (e.nextElement() as ASN1Integer).value
        if (v.toInt() != 0 && v.toInt() != 1) {
            throw IllegalArgumentException("wrong version for RSA private key")
        }

        version = v
        modulus = (e.nextElement() as ASN1Integer).value
        publicExponent = (e.nextElement() as ASN1Integer).value
        privateExponent = (e.nextElement() as ASN1Integer).value
        prime1 = (e.nextElement() as ASN1Integer).value
        prime2 = (e.nextElement() as ASN1Integer).value
        exponent1 = (e.nextElement() as ASN1Integer).value
        exponent2 = (e.nextElement() as ASN1Integer).value
        coefficient = (e.nextElement() as ASN1Integer).value

        if (e.hasMoreElements()) {
            otherPrimeInfos = e.nextElement() as ASN1Sequence
        }
    }

    /**
     * This outputs the key in PKCS1v2 format.
     *
     * RSAPrivateKey ::= SEQUENCE {
     * version Version,
     * modulus INTEGER, -- n
     * publicExponent INTEGER, -- e
     * privateExponent INTEGER, -- d
     * prime1 INTEGER, -- p
     * prime2 INTEGER, -- q
     * exponent1 INTEGER, -- d mod (p-1)
     * exponent2 INTEGER, -- d mod (q-1)
     * coefficient INTEGER, -- (inverse of q) mod p
     * otherPrimeInfos OtherPrimeInfos OPTIONAL
     * }

     * Version ::= INTEGER { two-prime(0), multi(1) }
     * (CONSTRAINED BY {-- version must be multi if otherPrimeInfos present --})
     *
     *
     *
     * This routine is written to output PKCS1 version 2.1, private keys.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(ASN1Integer(version))                       // version
        v.add(ASN1Integer(modulus))
        v.add(ASN1Integer(publicExponent))
        v.add(ASN1Integer(privateExponent))
        v.add(ASN1Integer(prime1))
        v.add(ASN1Integer(prime2))
        v.add(ASN1Integer(exponent1))
        v.add(ASN1Integer(exponent2))
        v.add(ASN1Integer(coefficient))

        if (otherPrimeInfos != null) {
            v.add(otherPrimeInfos)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): RSAPrivateKey {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): RSAPrivateKey? {
            if (obj is RSAPrivateKey) {
                return obj
            }

            if (obj != null) {
                return RSAPrivateKey(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
