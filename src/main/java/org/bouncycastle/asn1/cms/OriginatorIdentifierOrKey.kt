package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.2):
 * Content encryption key delivery mechanisms.
 *
 * OriginatorIdentifierOrKey ::= CHOICE {
 * issuerAndSerialNumber IssuerAndSerialNumber,
 * subjectKeyIdentifier [0] SubjectKeyIdentifier,
 * originatorKey [1] OriginatorPublicKey
 * }

 * SubjectKeyIdentifier ::= OCTET STRING
 *
 */
class OriginatorIdentifierOrKey : ASN1Object, ASN1Choice {
    var id: ASN1Encodable? = null
        private set

    constructor(
            id: IssuerAndSerialNumber) {
        this.id = id
    }


    @Deprecated("use version taking a SubjectKeyIdentifier")
    constructor(
            id: ASN1OctetString) : this(SubjectKeyIdentifier(id.octets)) {
    }

    constructor(
            id: SubjectKeyIdentifier) {
        this.id = DERTaggedObject(false, 0, id)
    }

    constructor(
            id: OriginatorPublicKey) {
        this.id = DERTaggedObject(false, 1, id)
    }


    @Deprecated("use more specific version")
    constructor(
            id: ASN1Primitive) {
        this.id = id
    }

    val issuerAndSerialNumber: IssuerAndSerialNumber?
        get() {
            if (id is IssuerAndSerialNumber) {
                return id as IssuerAndSerialNumber?
            }

            return null
        }

    val subjectKeyIdentifier: SubjectKeyIdentifier?
        get() {
            if (id is ASN1TaggedObject && (id as ASN1TaggedObject).tagNo == 0) {
                return SubjectKeyIdentifier.getInstance(id as ASN1TaggedObject?, false)
            }

            return null
        }

    val originatorKey: OriginatorPublicKey?
        get() {
            if (id is ASN1TaggedObject && (id as ASN1TaggedObject).tagNo == 1) {
                return OriginatorPublicKey.getInstance(id as ASN1TaggedObject?, false)
            }

            return null
        }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return id!!.toASN1Primitive()
    }

    companion object {

        /**
         * Return an OriginatorIdentifierOrKey object from a tagged object.

         * @param o the tagged object holding the object we want.
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the object held by the
         * *          tagged object cannot be converted.
         */
        fun getInstance(
                o: ASN1TaggedObject,
                explicit: Boolean): OriginatorIdentifierOrKey {
            if (!explicit) {
                throw IllegalArgumentException(
                        "Can't implicitly tag OriginatorIdentifierOrKey")
            }

            return getInstance(o.`object`)
        }

        /**
         * Return an OriginatorIdentifierOrKey object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [OriginatorIdentifierOrKey] object
         *  *  [IssuerAndSerialNumber] object
         *  *  [ASN1TaggedObject][org.bouncycastle.asn1.ASN1TaggedObject.getInstance] input formats with IssuerAndSerialNumber structure inside
         *

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                o: Any?): OriginatorIdentifierOrKey {
            if (o == null || o is OriginatorIdentifierOrKey) {
                return o as OriginatorIdentifierOrKey?
            }

            if (o is IssuerAndSerialNumber || o is ASN1Sequence) {
                return OriginatorIdentifierOrKey(IssuerAndSerialNumber.getInstance(o))
            }

            if (o is ASN1TaggedObject) {

                if (o.tagNo == 0) {
                    return OriginatorIdentifierOrKey(SubjectKeyIdentifier.getInstance(o, false))
                } else if (o.tagNo == 1) {
                    return OriginatorIdentifierOrKey(OriginatorPublicKey.getInstance(o, false))
                }
            }

            throw IllegalArgumentException("Invalid OriginatorIdentifierOrKey: " + o.javaClass.name)
        }
    }
}
