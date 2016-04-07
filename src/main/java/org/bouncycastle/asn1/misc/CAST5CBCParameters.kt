package org.bouncycastle.asn1.misc

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence

class CAST5CBCParameters : ASN1Object {
    internal var keyLength: ASN1Integer
    internal var iv: ASN1OctetString

    constructor(
            iv: ByteArray,
            keyLength: Int) {
        this.iv = DEROctetString(iv)
        this.keyLength = ASN1Integer(keyLength.toLong())
    }

    constructor(
            seq: ASN1Sequence) {
        iv = seq.getObjectAt(0) as ASN1OctetString
        keyLength = seq.getObjectAt(1) as ASN1Integer
    }

    fun getIV(): ByteArray {
        return iv.octets
    }

    fun getKeyLength(): Int {
        return keyLength.value.toInt()
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * cast5CBCParameters ::= SEQUENCE {
     * iv         OCTET STRING DEFAULT 0,
     * -- Initialization vector
     * keyLength  INTEGER
     * -- Key length, in bits
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(iv)
        v.add(keyLength)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                o: Any?): CAST5CBCParameters? {
            if (o is CAST5CBCParameters) {
                return o
            } else if (o != null) {
                return CAST5CBCParameters(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
