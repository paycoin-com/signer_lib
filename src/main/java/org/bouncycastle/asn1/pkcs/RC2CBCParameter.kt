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

class RC2CBCParameter : ASN1Object {
    internal var version: ASN1Integer? = null
    internal var iv: ASN1OctetString

    constructor(
            iv: ByteArray) {
        this.version = null
        this.iv = DEROctetString(iv)
    }

    constructor(
            parameterVersion: Int,
            iv: ByteArray) {
        this.version = ASN1Integer(parameterVersion.toLong())
        this.iv = DEROctetString(iv)
    }

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() == 1) {
            version = null
            iv = seq.getObjectAt(0) as ASN1OctetString
        } else {
            version = seq.getObjectAt(0) as ASN1Integer
            iv = seq.getObjectAt(1) as ASN1OctetString
        }
    }

    val rC2ParameterVersion: BigInteger?
        get() {
            if (version == null) {
                return null
            }

            return version!!.value
        }

    fun getIV(): ByteArray {
        return iv.octets
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (version != null) {
            v.add(version)
        }

        v.add(iv)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                o: Any?): RC2CBCParameter? {
            if (o is RC2CBCParameter) {
                return o
            }
            if (o != null) {
                return RC2CBCParameter(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
