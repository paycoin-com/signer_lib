package org.bouncycastle.asn1.eac

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence


/**
 * an Iso7816RSAPublicKeyStructure structure.
 *
 * Certificate Holder Authorization ::= SEQUENCE {
 * // modulus should be at least 1024bit and a multiple of 512.
 * DERTaggedObject        modulus,
 * // access rights    exponent
 * DERTaggedObject    accessRights,
 * }
 *
 */
class RSAPublicKey : PublicKeyDataObject {
    override var usage: ASN1ObjectIdentifier? = null
        private set(value: ASN1ObjectIdentifier?) {
            super.usage = value
        }
    var modulus: BigInteger? = null
        private set
    var publicExponent: BigInteger? = null
        private set
    private var valid = 0

    internal constructor(seq: ASN1Sequence) {
        val en = seq.objects

        this.usage = ASN1ObjectIdentifier.getInstance(en.nextElement())

        while (en.hasMoreElements()) {
            val `val` = UnsignedInteger.getInstance(en.nextElement())

            when (`val`.tagNo) {
                0x1 -> setModulus(`val`)
                0x2 -> setExponent(`val`)
                else -> throw IllegalArgumentException("Unknown DERTaggedObject :" + `val`.tagNo + "-> not an Iso7816RSAPublicKeyStructure")
            }
        }
        if (valid != 0x3) {
            throw IllegalArgumentException("missing argument -> not an Iso7816RSAPublicKeyStructure")
        }
    }

    constructor(usage: ASN1ObjectIdentifier, modulus: BigInteger, exponent: BigInteger) {
        this.usage = usage
        this.modulus = modulus
        this.publicExponent = exponent
    }

    private fun setModulus(modulus: UnsignedInteger) {
        if (valid and modulusValid == 0) {
            valid = valid or modulusValid
            this.modulus = modulus.value
        } else {
            throw IllegalArgumentException("Modulus already set")
        }
    }

    private fun setExponent(exponent: UnsignedInteger) {
        if (valid and exponentValid == 0) {
            valid = valid or exponentValid
            this.publicExponent = exponent.value
        } else {
            throw IllegalArgumentException("Exponent already set")
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(usage)
        v.add(UnsignedInteger(0x01, modulus))
        v.add(UnsignedInteger(0x02, publicExponent))

        return DERSequence(v)
    }

    companion object {
        private val modulusValid = 0x01
        private val exponentValid = 0x02
    }
}
