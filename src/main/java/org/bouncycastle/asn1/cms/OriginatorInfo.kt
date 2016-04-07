package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.2.1): OriginatorInfo object.
 *
 * RFC 3369:

 * OriginatorInfo ::= SEQUENCE {
 * certs [0] IMPLICIT CertificateSet OPTIONAL,
 * crls  [1] IMPLICIT CertificateRevocationLists OPTIONAL
 * }
 * CertificateRevocationLists ::= SET OF CertificateList (from X.509)

 * RFC 3582 / 5652:

 * OriginatorInfo ::= SEQUENCE {
 * certs [0] IMPLICIT CertificateSet OPTIONAL,
 * crls  [1] IMPLICIT RevocationInfoChoices OPTIONAL
 * }
 * RevocationInfoChoices ::= SET OF RevocationInfoChoice
 * RevocationInfoChoice ::= CHOICE {
 * crl CertificateList,
 * other [1] IMPLICIT OtherRevocationInfoFormat }

 * OtherRevocationInfoFormat ::= SEQUENCE {
 * otherRevInfoFormat OBJECT IDENTIFIER,
 * otherRevInfo ANY DEFINED BY otherRevInfoFormat }
 *
 *
 *
 * TODO: RevocationInfoChoices / RevocationInfoChoice.
 * Constructor using CertificateSet, CertificationInfoChoices
 */
class OriginatorInfo : ASN1Object {
    var certificates: ASN1Set? = null
        private set
    var crLs: ASN1Set? = null
        private set

    constructor(
            certs: ASN1Set,
            crls: ASN1Set) {
        this.certificates = certs
        this.crLs = crls
    }

    private constructor(
            seq: ASN1Sequence) {
        when (seq.size()) {
            0     // empty
            -> {
            }
            1 -> {
                val o = seq.getObjectAt(0) as ASN1TaggedObject
                when (o.tagNo) {
                    0 -> certificates = ASN1Set.getInstance(o, false)
                    1 -> crLs = ASN1Set.getInstance(o, false)
                    else -> throw IllegalArgumentException("Bad tag in OriginatorInfo: " + o.tagNo)
                }
            }
            2 -> {
                certificates = ASN1Set.getInstance(seq.getObjectAt(0) as ASN1TaggedObject, false)
                crLs = ASN1Set.getInstance(seq.getObjectAt(1) as ASN1TaggedObject, false)
            }
            else -> throw IllegalArgumentException("OriginatorInfo too big")
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (certificates != null) {
            v.add(DERTaggedObject(false, 0, certificates))
        }

        if (crLs != null) {
            v.add(DERTaggedObject(false, 1, crLs))
        }

        return DERSequence(v)
    }

    companion object {

        /**
         * Return an OriginatorInfo object from a tagged object.

         * @param obj the tagged object holding the object we want.
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the object held by the
         * *          tagged object cannot be converted.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): OriginatorInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        /**
         * Return an OriginatorInfo object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [OriginatorInfo] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with OriginatorInfo structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): OriginatorInfo? {
            if (obj is OriginatorInfo) {
                return obj
            } else if (obj != null) {
                return OriginatorInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
