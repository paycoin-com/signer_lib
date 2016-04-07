package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-10.2.7): OtherKeyAttribute object.
 *
 *
 *
 * OtherKeyAttribute ::= SEQUENCE {
 * keyAttrId OBJECT IDENTIFIER,
 * keyAttr ANY DEFINED BY keyAttrId OPTIONAL
 * }
 *
 */
class OtherKeyAttribute : ASN1Object {
    var keyAttrId: ASN1ObjectIdentifier? = null
        private set
    var keyAttr: ASN1Encodable? = null
        private set


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        keyAttrId = seq.getObjectAt(0) as ASN1ObjectIdentifier
        keyAttr = seq.getObjectAt(1)
    }

    constructor(
            keyAttrId: ASN1ObjectIdentifier,
            keyAttr: ASN1Encodable) {
        this.keyAttrId = keyAttrId
        this.keyAttr = keyAttr
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(keyAttrId)
        v.add(keyAttr)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an OtherKeyAttribute object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [OtherKeyAttribute] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with OtherKeyAttribute structure inside
         *

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                o: Any?): OtherKeyAttribute? {
            if (o is OtherKeyAttribute) {
                return o
            }

            if (o != null) {
                return OtherKeyAttribute(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
