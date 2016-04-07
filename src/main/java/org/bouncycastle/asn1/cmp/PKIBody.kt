package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.crmf.CertReqMessages
import org.bouncycastle.asn1.pkcs.CertificationRequest

class PKIBody : ASN1Object, ASN1Choice {

    var type: Int = 0
        private set
    var content: ASN1Encodable? = null
        private set

    private constructor(tagged: ASN1TaggedObject) {
        type = tagged.tagNo
        content = getBodyForType(type, tagged.`object`)
    }

    /**
     * Creates a new PKIBody.
     * @param type one of the TYPE_* constants
     * *
     * @param content message content
     */
    constructor(
            type: Int,
            content: ASN1Encodable) {
        this.type = type
        this.content = getBodyForType(type, content)
    }

    /**
     *
     * PKIBody ::= CHOICE {       -- message-specific body elements
     * ir       [0]  CertReqMessages,        --Initialization Request
     * ip       [1]  CertRepMessage,         --Initialization Response
     * cr       [2]  CertReqMessages,        --Certification Request
     * cp       [3]  CertRepMessage,         --Certification Response
     * p10cr    [4]  CertificationRequest,   --imported from [PKCS10]
     * popdecc  [5]  POPODecKeyChallContent, --pop Challenge
     * popdecr  [6]  POPODecKeyRespContent,  --pop Response
     * kur      [7]  CertReqMessages,        --Key Update Request
     * kup      [8]  CertRepMessage,         --Key Update Response
     * krr      [9]  CertReqMessages,        --Key Recovery Request
     * krp      [10] KeyRecRepContent,       --Key Recovery Response
     * rr       [11] RevReqContent,          --Revocation Request
     * rp       [12] RevRepContent,          --Revocation Response
     * ccr      [13] CertReqMessages,        --Cross-Cert. Request
     * ccp      [14] CertRepMessage,         --Cross-Cert. Response
     * ckuann   [15] CAKeyUpdAnnContent,     --CA Key Update Ann.
     * cann     [16] CertAnnContent,         --Certificate Ann.
     * rann     [17] RevAnnContent,          --Revocation Ann.
     * crlann   [18] CRLAnnContent,          --CRL Announcement
     * pkiconf  [19] PKIConfirmContent,      --Confirmation
     * nested   [20] NestedMessageContent,   --Nested Message
     * genm     [21] GenMsgContent,          --General Message
     * genp     [22] GenRepContent,          --General Response
     * error    [23] ErrorMsgContent,        --Error Message
     * certConf [24] CertConfirmContent,     --Certificate confirm
     * pollReq  [25] PollReqContent,         --Polling request
     * pollRep  [26] PollRepContent          --Polling response
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return DERTaggedObject(true, type, content)
    }

    companion object {
        val TYPE_INIT_REQ = 0
        val TYPE_INIT_REP = 1
        val TYPE_CERT_REQ = 2
        val TYPE_CERT_REP = 3
        val TYPE_P10_CERT_REQ = 4
        val TYPE_POPO_CHALL = 5
        val TYPE_POPO_REP = 6
        val TYPE_KEY_UPDATE_REQ = 7
        val TYPE_KEY_UPDATE_REP = 8
        val TYPE_KEY_RECOVERY_REQ = 9
        val TYPE_KEY_RECOVERY_REP = 10
        val TYPE_REVOCATION_REQ = 11
        val TYPE_REVOCATION_REP = 12
        val TYPE_CROSS_CERT_REQ = 13
        val TYPE_CROSS_CERT_REP = 14
        val TYPE_CA_KEY_UPDATE_ANN = 15
        val TYPE_CERT_ANN = 16
        val TYPE_REVOCATION_ANN = 17
        val TYPE_CRL_ANN = 18
        val TYPE_CONFIRM = 19
        val TYPE_NESTED = 20
        val TYPE_GEN_MSG = 21
        val TYPE_GEN_REP = 22
        val TYPE_ERROR = 23
        val TYPE_CERT_CONFIRM = 24
        val TYPE_POLL_REQ = 25
        val TYPE_POLL_REP = 26

        fun getInstance(o: Any?): PKIBody {
            if (o == null || o is PKIBody) {
                return o as PKIBody?
            }

            if (o is ASN1TaggedObject) {
                return PKIBody(o as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("Invalid object: " + o.javaClass.name)
        }

        private fun getBodyForType(
                type: Int,
                o: ASN1Encodable): ASN1Encodable {
            when (type) {
                TYPE_INIT_REQ -> return CertReqMessages.getInstance(o)
                TYPE_INIT_REP -> return CertRepMessage.getInstance(o)
                TYPE_CERT_REQ -> return CertReqMessages.getInstance(o)
                TYPE_CERT_REP -> return CertRepMessage.getInstance(o)
                TYPE_P10_CERT_REQ -> return CertificationRequest.getInstance(o)
                TYPE_POPO_CHALL -> return POPODecKeyChallContent.getInstance(o)
                TYPE_POPO_REP -> return POPODecKeyRespContent.getInstance(o)
                TYPE_KEY_UPDATE_REQ -> return CertReqMessages.getInstance(o)
                TYPE_KEY_UPDATE_REP -> return CertRepMessage.getInstance(o)
                TYPE_KEY_RECOVERY_REQ -> return CertReqMessages.getInstance(o)
                TYPE_KEY_RECOVERY_REP -> return KeyRecRepContent.getInstance(o)
                TYPE_REVOCATION_REQ -> return RevReqContent.getInstance(o)
                TYPE_REVOCATION_REP -> return RevRepContent.getInstance(o)
                TYPE_CROSS_CERT_REQ -> return CertReqMessages.getInstance(o)
                TYPE_CROSS_CERT_REP -> return CertRepMessage.getInstance(o)
                TYPE_CA_KEY_UPDATE_ANN -> return CAKeyUpdAnnContent.getInstance(o)
                TYPE_CERT_ANN -> return CMPCertificate.getInstance(o)
                TYPE_REVOCATION_ANN -> return RevAnnContent.getInstance(o)
                TYPE_CRL_ANN -> return CRLAnnContent.getInstance(o)
                TYPE_CONFIRM -> return PKIConfirmContent.getInstance(o)
                TYPE_NESTED -> return PKIMessages.getInstance(o)
                TYPE_GEN_MSG -> return GenMsgContent.getInstance(o)
                TYPE_GEN_REP -> return GenRepContent.getInstance(o)
                TYPE_ERROR -> return ErrorMsgContent.getInstance(o)
                TYPE_CERT_CONFIRM -> return CertConfirmContent.getInstance(o)
                TYPE_POLL_REQ -> return PollReqContent.getInstance(o)
                TYPE_POLL_REP -> return PollRepContent.getInstance(o)
                else -> throw IllegalArgumentException("unknown tag number: " + type)
            }
        }
    }
}
