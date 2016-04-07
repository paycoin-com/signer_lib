package org.bouncycastle.asn1.cmp

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.crmf.CertId
import org.bouncycastle.asn1.x509.CertificateList

class RevRepContent private constructor(seq: ASN1Sequence) : ASN1Object() {
    private val status: ASN1Sequence
    private var revCerts: ASN1Sequence? = null
    private var crls: ASN1Sequence? = null

    init {
        val en = seq.objects

        status = ASN1Sequence.getInstance(en.nextElement())
        while (en.hasMoreElements()) {
            val tObj = ASN1TaggedObject.getInstance(en.nextElement())

            if (tObj.tagNo == 0) {
                revCerts = ASN1Sequence.getInstance(tObj, true)
            } else {
                crls = ASN1Sequence.getInstance(tObj, true)
            }
        }
    }

    fun getStatus(): Array<PKIStatusInfo> {
        val results = arrayOfNulls<PKIStatusInfo>(status.size())

        for (i in results.indices) {
            results[i] = PKIStatusInfo.getInstance(status.getObjectAt(i))
        }

        return results
    }

    fun getRevCerts(): Array<CertId>? {
        if (revCerts == null) {
            return null
        }

        val results = arrayOfNulls<CertId>(revCerts!!.size())

        for (i in results.indices) {
            results[i] = CertId.getInstance(revCerts!!.getObjectAt(i))
        }

        return results
    }

    fun getCrls(): Array<CertificateList>? {
        if (crls == null) {
            return null
        }

        val results = arrayOfNulls<CertificateList>(crls!!.size())

        for (i in results.indices) {
            results[i] = CertificateList.getInstance(crls!!.getObjectAt(i))
        }

        return results
    }

    /**
     *
     * RevRepContent ::= SEQUENCE {
     * status       SEQUENCE SIZE (1..MAX) OF PKIStatusInfo,
     * -- in same order as was sent in RevReqContent
     * revCerts [0] SEQUENCE SIZE (1..MAX) OF CertId OPTIONAL,
     * -- IDs for which revocation was requested
     * -- (same order as status)
     * crls     [1] SEQUENCE SIZE (1..MAX) OF CertificateList OPTIONAL
     * -- the resulting CRLs (there may be more than one)
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(status)

        addOptional(v, 0, revCerts)
        addOptional(v, 1, crls)

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, tagNo: Int, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(DERTaggedObject(true, tagNo, obj))
        }
    }

    companion object {

        fun getInstance(o: Any?): RevRepContent? {
            if (o is RevRepContent) {
                return o
            }

            if (o != null) {
                return RevRepContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
