package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.X509Extensions

class TBSRequest : ASN1Object {

    var version: ASN1Integer
        internal set
    var requestorName: GeneralName? = null
        internal set
    var requestList: ASN1Sequence
        internal set
    var requestExtensions: Extensions? = null
        internal set

    internal var versionSet: Boolean = false

    /**
     * @param requestorName
     * *
     * @param requestList
     * *
     * @param requestExtensions
     */
    @Deprecated("use method taking Extensions\n      ")
    constructor(
            requestorName: GeneralName,
            requestList: ASN1Sequence,
            requestExtensions: X509Extensions) {
        this.version = V1
        this.requestorName = requestorName
        this.requestList = requestList
        this.requestExtensions = Extensions.getInstance(requestExtensions)
    }

    constructor(
            requestorName: GeneralName,
            requestList: ASN1Sequence,
            requestExtensions: Extensions) {
        this.version = V1
        this.requestorName = requestorName
        this.requestList = requestList
        this.requestExtensions = requestExtensions
    }

    private constructor(
            seq: ASN1Sequence) {
        var index = 0

        if (seq.getObjectAt(0) is ASN1TaggedObject) {
            val o = seq.getObjectAt(0) as ASN1TaggedObject

            if (o.tagNo == 0) {
                versionSet = true
                version = ASN1Integer.getInstance(seq.getObjectAt(0) as ASN1TaggedObject, true)
                index++
            } else {
                version = V1
            }
        } else {
            version = V1
        }

        if (seq.getObjectAt(index) is ASN1TaggedObject) {
            requestorName = GeneralName.getInstance(seq.getObjectAt(index++) as ASN1TaggedObject, true)
        }

        requestList = seq.getObjectAt(index++) as ASN1Sequence

        if (seq.size() == index + 1) {
            requestExtensions = Extensions.getInstance(seq.getObjectAt(index) as ASN1TaggedObject, true)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * TBSRequest      ::=     SEQUENCE {
     * version             [0]     EXPLICIT Version DEFAULT v1,
     * requestorName       [1]     EXPLICIT GeneralName OPTIONAL,
     * requestList                 SEQUENCE OF Request,
     * requestExtensions   [2]     EXPLICIT Extensions OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        //
        // if default don't include - unless explicitly provided. Not strictly correct
        // but required for some requests
        //
        if (version != V1 || versionSet) {
            v.add(DERTaggedObject(true, 0, version))
        }

        if (requestorName != null) {
            v.add(DERTaggedObject(true, 1, requestorName))
        }

        v.add(requestList)

        if (requestExtensions != null) {
            v.add(DERTaggedObject(true, 2, requestExtensions))
        }

        return DERSequence(v)
    }

    companion object {
        private val V1 = ASN1Integer(0)

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): TBSRequest {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): TBSRequest? {
            if (obj is TBSRequest) {
                return obj
            } else if (obj != null) {
                return TBSRequest(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
