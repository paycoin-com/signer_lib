package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class AttributeTypeAndValue : ASN1Object {
    var type: ASN1ObjectIdentifier? = null
        private set
    var value: ASN1Encodable? = null
        private set

    private constructor(seq: ASN1Sequence) {
        type = seq.getObjectAt(0) as ASN1ObjectIdentifier
        value = seq.getObjectAt(1) as ASN1Encodable
    }

    constructor(
            oid: String,
            value: ASN1Encodable) : this(ASN1ObjectIdentifier(oid), value) {
    }

    constructor(
            type: ASN1ObjectIdentifier,
            value: ASN1Encodable) {
        this.type = type
        this.value = value
    }

    /**
     *
     * AttributeTypeAndValue ::= SEQUENCE {
     * type         OBJECT IDENTIFIER,
     * value        ANY DEFINED BY type }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(type)
        v.add(value)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): AttributeTypeAndValue? {
            if (o is AttributeTypeAndValue) {
                return o
            }

            if (o != null) {
                return AttributeTypeAndValue(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
