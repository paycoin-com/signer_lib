package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class BasicOCSPResponse : ASN1Object {
    var tbsResponseData: ResponseData? = null
        private set
    var signatureAlgorithm: AlgorithmIdentifier? = null
        private set
    var signature: DERBitString? = null
        private set
    var certs: ASN1Sequence? = null
        private set

    constructor(
            tbsResponseData: ResponseData,
            signatureAlgorithm: AlgorithmIdentifier,
            signature: DERBitString,
            certs: ASN1Sequence) {
        this.tbsResponseData = tbsResponseData
        this.signatureAlgorithm = signatureAlgorithm
        this.signature = signature
        this.certs = certs
    }

    private constructor(
            seq: ASN1Sequence) {
        this.tbsResponseData = ResponseData.getInstance(seq.getObjectAt(0))
        this.signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
        this.signature = seq.getObjectAt(2) as DERBitString

        if (seq.size() > 3) {
            this.certs = ASN1Sequence.getInstance(seq.getObjectAt(3) as ASN1TaggedObject, true)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * BasicOCSPResponse       ::= SEQUENCE {
     * tbsResponseData      ResponseData,
     * signatureAlgorithm   AlgorithmIdentifier,
     * signature            BIT STRING,
     * certs                [0] EXPLICIT SEQUENCE OF Certificate OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(tbsResponseData)
        v.add(signatureAlgorithm)
        v.add(signature)
        if (certs != null) {
            v.add(DERTaggedObject(true, 0, certs))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): BasicOCSPResponse {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): BasicOCSPResponse? {
            if (obj is BasicOCSPResponse) {
                return obj
            } else if (obj != null) {
                return BasicOCSPResponse(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
