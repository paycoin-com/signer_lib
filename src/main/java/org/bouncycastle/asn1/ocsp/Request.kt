package org.bouncycastle.asn1.ocsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Extensions

class Request : ASN1Object {
    var reqCert: CertID
        internal set
    var singleRequestExtensions: Extensions? = null
        internal set

    constructor(
            reqCert: CertID,
            singleRequestExtensions: Extensions) {
        this.reqCert = reqCert
        this.singleRequestExtensions = singleRequestExtensions
    }

    private constructor(
            seq: ASN1Sequence) {
        reqCert = CertID.getInstance(seq.getObjectAt(0))

        if (seq.size() == 2) {
            singleRequestExtensions = Extensions.getInstance(
                    seq.getObjectAt(1) as ASN1TaggedObject, true)
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * Request         ::=     SEQUENCE {
     * reqCert                     CertID,
     * singleRequestExtensions     [0] EXPLICIT Extensions OPTIONAL }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(reqCert)

        if (singleRequestExtensions != null) {
            v.add(DERTaggedObject(true, 0, singleRequestExtensions))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): Request {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): Request? {
            if (obj is Request) {
                return obj
            } else if (obj != null) {
                return Request(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
