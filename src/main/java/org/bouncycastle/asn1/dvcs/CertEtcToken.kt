package org.bouncycastle.asn1.dvcs

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.cmp.PKIStatusInfo
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.asn1.ess.ESSCertID
import org.bouncycastle.asn1.ocsp.CertID
import org.bouncycastle.asn1.ocsp.CertStatus
import org.bouncycastle.asn1.ocsp.OCSPResponse
import org.bouncycastle.asn1.smime.SMIMECapabilities
import org.bouncycastle.asn1.x509.Certificate
import org.bouncycastle.asn1.x509.CertificateList
import org.bouncycastle.asn1.x509.Extension

/**
 *
 * CertEtcToken ::= CHOICE {
 * certificate                  [0] IMPLICIT Certificate ,
 * esscertid                    [1] ESSCertId ,
 * pkistatus                    [2] IMPLICIT PKIStatusInfo ,
 * assertion                    [3] ContentInfo ,
 * crl                          [4] IMPLICIT CertificateList,
 * ocspcertstatus               [5] CertStatus,
 * oscpcertid                   [6] IMPLICIT CertId ,
 * oscpresponse                 [7] IMPLICIT OCSPResponse,
 * capabilities                 [8] SMIMECapabilities,
 * extension                    Extension
 * }
 *
 */
class CertEtcToken : ASN1Object, ASN1Choice {

    var tagNo: Int = 0
        private set
    var value: ASN1Encodable? = null
        private set
    val extension: Extension?

    constructor(tagNo: Int, value: ASN1Encodable) {
        this.tagNo = tagNo
        this.value = value
    }

    constructor(extension: Extension) {
        this.tagNo = -1
        this.extension = extension
    }

    private constructor(choice: ASN1TaggedObject) {
        this.tagNo = choice.tagNo

        when (tagNo) {
            TAG_CERTIFICATE -> value = Certificate.getInstance(choice, false)
            TAG_ESSCERTID -> value = ESSCertID.getInstance(choice.`object`)
            TAG_PKISTATUS -> value = PKIStatusInfo.getInstance(choice, false)
            TAG_ASSERTION -> value = ContentInfo.getInstance(choice.`object`)
            TAG_CRL -> value = CertificateList.getInstance(choice, false)
            TAG_OCSPCERTSTATUS -> value = CertStatus.getInstance(choice.`object`)
            TAG_OCSPCERTID -> value = CertID.getInstance(choice, false)
            TAG_OCSPRESPONSE -> value = OCSPResponse.getInstance(choice, false)
            TAG_CAPABILITIES -> value = SMIMECapabilities.getInstance(choice.`object`)
            else -> throw IllegalArgumentException("Unknown tag: " + tagNo)
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        if (extension == null) {
            return DERTaggedObject(explicit[tagNo], tagNo, value)
        } else {
            return extension.toASN1Primitive()
        }
    }

    override fun toString(): String {
        return "CertEtcToken {\n$value}\n"
    }

    companion object {
        val TAG_CERTIFICATE = 0
        val TAG_ESSCERTID = 1
        val TAG_PKISTATUS = 2
        val TAG_ASSERTION = 3
        val TAG_CRL = 4
        val TAG_OCSPCERTSTATUS = 5
        val TAG_OCSPCERTID = 6
        val TAG_OCSPRESPONSE = 7
        val TAG_CAPABILITIES = 8

        private val explicit = booleanArrayOf(false, true, false, true, false, true, false, false, true)

        fun getInstance(obj: Any?): CertEtcToken? {
            if (obj is CertEtcToken) {
                return obj
            } else if (obj is ASN1TaggedObject) {
                return CertEtcToken(obj as ASN1TaggedObject?)
            } else if (obj != null) {
                return CertEtcToken(Extension.getInstance(obj))
            }

            return null
        }

        fun arrayFromSequence(seq: ASN1Sequence): Array<CertEtcToken> {
            val tmp = arrayOfNulls<CertEtcToken>(seq.size())

            for (i in tmp.indices) {
                tmp[i] = CertEtcToken.getInstance(seq.getObjectAt(i))
            }

            return tmp
        }
    }
}
