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

class PKCS12PBEParams : ASN1Object {
    internal var iterations: ASN1Integer
    internal var iv: ASN1OctetString

    constructor(
            salt: ByteArray,
            iterations: Int) {
        this.iv = DEROctetString(salt)
        this.iterations = ASN1Integer(iterations.toLong())
    }

    private constructor(
            seq: ASN1Sequence) {
        iv = seq.getObjectAt(0) as ASN1OctetString
        iterations = ASN1Integer.getInstance(seq.getObjectAt(1))
    }

    fun getIterations(): BigInteger {
        return iterations.value
    }

    fun getIV(): ByteArray {
        return iv.octets
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(iv)
        v.add(iterations)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): PKCS12PBEParams? {
            if (obj is PKCS12PBEParams) {
                return obj
            } else if (obj != null) {
                return PKCS12PBEParams(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
