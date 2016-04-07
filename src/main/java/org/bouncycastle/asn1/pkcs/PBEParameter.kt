package org.bouncycastle.asn1.pkcs

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence

class PBEParameter : ASN1Object {
    internal var iterations: ASN1Integer
    internal var salt: ASN1OctetString

    constructor(
            salt: ByteArray,
            iterations: Int) {
        if (salt.size != 8) {
            throw IllegalArgumentException("salt length must be 8")
        }
        this.salt = DEROctetString(salt)
        this.iterations = ASN1Integer(iterations.toLong())
    }

    private constructor(
            seq: ASN1Sequence) {
        salt = seq.getObjectAt(0) as ASN1OctetString
        iterations = seq.getObjectAt(1) as ASN1Integer
    }

    val iterationCount: BigInteger
        get() = iterations.value

    fun getSalt(): ByteArray {
        return salt.octets
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(salt)
        v.add(iterations)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): PBEParameter? {
            if (obj is PBEParameter) {
                return obj
            } else if (obj != null) {
                return PBEParameter(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
