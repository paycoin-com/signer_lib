package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class PKIPublicationInfo private constructor(seq: ASN1Sequence) : ASN1Object() {
    val action: ASN1Integer
    private val pubInfos: ASN1Sequence?

    init {
        action = ASN1Integer.getInstance(seq.getObjectAt(0))
        pubInfos = ASN1Sequence.getInstance(seq.getObjectAt(1))
    }

    fun getPubInfos(): Array<SinglePubInfo>? {
        if (pubInfos == null) {
            return null
        }

        val results = arrayOfNulls<SinglePubInfo>(pubInfos.size())

        for (i in results.indices) {
            results[i] = SinglePubInfo.getInstance(pubInfos.getObjectAt(i))
        }

        return results
    }

    /**
     *
     * PKIPublicationInfo ::= SEQUENCE {
     * action     INTEGER {
     * dontPublish (0),
     * pleasePublish (1) },
     * pubInfos  SEQUENCE SIZE (1..MAX) OF SinglePubInfo OPTIONAL }
     * -- pubInfos MUST NOT be present if action is "dontPublish"
     * -- (if action is "pleasePublish" and pubInfos is omitted,
     * -- "dontCare" is assumed)
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(action)
        v.add(pubInfos)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): PKIPublicationInfo? {
            if (o is PKIPublicationInfo) {
                return o
            }

            if (o != null) {
                return PKIPublicationInfo(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
