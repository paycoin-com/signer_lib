package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DLSet

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652) defines
 * 5 "SET OF Attribute" entities with 5 different names.
 * This is common implementation for them all:
 *
 * SignedAttributes      ::= SET SIZE (1..MAX) OF Attribute
 * UnsignedAttributes    ::= SET SIZE (1..MAX) OF Attribute
 * UnprotectedAttributes ::= SET SIZE (1..MAX) OF Attribute
 * AuthAttributes        ::= SET SIZE (1..MAX) OF Attribute
 * UnauthAttributes      ::= SET SIZE (1..MAX) OF Attribute

 * Attributes ::=
 * SET SIZE(1..MAX) OF Attribute
 *
 */
class Attributes : ASN1Object {
    private var attributes: ASN1Set? = null

    private constructor(set: ASN1Set) {
        attributes = set
    }

    constructor(v: ASN1EncodableVector) {
        attributes = DLSet(v)
    }

    fun getAttributes(): Array<Attribute> {
        val rv = arrayOfNulls<Attribute>(attributes!!.size())

        for (i in rv.indices) {
            rv[i] = Attribute.getInstance(attributes!!.getObjectAt(i))
        }

        return rv
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return attributes
    }

    companion object {

        /**
         * Return an Attribute set object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [Attributes] object
         *  *  [ASN1Set][org.bouncycastle.asn1.ASN1Set.getInstance] input formats with Attributes structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(obj: Any?): Attributes? {
            if (obj is Attributes) {
                return obj
            } else if (obj != null) {
                return Attributes(ASN1Set.getInstance(obj))
            }

            return null
        }
    }
}
