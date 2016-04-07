package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DERSequence

class Attribute : ASN1Object {
    private var attrType: ASN1ObjectIdentifier? = null
    var attrValues: ASN1Set? = null
        private set

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() != 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        attrType = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))
        attrValues = ASN1Set.getInstance(seq.getObjectAt(1))
    }

    constructor(
            attrType: ASN1ObjectIdentifier,
            attrValues: ASN1Set) {
        this.attrType = attrType
        this.attrValues = attrValues
    }

    fun getAttrType(): ASN1ObjectIdentifier {
        return ASN1ObjectIdentifier(attrType!!.id)
    }

    val attributeValues: Array<ASN1Encodable>
        get() = attrValues!!.toArray()

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * Attribute ::= SEQUENCE {
     * attrType OBJECT IDENTIFIER,
     * attrValues SET OF AttributeValue
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(attrType)
        v.add(attrValues)

        return DERSequence(v)
    }

    companion object {

        /**
         * return an Attribute object from the given object.

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                o: Any?): Attribute? {
            if (o is Attribute) {
                return o
            }

            if (o != null) {
                return Attribute(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
