package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5940](http://tools.ietf.org/html/rfc5940):
 * Additional Cryptographic Message Syntax (CMS) Revocation Information Choices.
 *
 *
 *
 * SCVPReqRes ::= SEQUENCE {
 * request  [0] EXPLICIT ContentInfo OPTIONAL,
 * response     ContentInfo }
 *
 */
class SCVPReqRes : ASN1Object {
    val request: ContentInfo?
    val response: ContentInfo

    private constructor(
            seq: ASN1Sequence) {
        if (seq.getObjectAt(0) is ASN1TaggedObject) {
            this.request = ContentInfo.getInstance(ASN1TaggedObject.getInstance(seq.getObjectAt(0)), true)
            this.response = ContentInfo.getInstance(seq.getObjectAt(1))
        } else {
            this.request = null
            this.response = ContentInfo.getInstance(seq.getObjectAt(0))
        }
    }

    constructor(response: ContentInfo) {
        this.request = null       // use of this confuses earlier JDKs
        this.response = response
    }

    constructor(request: ContentInfo, response: ContentInfo) {
        this.request = request
        this.response = response
    }

    /**
     * @return  the ASN.1 primitive representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (request != null) {
            v.add(DERTaggedObject(true, 0, request))
        }

        v.add(response)

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a SCVPReqRes object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [SCVPReqRes] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with SCVPReqRes structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): SCVPReqRes? {
            if (obj is SCVPReqRes) {
                return obj
            } else if (obj != null) {
                return SCVPReqRes(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
