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
 * OtherRevVals ::= SEQUENCE {
 * otherRevValType OtherRevValType,
 * otherRevVals ANY DEFINED BY OtherRevValType
 * }

 * OtherRevValType ::= OBJECT IDENTIFIER
 *
 */
class OtherRevVals : ASN1Object {

    var otherRevValType: ASN1ObjectIdentifier? = null
        private set

    var otherRevVals: ASN1Encodable? = null
        private set

    private constructor(seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        this.otherRevValType = seq.getObjectAt(0) as ASN1ObjectIdentifier
        try {
            this.otherRevVals = ASN1Primitive.fromByteArray(seq.getObjectAt(1).toASN1Primitive().getEncoded(ASN1Encoding.DER))
        } catch (e: IOException) {
            throw IllegalStateException()
        }

    }

    constructor(otherRevValType: ASN1ObjectIdentifier,
                otherRevVals: ASN1Encodable) {
        this.otherRevValType = otherRevValType
        this.otherRevVals = otherRevVals
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.otherRevValType)
        v.add(this.otherRevVals)
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): OtherRevVals? {
            if (obj is OtherRevVals) {
                return obj
            }
            if (obj != null) {
                return OtherRevVals(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
