package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class ResponseBytes : ASN1Object {
    var responseType: ASN1ObjectIdentifier
        internal set
    var response: ASN1OctetString
        internal set

    constructor(
            responseType: ASN1ObjectIdentifier,
            response: ASN1OctetString) {
        this.responseType = responseType
        this.response = response
    }


    @Deprecated("use getInstance()")
    constructor(
            seq: ASN1Sequence) {
        responseType = seq.getObjectAt(0) as ASN1ObjectIdentifier
        response = seq.getObjectAt(1) as ASN1OctetString
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * ResponseBytes ::=       SEQUENCE {
     * responseType   OBJECT IDENTIFIER,
     * response       OCTET STRING }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(responseType)
        v.add(response)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ResponseBytes {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): ResponseBytes? {
            if (obj is ResponseBytes) {
                return obj
            } else if (obj != null) {
                return ResponseBytes(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
