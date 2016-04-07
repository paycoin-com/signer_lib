package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

class CertRepMessage : ASN1Object {
    private var caPubs: ASN1Sequence? = null
    private var response: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        var index = 0

        if (seq.size() > 1) {
            caPubs = ASN1Sequence.getInstance(seq.getObjectAt(index++) as ASN1TaggedObject, true)
        }

        response = ASN1Sequence.getInstance(seq.getObjectAt(index))
    }

    constructor(caPubs: Array<CMPCertificate>?, response: Array<CertResponse>?) {
        if (response == null) {
            throw IllegalArgumentException("'response' cannot be null")
        }

        if (caPubs != null) {
            val v = ASN1EncodableVector()
            for (i in caPubs.indices) {
                v.add(caPubs[i])
            }
            this.caPubs = DERSequence(v)
        }

        run {
            val v = ASN1EncodableVector()
            for (i in response.indices) {
                v.add(response[i])
            }
            this.response = DERSequence(v)
        }
    }

    fun getCaPubs(): Array<CMPCertificate>? {
        if (caPubs == null) {
            return null
        }

        val results = arrayOfNulls<CMPCertificate>(caPubs!!.size())

        for (i in results.indices) {
            results[i] = CMPCertificate.getInstance(caPubs!!.getObjectAt(i))
        }

        return results
    }

    fun getResponse(): Array<CertResponse> {
        val results = arrayOfNulls<CertResponse>(response!!.size())

        for (i in results.indices) {
            results[i] = CertResponse.getInstance(response!!.getObjectAt(i))
        }

        return results
    }

    /**
     *
     * CertRepMessage ::= SEQUENCE {
     * caPubs       [1] SEQUENCE SIZE (1..MAX) OF CMPCertificate
     * OPTIONAL,
     * response         SEQUENCE OF CertResponse
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (caPubs != null) {
            v.add(DERTaggedObject(true, 1, caPubs))
        }

        v.add(response)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CertRepMessage? {
            if (o is CertRepMessage) {
                return o
            }

            if (o != null) {
                return CertRepMessage(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
