package org.bouncycastle.asn1.misc

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence

class IDEACBCPar : ASN1Object {
    internal var iv: ASN1OctetString? = null

    constructor(
            iv: ByteArray) {
        this.iv = DEROctetString(iv)
    }

    constructor(
            seq: ASN1Sequence) {
        if (seq.size() == 1) {
            iv = seq.getObjectAt(0) as ASN1OctetString
        } else {
            iv = null
        }
    }

    fun getIV(): ByteArray? {
        if (iv != null) {
            return iv!!.octets
        } else {
            return null
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * IDEA-CBCPar ::= SEQUENCE {
     * iv    OCTET STRING OPTIONAL -- exactly 8 octets
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (iv != null) {
            v.add(iv)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                o: Any?): IDEACBCPar? {
            if (o is IDEACBCPar) {
                return o
            } else if (o != null) {
                return IDEACBCPar(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
