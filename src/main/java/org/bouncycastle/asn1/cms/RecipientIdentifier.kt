package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.1):
 * Content encryption key delivery mechanisms.
 *
 * RecipientIdentifier ::= CHOICE {
 * issuerAndSerialNumber IssuerAndSerialNumber,
 * subjectKeyIdentifier [0] SubjectKeyIdentifier
 * }

 * SubjectKeyIdentifier ::= OCTET STRING
 *
 */
class RecipientIdentifier : ASN1Object, ASN1Choice {
    private var id: ASN1Encodable? = null

    constructor(
            id: IssuerAndSerialNumber) {
        this.id = id
    }

    constructor(
            id: ASN1OctetString) {
        this.id = DERTaggedObject(false, 0, id)
    }

    constructor(
            id: ASN1Primitive) {
        this.id = id
    }

    val isTagged: Boolean
        get() = id is ASN1TaggedObject

    fun getId(): ASN1Encodable {
        if (id is ASN1TaggedObject) {
            return ASN1OctetString.getInstance(id as ASN1TaggedObject?, false)
        }

        return IssuerAndSerialNumber.getInstance(id)
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return id!!.toASN1Primitive()
    }

    companion object {

        /**
         * Return a RecipientIdentifier object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [RecipientIdentifier] object
         *  *  [IssuerAndSerialNumber] object
         *  *  [ASN1OctetString][org.bouncycastle.asn1.ASN1OctetString.getInstance] input formats (OctetString, byte[]) with value of KeyIdentifier in DER form
         *  *  [ASN1Primitive][org.bouncycastle.asn1.ASN1Primitive] for RecipientIdentifier constructor
         *

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                o: Any?): RecipientIdentifier {
            if (o == null || o is RecipientIdentifier) {
                return o as RecipientIdentifier?
            }

            if (o is IssuerAndSerialNumber) {
                return RecipientIdentifier(o as IssuerAndSerialNumber?)
            }

            if (o is ASN1OctetString) {
                return RecipientIdentifier(o as ASN1OctetString?)
            }

            if (o is ASN1Primitive) {
                return RecipientIdentifier(o as ASN1Primitive?)
            }

            throw IllegalArgumentException(
                    "Illegal object in RecipientIdentifier: " + o.javaClass.name)
        }
    }
}
