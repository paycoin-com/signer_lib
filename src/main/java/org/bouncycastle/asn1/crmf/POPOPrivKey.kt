package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.cms.EnvelopedData

class POPOPrivKey : ASN1Object, ASN1Choice {

    var type: Int = 0
        private set
    var value: ASN1Encodable? = null
        private set

    private constructor(obj: ASN1TaggedObject) {
        this.type = obj.tagNo

        when (type) {
            thisMessage -> this.value = DERBitString.getInstance(obj, false)
            subsequentMessage -> this.value = SubsequentMessage.valueOf(ASN1Integer.getInstance(obj, false).value.toInt())
            dhMAC -> this.value = DERBitString.getInstance(obj, false)
            agreeMAC -> this.value = PKMACValue.getInstance(obj, false)
            encryptedKey -> this.value = EnvelopedData.getInstance(obj, false)
            else -> throw IllegalArgumentException("unknown tag in POPOPrivKey")
        }
    }

    constructor(msg: SubsequentMessage) {
        this.type = subsequentMessage
        this.value = msg
    }

    /**
     *
     * POPOPrivKey ::= CHOICE {
     * thisMessage       [0] BIT STRING,         -- Deprecated
     * -- possession is proven in this message (which contains the private
     * -- key itself (encrypted for the CA))
     * subsequentMessage [1] SubsequentMessage,
     * -- possession will be proven in a subsequent message
     * dhMAC             [2] BIT STRING,         -- Deprecated
     * agreeMAC          [3] PKMACValue,
     * encryptedKey      [4] EnvelopedData }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return DERTaggedObject(false, type, value)
    }

    companion object {
        val thisMessage = 0
        val subsequentMessage = 1
        val dhMAC = 2
        val agreeMAC = 3
        val encryptedKey = 4

        fun getInstance(obj: Any?): POPOPrivKey? {
            if (obj is POPOPrivKey) {
                return obj
            }
            if (obj != null) {
                return POPOPrivKey(ASN1TaggedObject.getInstance(obj))
            }

            return null
        }

        fun getInstance(obj: ASN1TaggedObject, explicit: Boolean): POPOPrivKey {
            return getInstance(ASN1TaggedObject.getInstance(obj, explicit))
        }
    }
}
