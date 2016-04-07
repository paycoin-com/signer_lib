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
@Deprecated("use ECPrivateKey")
class ECPrivateKeyStructure : ASN1Object {
    private var seq: ASN1Sequence? = null

    constructor(
            seq: ASN1Sequence) {
        this.seq = seq
    }

    constructor(
            key: BigInteger) {
        val bytes = BigIntegers.asUnsignedByteArray(key)

        val v = ASN1EncodableVector()

        v.add(ASN1Integer(1))
        v.add(DEROctetString(bytes))

        seq = DERSequence(v)
    }

    constructor(
            key: BigInteger,
            parameters: ASN1Encodable) : this(key, null, parameters) {
    }

    constructor(
            key: BigInteger,
            publicKey: DERBitString?,
            parameters: ASN1Encodable?) {
        val bytes = BigIntegers.asUnsignedByteArray(key)

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
                    return (obj.`object` as ASN1Encodable).toASN1Primitive()
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
}
