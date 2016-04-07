package org.bouncycastle.asn1.crmf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class CertReqMsg : ASN1Object {
    var certReq: CertRequest? = null
        private set

    var pop: ProofOfPossession? = null
        private set
    private var regInfo: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        val en = seq.objects

        certReq = CertRequest.getInstance(en.nextElement())
        while (en.hasMoreElements()) {
            val o = en.nextElement()

            if (o is ASN1TaggedObject || o is ProofOfPossession) {
                pop = ProofOfPossession.getInstance(o)
            } else {
                regInfo = ASN1Sequence.getInstance(o)
            }
        }
    }

    /**
     * Creates a new CertReqMsg.
     * @param certReq CertRequest
     * *
     * @param pop may be null
     * *
     * @param regInfo may be null
     */
    constructor(
            certReq: CertRequest?,
            pop: ProofOfPossession,
            regInfo: Array<AttributeTypeAndValue>?) {
        if (certReq == null) {
            throw IllegalArgumentException("'certReq' cannot be null")
        }

        this.certReq = certReq
        this.pop = pop

        if (regInfo != null) {
            this.regInfo = DERSequence(regInfo)
        }
    }


    val popo: ProofOfPossession
        get() = pop

    fun getRegInfo(): Array<AttributeTypeAndValue>? {
        if (regInfo == null) {
            return null
        }

        val results = arrayOfNulls<AttributeTypeAndValue>(regInfo!!.size())

        for (i in results.indices) {
            results[i] = AttributeTypeAndValue.getInstance(regInfo!!.getObjectAt(i))
        }

        return results
    }

    /**
     *
     * CertReqMsg ::= SEQUENCE {
     * certReq   CertRequest,
     * popo       ProofOfPossession  OPTIONAL,
     * -- content depends upon key type
     * regInfo   SEQUENCE SIZE(1..MAX) OF AttributeTypeAndValue OPTIONAL }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certReq)

        addOptional(v, pop)
        addOptional(v, regInfo)

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(obj)
        }
    }

    companion object {

        fun getInstance(o: Any?): CertReqMsg? {
            if (o is CertReqMsg) {
                return o
            } else if (o != null) {
                return CertReqMsg(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
