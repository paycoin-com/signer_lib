package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

class OCSPRequest : ASN1Object {
    var tbsRequest: TBSRequest
        internal set
    var optionalSignature: Signature? = null
        internal set

    constructor(
            tbsRequest: TBSRequest,
            optionalSignature: Signature) {
        this.tbsRequest = tbsRequest
        this.optionalSignature = optionalSignature
    }

    private constructor(
            seq: ASN1Sequence) {
        tbsRequest = TBSRequest.getInstance(seq.getObjectAt(0))

        if (seq.size() == 2) {
            optionalSignature = Signature.getInstance(
                    seq.getObjectAt(1) as ASN1TaggedObject, true)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * OCSPRequest     ::=     SEQUENCE {
     * tbsRequest                  TBSRequest,
     * optionalSignature   [0]     EXPLICIT Signature OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(tbsRequest)

        if (optionalSignature != null) {
            v.add(DERTaggedObject(true, 0, optionalSignature))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): OCSPRequest {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): OCSPRequest? {
            if (obj is OCSPRequest) {
                return obj
            } else if (obj != null) {
                return OCSPRequest(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
