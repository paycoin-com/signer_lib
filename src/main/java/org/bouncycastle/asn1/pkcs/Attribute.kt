package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DERSequence

class Attribute : ASN1Object {
    var attrType: ASN1ObjectIdentifier? = null
        private set
    var attrValues: ASN1Set? = null
        private set

    constructor(
            seq: ASN1Sequence) {
        attrType = seq.getObjectAt(0) as ASN1ObjectIdentifier
        attrValues = seq.getObjectAt(1) as ASN1Set
    }

    constructor(
            attrType: ASN1ObjectIdentifier,
            attrValues: ASN1Set) {
        this.attrType = attrType
        this.attrValues = attrValues
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
                o: Any?): Attribute {
            if (o == null || o is Attribute) {
                return o as Attribute?
            }

            if (o is ASN1Sequence) {
                return Attribute(o as ASN1Sequence?)
            }

            throw IllegalArgumentException("unknown object in factory: " + o.javaClass.name)
        }
    }
}
