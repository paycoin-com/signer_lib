package org.bouncycastle.asn1.cmp

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.GeneralName

class PKIHeader : ASN1Object {

    var pvno: ASN1Integer? = null
        private set
    var sender: GeneralName? = null
        private set
    var recipient: GeneralName? = null
        private set
    var messageTime: ASN1GeneralizedTime? = null
        private set
    var protectionAlg: AlgorithmIdentifier? = null
        private set
    var senderKID: ASN1OctetString? = null
        private set       // KeyIdentifier
    var recipKID: ASN1OctetString? = null
        private set        // KeyIdentifier
    var transactionID: ASN1OctetString? = null
        private set
    var senderNonce: ASN1OctetString? = null
        private set
    var recipNonce: ASN1OctetString? = null
        private set
    var freeText: PKIFreeText? = null
        private set
    private var generalInfo: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        val en = seq.objects

        pvno = ASN1Integer.getInstance(en.nextElement())
        sender = GeneralName.getInstance(en.nextElement())
        recipient = GeneralName.getInstance(en.nextElement())

        while (en.hasMoreElements()) {
            val tObj = en.nextElement() as ASN1TaggedObject

            when (tObj.tagNo) {
                0 -> messageTime = ASN1GeneralizedTime.getInstance(tObj, true)
                1 -> protectionAlg = AlgorithmIdentifier.getInstance(tObj, true)
                2 -> senderKID = ASN1OctetString.getInstance(tObj, true)
                3 -> recipKID = ASN1OctetString.getInstance(tObj, true)
                4 -> transactionID = ASN1OctetString.getInstance(tObj, true)
                5 -> senderNonce = ASN1OctetString.getInstance(tObj, true)
                6 -> recipNonce = ASN1OctetString.getInstance(tObj, true)
                7 -> freeText = PKIFreeText.getInstance(tObj, true)
                8 -> generalInfo = ASN1Sequence.getInstance(tObj, true)
                else -> throw IllegalArgumentException("unknown tag number: " + tObj.tagNo)
            }
        }
    }

    constructor(
            pvno: Int,
            sender: GeneralName,
            recipient: GeneralName) : this(ASN1Integer(pvno.toLong()), sender, recipient) {
    }

    private constructor(
            pvno: ASN1Integer,
            sender: GeneralName,
            recipient: GeneralName) {
        this.pvno = pvno
        this.sender = sender
        this.recipient = recipient
    }

    fun getGeneralInfo(): Array<InfoTypeAndValue>? {
        if (generalInfo == null) {
            return null
        }
        val results = arrayOfNulls<InfoTypeAndValue>(generalInfo!!.size())
        for (i in results.indices) {
            results[i] = InfoTypeAndValue.getInstance(generalInfo!!.getObjectAt(i))
        }
        return results
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
    override fun toASN1Primitive(): ASN1Primitive {
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

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, tagNo: Int, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(DERTaggedObject(true, tagNo, obj))
        }
    }

    companion object {
        /**
         * Value for a "null" recipient or sender.
         */
        val NULL_NAME = GeneralName(X500Name.getInstance(DERSequence()))

        val CMP_1999 = 1
        val CMP_2000 = 2

        fun getInstance(o: Any?): PKIHeader? {
            if (o is PKIHeader) {
                return o
            }

            if (o != null) {
                return PKIHeader(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
