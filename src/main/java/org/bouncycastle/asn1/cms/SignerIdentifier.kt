package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-5.3):
 * Identify who signed the containing [SignerInfo] object.
 *
 *
 * The certificates referred to by this are at containing [SignedData] structure.
 *
 *
 *
 * SignerIdentifier ::= CHOICE {
 * issuerAndSerialNumber IssuerAndSerialNumber,
 * subjectKeyIdentifier [0] SubjectKeyIdentifier
 * }

 * SubjectKeyIdentifier ::= OCTET STRING
 *
 */
class SignerIdentifier : ASN1Object, ASN1Choice {
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

        return id
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return id!!.toASN1Primitive()
    }

    companion object {

        /**
         * Return a SignerIdentifier object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [SignerIdentifier] object
         *  *  [IssuerAndSerialNumber] object
         *  *  [ASN1OctetString][org.bouncycastle.asn1.ASN1OctetString.getInstance] input formats with SignerIdentifier structure inside
         *  *  [ASN1Primitive][org.bouncycastle.asn1.ASN1Primitive] for SignerIdentifier constructor.
         *

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                o: Any?): SignerIdentifier {
            if (o == null || o is SignerIdentifier) {
                return o as SignerIdentifier?
            }

            if (o is IssuerAndSerialNumber) {
                return SignerIdentifier(o as IssuerAndSerialNumber?)
            }

            if (o is ASN1OctetString) {
                return SignerIdentifier(o as ASN1OctetString?)
            }

            if (o is ASN1Primitive) {
                return SignerIdentifier(o as ASN1Primitive?)
            }

            throw IllegalArgumentException(
                    "Illegal object in SignerIdentifier: " + o.javaClass.name)
        }
    }
}
