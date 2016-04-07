package org.bouncycastle.asn1.dvcs

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
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

class DVCSCertInfoBuilder(
        private var dvReqInfo: DVCSRequestInformation?,
        private var messageImprint: DigestInfo?,
        private var serialNumber: ASN1Integer?,
        private var responseTime: DVCSTime?) {

    private var version = DEFAULT_VERSION
    private var dvStatus: PKIStatusInfo? = null
    private var policy: PolicyInformation? = null
    private var reqSignature: ASN1Set? = null
    private var certs: ASN1Sequence? = null
    private var extensions: Extensions? = null

    fun build(): DVCSCertInfo {

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

        return DVCSCertInfo.getInstance(DERSequence(v))
    }

    fun setVersion(version: Int) {
        this.version = version
    }

    fun setDvReqInfo(dvReqInfo: DVCSRequestInformation) {
        this.dvReqInfo = dvReqInfo
    }

    fun setMessageImprint(messageImprint: DigestInfo) {
        this.messageImprint = messageImprint
    }

    fun setSerialNumber(serialNumber: ASN1Integer) {
        this.serialNumber = serialNumber
    }

    fun setResponseTime(responseTime: DVCSTime) {
        this.responseTime = responseTime
    }

    fun setDvStatus(dvStatus: PKIStatusInfo) {
        this.dvStatus = dvStatus
    }

    fun setPolicy(policy: PolicyInformation) {
        this.policy = policy
    }

    fun setReqSignature(reqSignature: ASN1Set) {
        this.reqSignature = reqSignature
    }

    fun setCerts(certs: Array<TargetEtcChain>) {
        this.certs = DERSequence(certs)
    }

    fun setExtensions(extensions: Extensions) {
        this.extensions = extensions
    }

    companion object {

        private val DEFAULT_VERSION = 1
        private val TAG_DV_STATUS = 0
        private val TAG_POLICY = 1
        private val TAG_REQ_SIGNATURE = 2
        private val TAG_CERTS = 3
    }

}
