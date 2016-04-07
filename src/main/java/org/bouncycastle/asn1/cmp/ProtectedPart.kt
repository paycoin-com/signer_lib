package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class ProtectedPart : ASN1Object {
    var header: PKIHeader? = null
        private set
    var body: PKIBody? = null
        private set

    private constructor(seq: ASN1Sequence) {
        header = PKIHeader.getInstance(seq.getObjectAt(0))
        body = PKIBody.getInstance(seq.getObjectAt(1))
    }

    constructor(header: PKIHeader, body: PKIBody) {
        this.header = header
        this.body = body
    }

    /**
     *
     * ProtectedPart ::= SEQUENCE {
     * header    PKIHeader,
     * body      PKIBody
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(header)
        v.add(body)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): ProtectedPart? {
            if (o is ProtectedPart) {
                return o
            }

            if (o != null) {
                return ProtectedPart(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
