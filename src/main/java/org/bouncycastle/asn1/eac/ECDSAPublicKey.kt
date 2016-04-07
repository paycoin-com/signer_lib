package org.bouncycastle.asn1.eac

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * an Iso7816ECDSAPublicKeyStructure structure.
 *
 * Certificate Holder Authorization ::= SEQUENCE {
 * ASN1TaggedObject primeModulusP;        // OPTIONAL
 * ASN1TaggedObject firstCoefA;            // OPTIONAL
 * ASN1TaggedObject secondCoefB;        // OPTIONAL
 * ASN1TaggedObject basePointG;            // OPTIONAL
 * ASN1TaggedObject orderOfBasePointR;    // OPTIONAL
 * ASN1TaggedObject publicPointY;        //REQUIRED
 * ASN1TaggedObject    cofactorF;            // OPTIONAL
 * }
 *
 */
class ECDSAPublicKey : PublicKeyDataObject {
    override var usage: ASN1ObjectIdentifier? = null
        private set(value: ASN1ObjectIdentifier?) {
            super.usage = value
        }
    private var primeModulusP: BigInteger? = null        // OPTIONAL
    private var firstCoefA: BigInteger? = null            // OPTIONAL
    private var secondCoefB: BigInteger? = null        // OPTIONAL
    private var basePointG: ByteArray? = null            // OPTIONAL
    private var orderOfBasePointR: BigInteger? = null    // OPTIONAL
    private var publicPointY: ByteArray? = null        //REQUIRED
    private var cofactorF: BigInteger? = null            // OPTIONAL
    private var options: Int = 0

    @Throws(IllegalArgumentException::class)
    internal constructor(seq: ASN1Sequence) {
        val en = seq.objects

        this.usage = ASN1ObjectIdentifier.getInstance(en.nextElement())

        options = 0
        while (en.hasMoreElements()) {
            val obj = en.nextElement()

            if (obj is ASN1TaggedObject) {
                when (obj.tagNo) {
                    0x1 -> setPrimeModulusP(UnsignedInteger.getInstance(obj)!!.value)
                    0x2 -> setFirstCoefA(UnsignedInteger.getInstance(obj)!!.value)
                    0x3 -> setSecondCoefB(UnsignedInteger.getInstance(obj)!!.value)
                    0x4 -> setBasePointG(ASN1OctetString.getInstance(obj, false))
                    0x5 -> setOrderOfBasePointR(UnsignedInteger.getInstance(obj)!!.value)
                    0x6 -> setPublicPointY(ASN1OctetString.getInstance(obj, false))
                    0x7 -> setCofactorF(UnsignedInteger.getInstance(obj)!!.value)
                    else -> {
                        options = 0
                        throw IllegalArgumentException("Unknown Object Identifier!")
                    }
                }
            } else {
                throw IllegalArgumentException("Unknown Object Identifier!")
            }
        }
        if (options != 0x20 && options != 0x7F) {
            throw IllegalArgumentException("All options must be either present or absent!")
        }
    }

    @Throws(IllegalArgumentException::class)
    constructor(usage: ASN1ObjectIdentifier, ppY: ByteArray) {
        this.usage = usage
        setPublicPointY(DEROctetString(ppY))
    }

    constructor(usage: ASN1ObjectIdentifier, p: BigInteger, a: BigInteger, b: BigInteger, basePoint: ByteArray, order: BigInteger, publicPoint: ByteArray, cofactor: Int) {
        this.usage = usage
        setPrimeModulusP(p)
        setFirstCoefA(a)
        setSecondCoefB(b)
        setBasePointG(DEROctetString(basePoint))
        setOrderOfBasePointR(order)
        setPublicPointY(DEROctetString(publicPoint))
        setCofactorF(BigInteger.valueOf(cofactor.toLong()))
    }

    fun getBasePointG(): ByteArray? {
        if (options and G != 0) {
            return basePointG
        } else {
            return null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun setBasePointG(basePointG: ASN1OctetString) {
        if (options and G == 0) {
            options = options or G
            this.basePointG = basePointG.octets
        } else {
            throw IllegalArgumentException("Base Point G already set")
        }
    }

    fun getCofactorF(): BigInteger? {
        if (options and F != 0) {
            return cofactorF
        } else {
            return null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun setCofactorF(cofactorF: BigInteger) {
        if (options and F == 0) {
            options = options or F
            this.cofactorF = cofactorF
        } else {
            throw IllegalArgumentException("Cofactor F already set")
        }
    }

    fun getFirstCoefA(): BigInteger? {
        if (options and A != 0) {
            return firstCoefA
        } else {
            return null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun setFirstCoefA(firstCoefA: BigInteger) {
        if (options and A == 0) {
            options = options or A
            this.firstCoefA = firstCoefA
        } else {
            throw IllegalArgumentException("First Coef A already set")
        }
    }

    fun getOrderOfBasePointR(): BigInteger? {
        if (options and R != 0) {
            return orderOfBasePointR
        } else {
            return null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun setOrderOfBasePointR(orderOfBasePointR: BigInteger) {
        if (options and R == 0) {
            options = options or R
            this.orderOfBasePointR = orderOfBasePointR
        } else {
            throw IllegalArgumentException("Order of base point R already set")
        }
    }

    fun getPrimeModulusP(): BigInteger? {
        if (options and P != 0) {
            return primeModulusP
        } else {
            return null
        }
    }

    private fun setPrimeModulusP(primeModulusP: BigInteger) {
        if (options and P == 0) {
            options = options or P
            this.primeModulusP = primeModulusP
        } else {
            throw IllegalArgumentException("Prime Modulus P already set")
        }
    }

    fun getPublicPointY(): ByteArray? {
        if (options and Y != 0) {
            return publicPointY
        } else {
            return null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun setPublicPointY(publicPointY: ASN1OctetString) {
        if (options and Y == 0) {
            options = options or Y
            this.publicPointY = publicPointY.octets
        } else {
            throw IllegalArgumentException("Public Point Y already set")
        }
    }

    fun getSecondCoefB(): BigInteger? {
        if (options and B != 0) {
            return secondCoefB
        } else {
            return null
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun setSecondCoefB(secondCoefB: BigInteger) {
        if (options and B == 0) {
            options = options or B
            this.secondCoefB = secondCoefB
        } else {
            throw IllegalArgumentException("Second Coef B already set")
        }
    }

    fun hasParameters(): Boolean {
        return primeModulusP != null
    }

    fun getASN1EncodableVector(oid: ASN1ObjectIdentifier, publicPointOnly: Boolean): ASN1EncodableVector {
        val v = ASN1EncodableVector()
        v.add(oid)

        if (!publicPointOnly) {
            v.add(UnsignedInteger(0x01, getPrimeModulusP()))
            v.add(UnsignedInteger(0x02, getFirstCoefA()))
            v.add(UnsignedInteger(0x03, getSecondCoefB()))
            v.add(DERTaggedObject(false, 0x04, DEROctetString(getBasePointG())))
            v.add(UnsignedInteger(0x05, getOrderOfBasePointR()))
        }
        v.add(DERTaggedObject(false, 0x06, DEROctetString(getPublicPointY())))
        if (!publicPointOnly) {
            v.add(UnsignedInteger(0x07, getCofactorF()))
        }

        return v
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return DERSequence(getASN1EncodableVector(usage, false))
    }

    companion object {
        private val P = 0x01
        private val A = 0x02
        private val B = 0x04
        private val G = 0x08
        private val R = 0x10
        private val Y = 0x20
        private val F = 0x40
    }
}
