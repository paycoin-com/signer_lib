package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class CertRequest : ASN1Object {
    var certReqId: ASN1Integer? = null
        private set
    var certTemplate: CertTemplate? = null
        private set
    var controls: Controls? = null
        private set

    private constructor(seq: ASN1Sequence) {
        certReqId = ASN1Integer(ASN1Integer.getInstance(seq.getObjectAt(0)).value)
        certTemplate = CertTemplate.getInstance(seq.getObjectAt(1))
        if (seq.size() > 2) {
            controls = Controls.getInstance(seq.getObjectAt(2))
        }
    }

    constructor(
            certReqId: Int,
            certTemplate: CertTemplate,
            controls: Controls) : this(ASN1Integer(certReqId.toLong()), certTemplate, controls) {
    }

    constructor(
            certReqId: ASN1Integer,
            certTemplate: CertTemplate,
            controls: Controls) {
        this.certReqId = certReqId
        this.certTemplate = certTemplate
        this.controls = controls
    }

    /**
     *
     * CertRequest ::= SEQUENCE {
     * certReqId     INTEGER,          -- ID for matching request and reply
     * certTemplate  CertTemplate,  -- Selected fields of cert to be issued
     * controls      Controls OPTIONAL }   -- Attributes affecting issuance
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certReqId)
        v.add(certTemplate)

        if (controls != null) {
            v.add(controls)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CertRequest? {
            if (o is CertRequest) {
                return o
            } else if (o != null) {
                return CertRequest(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
