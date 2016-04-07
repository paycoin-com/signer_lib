package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

class OCSPResponse : ASN1Object {
    var responseStatus: OCSPResponseStatus
        internal set
    var responseBytes: ResponseBytes? = null
        internal set

    constructor(
            responseStatus: OCSPResponseStatus,
            responseBytes: ResponseBytes) {
        this.responseStatus = responseStatus
        this.responseBytes = responseBytes
    }

    private constructor(
            seq: ASN1Sequence) {
        responseStatus = OCSPResponseStatus.getInstance(seq.getObjectAt(0))

        if (seq.size() == 2) {
            responseBytes = ResponseBytes.getInstance(
                    seq.getObjectAt(1) as ASN1TaggedObject, true)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * OCSPResponse ::= SEQUENCE {
     * responseStatus         OCSPResponseStatus,
     * responseBytes          [0] EXPLICIT ResponseBytes OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(responseStatus)

        if (responseBytes != null) {
            v.add(DERTaggedObject(true, 0, responseBytes))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): OCSPResponse {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): OCSPResponse? {
            if (obj is OCSPResponse) {
                return obj
            } else if (obj != null) {
                return OCSPResponse(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
