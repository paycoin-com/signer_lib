package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DERSequence

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#page-14):
 * Attribute is a pair of OID (as type identifier) + set of values.
 *
 *
 *
 * Attribute ::= SEQUENCE {
 * attrType OBJECT IDENTIFIER,
 * attrValues SET OF AttributeValue
 * }

 * AttributeValue ::= ANY
 *
 *
 *
 * General rule on values is that same AttributeValue must not be included
 * multiple times into the set. That is, if the value is a SET OF INTEGERs,
 * then having same value repeated is wrong: (1, 1), but different values is OK: (1, 2).
 * Normally the AttributeValue syntaxes are more complicated than that.
 *
 *
 * General rule of Attribute usage is that the [Attributes] containers
 * must not have multiple Attribute:s with same attrType (OID) there.
 */
open class Attribute : ASN1Object {
    var attrType: ASN1ObjectIdentifier? = null
        private set
    var attrValues: ASN1Set? = null
        private set

    private constructor(
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
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(attrType)
        v.add(attrValues)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an Attribute object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [Attribute] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with Attribute structure inside
         *

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
