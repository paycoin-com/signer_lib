package org.bouncycastle.asn1.esf

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 *
 * OtherRevRefs ::= SEQUENCE {
 * otherRevRefType OtherRevRefType,
 * otherRevRefs ANY DEFINED BY otherRevRefType
 * }

 * OtherRevRefType ::= OBJECT IDENTIFIER
 *
 */
class OtherRevRefs : ASN1Object {

    var otherRevRefType: ASN1ObjectIdentifier? = null
        private set
    var otherRevRefs: ASN1Encodable? = null
        private set

    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        this.otherRevRefType = ASN1ObjectIdentifier((seq.getObjectAt(0) as ASN1ObjectIdentifier).id)
        try {
            this.otherRevRefs = ASN1Primitive.fromByteArray(seq.getObjectAt(1).toASN1Primitive().getEncoded(ASN1Encoding.DER))
        } catch (e: IOException) {
            throw IllegalStateException()
        }

    }

    constructor(otherRevRefType: ASN1ObjectIdentifier, otherRevRefs: ASN1Encodable) {
        this.otherRevRefType = otherRevRefType
        this.otherRevRefs = otherRevRefs
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.otherRevRefType)
        v.add(this.otherRevRefs)
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): OtherRevRefs? {
            if (obj is OtherRevRefs) {
                return obj
            } else if (obj != null) {
                return OtherRevRefs(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
