package org.bouncycastle.asn1.dvcs

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.cmp.PKIStatusInfo
import org.bouncycastle.asn1.x509.DigestInfo
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.PolicyInformation

/**
 *
 * DVCSCertInfo::= SEQUENCE  {
 * version             Integer DEFAULT 1 ,
 * dvReqInfo           DVCSRequestInformation,
 * messageImprint      DigestInfo,
 * serialNumber        Integer,
 * responseTime        DVCSTime,
 * dvStatus            [0] PKIStatusInfo OPTIONAL,
 * policy              [1] PolicyInformation OPTIONAL,
 * reqSignature        [2] SignerInfos  OPTIONAL,
 * certs               [3] SEQUENCE SIZE (1..MAX) OF
 * TargetEtcChain OPTIONAL,
 * extensions          Extensions OPTIONAL
 * }
 *
 */

class DVCSCertInfo : ASN1Object {

    var version = DEFAULT_VERSION
        private set(version) {
            this.version = version
        }
    var dvReqInfo: DVCSRequestInformation? = null
        private set(dvReqInfo) {
            this.dvReqInfo = dvReqInfo
        }
    var messageImprint: DigestInfo? = null
        private set(messageImprint) {
            this.messageImprint = messageImprint
        }
    var serialNumber: ASN1Integer? = null
        private set
    var responseTime: DVCSTime? = null
        private set
    var dvStatus: PKIStatusInfo? = null
        private set
    var policy: PolicyInformation? = null
        private set
    var reqSignature: ASN1Set? = null
        private set
    private var certs: ASN1Sequence? = null
    var extensions: Extensions? = null
        private set

    constructor(
            dvReqInfo: DVCSRequestInformation,
            messageImprint: DigestInfo,
            serialNumber: ASN1Integer,
            responseTime: DVCSTime) {
        this.dvReqInfo = dvReqInfo
        this.messageImprint = messageImprint
        this.serialNumber = serialNumber
        this.responseTime = responseTime
    }

    private constructor(seq: ASN1Sequence) {
        var i = 0
        var x = seq.getObjectAt(i++)
        if (x is ASN1Integer) {
            val encVersion = ASN1Integer.getInstance(x)
            this.version = encVersion.value.toInt()
            x = seq.getObjectAt(i++)
        }

        this.dvReqInfo = DVCSRequestInformation.getInstance(x)
        x = seq.getObjectAt(i++)
        this.messageImprint = DigestInfo.getInstance(x)
        x = seq.getObjectAt(i++)
        this.serialNumber = ASN1Integer.getInstance(x)
        x = seq.getObjectAt(i++)
        this.responseTime = DVCSTime.getInstance(x)

        while (i < seq.size()) {

            x = seq.getObjectAt(i++)

            try {
                val t = ASN1TaggedObject.getInstance(x)
                val tagNo = t.tagNo

                when (tagNo) {
                    TAG_DV_STATUS -> this.dvStatus = PKIStatusInfo.getInstance(t, false)
                    TAG_POLICY -> this.policy = PolicyInformation.getInstance(ASN1Sequence.getInstance(t, false))
                    TAG_REQ_SIGNATURE -> this.reqSignature = ASN1Set.getInstance(t, false)
                    TAG_CERTS -> this.certs = ASN1Sequence.getInstance(t, false)
                }

                continue

            } catch (e: IllegalArgumentException) {
            }

            try {
                this.extensions = Extensions.getInstance(x)
            } catch (e: IllegalArgumentException) {
            }

        }

    }

    override fun toASN1Primitive(): ASN1Primitive {

        val v = ASN1EncodableVector()

        if (version != DEFAULT_VERSION) {
            v.add(ASN1Integer(version.toLong()))
        }
        v.add(dvReqInfo)
        v.add(messageImprint)
        v.add(serialNumber)
        v.add(responseTime)
        if (dvStatus != null) {
            v.add(DERTaggedObject(false, TAG_DV_STATUS, dvStatus))
        }
        if (policy != null) {
            v.add(DERTaggedObject(false, TAG_POLICY, policy))
        }
        if (reqSignature != null) {
            v.add(DERTaggedObject(false, TAG_REQ_SIGNATURE, reqSignature))
        }
        if (certs != null) {
            v.add(DERTaggedObject(false, TAG_CERTS, certs))
        }
        if (extensions != null) {
            v.add(extensions)
        }

        return DERSequence(v)
    }

    override fun toString(): String {
        val s = StringBuffer()

        s.append("DVCSCertInfo {\n")

        if (version != DEFAULT_VERSION) {
            s.append("version: " + version + "\n")
        }
        s.append("dvReqInfo: " + dvReqInfo + "\n")
        s.append("messageImprint: " + messageImprint + "\n")
        s.append("serialNumber: " + serialNumber + "\n")
        s.append("responseTime: " + responseTime + "\n")
        if (dvStatus != null) {
            s.append("dvStatus: " + dvStatus + "\n")
        }
        if (policy != null) {
            s.append("policy: " + policy + "\n")
        }
        if (reqSignature != null) {
            s.append("reqSignature: " + reqSignature + "\n")
        }
        if (certs != null) {
            s.append("certs: " + certs + "\n")
        }
        if (extensions != null) {
            s.append("extensions: " + extensions + "\n")
        }

        s.append("}\n")
        return s.toString()
    }

    fun getCerts(): Array<TargetEtcChain>? {
        if (certs != null) {
            return TargetEtcChain.arrayFromSequence(certs)
        }

        return null
    }

    companion object {

        private val DEFAULT_VERSION = 1
        private val TAG_DV_STATUS = 0
        private val TAG_POLICY = 1
        private val TAG_REQ_SIGNATURE = 2
        private val TAG_CERTS = 3

        fun getInstance(obj: Any?): DVCSCertInfo? {
            if (obj is DVCSCertInfo) {
                return obj
            } else if (obj != null) {
                return DVCSCertInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DVCSCertInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}
