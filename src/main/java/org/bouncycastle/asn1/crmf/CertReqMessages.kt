package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class CertReqMessages : ASN1Object {
    private var content: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        content = seq
    }

    constructor(
            msg: CertReqMsg) {
        content = DERSequence(msg)
    }

    constructor(
            msgs: Array<CertReqMsg>) {
        val v = ASN1EncodableVector()
        for (i in msgs.indices) {
            v.add(msgs[i])
        }
        content = DERSequence(v)
    }

    fun toCertReqMsgArray(): Array<CertReqMsg> {
        val result = arrayOfNulls<CertReqMsg>(content!!.size())

        for (i in result.indices) {
            result[i] = CertReqMsg.getInstance(content!!.getObjectAt(i))
        }

        return result
    }

    /**
     *
     * CertReqMessages ::= SEQUENCE SIZE (1..MAX) OF CertReqMsg
     *

     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): CertReqMessages? {
            if (o is CertReqMessages) {
                return o
            }

            if (o != null) {
                return CertReqMessages(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
