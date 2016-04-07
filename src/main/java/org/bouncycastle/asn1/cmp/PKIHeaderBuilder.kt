package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.GeneralName

class PKIHeaderBuilder private constructor(
        private val pvno: ASN1Integer,
        private val sender: GeneralName,
        private val recipient: GeneralName) {
    private var messageTime: ASN1GeneralizedTime? = null
    private var protectionAlg: AlgorithmIdentifier? = null
    private var senderKID: ASN1OctetString? = null       // KeyIdentifier
    private var recipKID: ASN1OctetString? = null        // KeyIdentifier
    private var transactionID: ASN1OctetString? = null
    private var senderNonce: ASN1OctetString? = null
    private var recipNonce: ASN1OctetString? = null
    private var freeText: PKIFreeText? = null
    private var generalInfo: ASN1Sequence? = null

    constructor(
            pvno: Int,
            sender: GeneralName,
            recipient: GeneralName) : this(ASN1Integer(pvno.toLong()), sender, recipient) {
    }

    fun setMessageTime(time: ASN1GeneralizedTime): PKIHeaderBuilder {
        messageTime = time

        return this
    }

    fun setProtectionAlg(aid: AlgorithmIdentifier): PKIHeaderBuilder {
        protectionAlg = aid

        return this
    }

    fun setSenderKID(kid: ByteArray?): PKIHeaderBuilder {
        return setSenderKID(if (kid == null) null else DEROctetString(kid))
    }

    fun setSenderKID(kid: ASN1OctetString?): PKIHeaderBuilder {
        senderKID = kid

        return this
    }

    fun setRecipKID(kid: ByteArray?): PKIHeaderBuilder {
        return setRecipKID(if (kid == null) null else DEROctetString(kid))
    }

    fun setRecipKID(kid: DEROctetString?): PKIHeaderBuilder {
        recipKID = kid

        return this
    }

    fun setTransactionID(tid: ByteArray?): PKIHeaderBuilder {
        return setTransactionID(if (tid == null) null else DEROctetString(tid))
    }

    fun setTransactionID(tid: ASN1OctetString?): PKIHeaderBuilder {
        transactionID = tid

        return this
    }

    fun setSenderNonce(nonce: ByteArray?): PKIHeaderBuilder {
        return setSenderNonce(if (nonce == null) null else DEROctetString(nonce))
    }

    fun setSenderNonce(nonce: ASN1OctetString?): PKIHeaderBuilder {
        senderNonce = nonce

        return this
    }

    fun setRecipNonce(nonce: ByteArray?): PKIHeaderBuilder {
        return setRecipNonce(if (nonce == null) null else DEROctetString(nonce))
    }

    fun setRecipNonce(nonce: ASN1OctetString?): PKIHeaderBuilder {
        recipNonce = nonce

        return this
    }

    fun setFreeText(text: PKIFreeText): PKIHeaderBuilder {
        freeText = text

        return this
    }

    fun setGeneralInfo(genInfo: InfoTypeAndValue): PKIHeaderBuilder {
        return setGeneralInfo(makeGeneralInfoSeq(genInfo))
    }

    fun setGeneralInfo(genInfos: Array<InfoTypeAndValue>): PKIHeaderBuilder {
        return setGeneralInfo(makeGeneralInfoSeq(genInfos))
    }

    fun setGeneralInfo(seqOfInfoTypeAndValue: ASN1Sequence): PKIHeaderBuilder {
        generalInfo = seqOfInfoTypeAndValue

        return this
    }

    private fun makeGeneralInfoSeq(
            generalInfo: InfoTypeAndValue): ASN1Sequence {
        return DERSequence(generalInfo)
    }

    private fun makeGeneralInfoSeq(
            generalInfos: Array<InfoTypeAndValue>?): ASN1Sequence {
        var genInfoSeq: ASN1Sequence? = null
        if (generalInfos != null) {
            val v = ASN1EncodableVector()
            for (i in generalInfos.indices) {
                v.add(generalInfos[i])
            }
            genInfoSeq = DERSequence(v)
        }
        return genInfoSeq
    }

    /**
     *
     * PKIHeader ::= SEQUENCE {
     * pvno                INTEGER     { cmp1999(1), cmp2000(2) },
     * sender              GeneralName,
     * -- identifies the sender
     * recipient           GeneralName,
     * -- identifies the intended recipient
     * messageTime     [0] GeneralizedTime         OPTIONAL,
     * -- time of production of this message (used when sender
     * -- believes that the transport will be "suitable"; i.e.,
     * -- that the time will still be meaningful upon receipt)
     * protectionAlg   [1] AlgorithmIdentifier     OPTIONAL,
     * -- algorithm used for calculation of protection bits
     * senderKID       [2] KeyIdentifier           OPTIONAL,
     * recipKID        [3] KeyIdentifier           OPTIONAL,
     * -- to identify specific keys used for protection
     * transactionID   [4] OCTET STRING            OPTIONAL,
     * -- identifies the transaction; i.e., this will be the same in
     * -- corresponding request, response, certConf, and PKIConf
     * -- messages
     * senderNonce     [5] OCTET STRING            OPTIONAL,
     * recipNonce      [6] OCTET STRING            OPTIONAL,
     * -- nonces used to provide replay protection, senderNonce
     * -- is inserted by the creator of this message; recipNonce
     * -- is a nonce previously inserted in a related message by
     * -- the intended recipient of this message
     * freeText        [7] PKIFreeText             OPTIONAL,
     * -- this may be used to indicate context-specific instructions
     * -- (this field is intended for human consumption)
     * generalInfo     [8] SEQUENCE SIZE (1..MAX) OF
     * InfoTypeAndValue     OPTIONAL
     * -- this may be used to convey context-specific information
     * -- (this field not primarily intended for human consumption)
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    fun build(): PKIHeader {
        val v = ASN1EncodableVector()

        v.add(pvno)
        v.add(sender)
        v.add(recipient)
        addOptional(v, 0, messageTime)
        addOptional(v, 1, protectionAlg)
        addOptional(v, 2, senderKID)
        addOptional(v, 3, recipKID)
        addOptional(v, 4, transactionID)
        addOptional(v, 5, senderNonce)
        addOptional(v, 6, recipNonce)
        addOptional(v, 7, freeText)
        addOptional(v, 8, generalInfo)

        messageTime = null
        protectionAlg = null
        senderKID = null
        recipKID = null
        transactionID = null
        senderNonce = null
        recipNonce = null
        freeText = null
        generalInfo = null

        return PKIHeader.getInstance(DERSequence(v))
    }

    private fun addOptional(v: ASN1EncodableVector, tagNo: Int, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(DERTaggedObject(true, tagNo, obj))
        }
    }
}
