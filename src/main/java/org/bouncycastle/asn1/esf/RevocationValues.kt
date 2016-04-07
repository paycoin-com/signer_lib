package org.bouncycastle.asn1.esf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse
import org.bouncycastle.asn1.x509.CertificateList

/**
 *
 * RevocationValues ::= SEQUENCE {
 * crlVals [0] SEQUENCE OF CertificateList OPTIONAL,
 * ocspVals [1] SEQUENCE OF BasicOCSPResponse OPTIONAL,
 * otherRevVals [2] OtherRevVals OPTIONAL}
 *
 */
class RevocationValues : ASN1Object {

    private var crlVals: ASN1Sequence? = null
    private var ocspVals: ASN1Sequence? = null
    var otherRevVals: OtherRevVals? = null
        private set

    private constructor(seq: ASN1Sequence) {
        if (seq.size() > 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        val e = seq.objects
        while (e.hasMoreElements()) {
            val o = e.nextElement() as DERTaggedObject
            when (o.tagNo) {
                0 -> {
                    val crlValsSeq = o.`object` as ASN1Sequence
                    val crlValsEnum = crlValsSeq.objects
                    while (crlValsEnum.hasMoreElements()) {
                        CertificateList.getInstance(crlValsEnum.nextElement())
                    }
                    this.crlVals = crlValsSeq
                }
                1 -> {
                    val ocspValsSeq = o.`object` as ASN1Sequence
                    val ocspValsEnum = ocspValsSeq.objects
                    while (ocspValsEnum.hasMoreElements()) {
                        BasicOCSPResponse.getInstance(ocspValsEnum.nextElement())
                    }
                    this.ocspVals = ocspValsSeq
                }
                2 -> this.otherRevVals = OtherRevVals.getInstance(o.`object`)
                else -> throw IllegalArgumentException("invalid tag: " + o.tagNo)
            }
        }
    }

    constructor(crlVals: Array<CertificateList>?,
                ocspVals: Array<BasicOCSPResponse>?, otherRevVals: OtherRevVals) {
        if (null != crlVals) {
            this.crlVals = DERSequence(crlVals)
        }
        if (null != ocspVals) {
            this.ocspVals = DERSequence(ocspVals)
        }
        this.otherRevVals = otherRevVals
    }

    fun getCrlVals(): Array<CertificateList> {
        if (null == this.crlVals) {
            return arrayOfNulls(0)
        }
        val result = arrayOfNulls<CertificateList>(this.crlVals!!.size())
        for (idx in result.indices) {
            result[idx] = CertificateList.getInstance(this.crlVals!!.getObjectAt(idx))
        }
        return result
    }

    fun getOcspVals(): Array<BasicOCSPResponse> {
        if (null == this.ocspVals) {
            return arrayOfNulls(0)
        }
        val result = arrayOfNulls<BasicOCSPResponse>(this.ocspVals!!.size())
        for (idx in result.indices) {
            result[idx] = BasicOCSPResponse.getInstance(this.ocspVals!!.getObjectAt(idx))
        }
        return result
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        if (null != this.crlVals) {
            v.add(DERTaggedObject(true, 0, this.crlVals))
        }
        if (null != this.ocspVals) {
            v.add(DERTaggedObject(true, 1, this.ocspVals))
        }
        if (null != this.otherRevVals) {
            v.add(DERTaggedObject(true, 2, this.otherRevVals!!.toASN1Primitive()))
        }
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): RevocationValues? {
            if (obj is RevocationValues) {
                return obj
            } else if (obj != null) {
                return RevocationValues(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
