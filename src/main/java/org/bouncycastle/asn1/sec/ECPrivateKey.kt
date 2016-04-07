package org.bouncycastle.asn1.sec

import java.math.BigInteger
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.util.BigIntegers

/**
 * the elliptic curve private key object from SEC 1
 */
class ECPrivateKey : ASN1Object {
    private var seq: ASN1Sequence? = null

    private constructor(
            seq: ASN1Sequence) {
        this.seq = seq
    }


    @Deprecated("use constructor which takes orderBitLength to guarantee correct encoding.")
    constructor(
            key: BigInteger) : this(key.bitLength(), key) {
    }

    /**
     * Base constructor.

     * @param orderBitLength the bitLength of the order of the curve.
     * *
     * @param key the private key value.
     */
    constructor(
            orderBitLength: Int,
            key: BigInteger) {
        val bytes = BigIntegers.asUnsignedByteArray((orderBitLength + 7) / 8, key)

        val v = ASN1EncodableVector()

        v.add(ASN1Integer(1))
        v.add(DEROctetString(bytes))

        seq = DERSequence(v)
    }


    @Deprecated("use constructor which takes orderBitLength to guarantee correct encoding.")
    constructor(
            key: BigInteger,
            parameters: ASN1Encodable) : this(key, null, parameters) {
    }


    @Deprecated("use constructor which takes orderBitLength to guarantee correct encoding.")
    constructor(
            key: BigInteger,
            publicKey: DERBitString?,
            parameters: ASN1Encodable) : this(key.bitLength(), key, publicKey, parameters) {
    }

    constructor(
            orderBitLength: Int,
            key: BigInteger,
            parameters: ASN1Encodable) : this(orderBitLength, key, null, parameters) {
    }

    constructor(
            orderBitLength: Int,
            key: BigInteger,
            publicKey: DERBitString?,
            parameters: ASN1Encodable?) {
        val bytes = BigIntegers.asUnsignedByteArray((orderBitLength + 7) / 8, key)

        val v = ASN1EncodableVector()

        v.add(ASN1Integer(1))
        v.add(DEROctetString(bytes))

        if (parameters != null) {
            v.add(DERTaggedObject(true, 0, parameters))
        }

        if (publicKey != null) {
            v.add(DERTaggedObject(true, 1, publicKey))
        }

        seq = DERSequence(v)
    }

    val key: BigInteger
        get() {
            val octs = seq!!.getObjectAt(1) as ASN1OctetString

            return BigInteger(1, octs.octets)
        }

    val publicKey: DERBitString
        get() = getObjectInTag(1) as DERBitString?

    val parameters: ASN1Primitive
        get() = getObjectInTag(0)

    private fun getObjectInTag(tagNo: Int): ASN1Primitive? {
        val e = seq!!.objects

        while (e.hasMoreElements()) {
            val obj = e.nextElement() as ASN1Encodable

            if (obj is ASN1TaggedObject) {
                if (obj.tagNo == tagNo) {
                    return obj.`object`.toASN1Primitive()
                }
            }
        }
        return null
    }

    /**
     * ECPrivateKey ::= SEQUENCE {
     * version INTEGER { ecPrivkeyVer1(1) } (ecPrivkeyVer1),
     * privateKey OCTET STRING,
     * parameters [0] Parameters OPTIONAL,
     * publicKey [1] BIT STRING OPTIONAL }
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return seq
    }

    companion object {

        fun getInstance(
                obj: Any?): ECPrivateKey? {
            if (obj is ECPrivateKey) {
                return obj
            }

            if (obj != null) {
                return ECPrivateKey(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
