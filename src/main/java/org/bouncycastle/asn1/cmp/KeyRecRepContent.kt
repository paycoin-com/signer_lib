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

class KeyRecRepContent private constructor(seq: ASN1Sequence) : ASN1Object() {
    val status: PKIStatusInfo
    var newSigCert: CMPCertificate? = null
        private set
    private var caCerts: ASN1Sequence? = null
    private var keyPairHist: ASN1Sequence? = null

    init {
        val en = seq.objects

        status = PKIStatusInfo.getInstance(en.nextElement())

        while (en.hasMoreElements()) {
            val tObj = ASN1TaggedObject.getInstance(en.nextElement())

            when (tObj.tagNo) {
                0 -> newSigCert = CMPCertificate.getInstance(tObj.`object`)
                1 -> caCerts = ASN1Sequence.getInstance(tObj.`object`)
                2 -> keyPairHist = ASN1Sequence.getInstance(tObj.`object`)
                else -> throw IllegalArgumentException("unknown tag number: " + tObj.tagNo)
            }
        }
    }

    fun getCaCerts(): Array<CMPCertificate>? {
        if (caCerts == null) {
            return null
        }

        val results = arrayOfNulls<CMPCertificate>(caCerts!!.size())

        for (i in results.indices) {
            results[i] = CMPCertificate.getInstance(caCerts!!.getObjectAt(i))
        }

        return results
    }

    fun getKeyPairHist(): Array<CertifiedKeyPair>? {
        if (keyPairHist == null) {
            return null
        }

        val results = arrayOfNulls<CertifiedKeyPair>(keyPairHist!!.size())

        for (i in results.indices) {
            results[i] = CertifiedKeyPair.getInstance(keyPairHist!!.getObjectAt(i))
        }

        return results
    }

    /**
     *
     * KeyRecRepContent ::= SEQUENCE {
     * status                  PKIStatusInfo,
     * newSigCert          [0] CMPCertificate OPTIONAL,
     * caCerts             [1] SEQUENCE SIZE (1..MAX) OF
     * CMPCertificate OPTIONAL,
     * keyPairHist         [2] SEQUENCE SIZE (1..MAX) OF
     * CertifiedKeyPair OPTIONAL
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(status)

        addOptional(v, 0, newSigCert)
        addOptional(v, 1, caCerts)
        addOptional(v, 2, keyPairHist)

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, tagNo: Int, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(DERTaggedObject(true, tagNo, obj))
        }
    }

    companion object {

        fun getInstance(o: Any?): KeyRecRepContent? {
            if (o is KeyRecRepContent) {
                return o
            }

            if (o != null) {
                return KeyRecRepContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
