package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.X509Extensions

class ResponseData : ASN1Object {

    private var versionPresent: Boolean = false

    var version: ASN1Integer? = null
        private set
    var responderID: ResponderID? = null
        private set
    var producedAt: ASN1GeneralizedTime? = null
        private set
    var responses: ASN1Sequence? = null
        private set
    var responseExtensions: Extensions? = null
        private set

    constructor(
            version: ASN1Integer,
            responderID: ResponderID,
            producedAt: ASN1GeneralizedTime,
            responses: ASN1Sequence,
            responseExtensions: Extensions) {
        this.version = version
        this.responderID = responderID
        this.producedAt = producedAt
        this.responses = responses
        this.responseExtensions = responseExtensions
    }

    /**
     * @param responderID
     * *
     * @param producedAt
     * *
     * @param responses
     * *
     * @param responseExtensions
     */
    @Deprecated("use method taking Extensions\n      ")
    constructor(
            responderID: ResponderID,
            producedAt: ASN1GeneralizedTime,
            responses: ASN1Sequence,
            responseExtensions: X509Extensions) : this(V1, responderID, ASN1GeneralizedTime.getInstance(producedAt), responses, Extensions.getInstance(responseExtensions)) {
    }

    constructor(
            responderID: ResponderID,
            producedAt: ASN1GeneralizedTime,
            responses: ASN1Sequence,
            responseExtensions: Extensions) : this(V1, responderID, producedAt, responses, responseExtensions) {
    }

    private constructor(
            seq: ASN1Sequence) {
        var index = 0

        if (seq.getObjectAt(0) is ASN1TaggedObject) {
            val o = seq.getObjectAt(0) as ASN1TaggedObject

            if (o.tagNo == 0) {
                this.versionPresent = true
                this.version = ASN1Integer.getInstance(
                        seq.getObjectAt(0) as ASN1TaggedObject, true)
                index++
            } else {
                this.version = V1
            }
        } else {
            this.version = V1
        }

        this.responderID = ResponderID.getInstance(seq.getObjectAt(index++))
        this.producedAt = ASN1GeneralizedTime.getInstance(seq.getObjectAt(index++))
        this.responses = seq.getObjectAt(index++) as ASN1Sequence

        if (seq.size() > index) {
            this.responseExtensions = Extensions.getInstance(
                    seq.getObjectAt(index) as ASN1TaggedObject, true)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * ResponseData ::= SEQUENCE {
     * version              [0] EXPLICIT Version DEFAULT v1,
     * responderID              ResponderID,
     * producedAt               GeneralizedTime,
     * responses                SEQUENCE OF SingleResponse,
     * responseExtensions   [1] EXPLICIT Extensions OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (versionPresent || version != V1) {
            v.add(DERTaggedObject(true, 0, version))
        }

        v.add(responderID)
        v.add(producedAt)
        v.add(responses)
        if (responseExtensions != null) {
            v.add(DERTaggedObject(true, 1, responseExtensions))
        }

        return DERSequence(v)
    }

    companion object {
        private val V1 = ASN1Integer(0)

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ResponseData {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): ResponseData? {
            if (obj is ResponseData) {
                return obj
            } else if (obj != null) {
                return ResponseData(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
