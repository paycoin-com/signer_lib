package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.cms.EnvelopedData

class EncryptedKey : ASN1Object, ASN1Choice {
    private val envelopedData: EnvelopedData
    private val encryptedValue: EncryptedValue?

    constructor(envelopedData: EnvelopedData) {
        this.envelopedData = envelopedData
    }

    constructor(encryptedValue: EncryptedValue) {
        this.encryptedValue = encryptedValue
    }

    val isEncryptedValue: Boolean
        get() = encryptedValue != null

    val value: ASN1Encodable
        get() {
            if (encryptedValue != null) {
                return encryptedValue
            }

            return envelopedData
        }

    /**
     *
     * EncryptedKey ::= CHOICE {
     * encryptedValue        EncryptedValue, -- deprecated
     * envelopedData     [0] EnvelopedData }
     * -- The encrypted private key MUST be placed in the envelopedData
     * -- encryptedContentInfo encryptedContent OCTET STRING.
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (encryptedValue != null) {
            return encryptedValue.toASN1Primitive()
        }

        return DERTaggedObject(false, 0, envelopedData)
    }

    companion object {

        fun getInstance(o: Any): EncryptedKey {
            if (o is EncryptedKey) {
                return o
            } else if (o is ASN1TaggedObject) {
                return EncryptedKey(EnvelopedData.getInstance(o, false))
            } else if (o is EncryptedValue) {
                return EncryptedKey(o)
            } else {
                return EncryptedKey(EncryptedValue.getInstance(o))
            }
        }
    }
}
