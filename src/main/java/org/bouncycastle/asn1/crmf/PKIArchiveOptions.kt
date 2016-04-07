package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

class PKIArchiveOptions : ASN1Object, ASN1Choice {

    var value: ASN1Encodable? = null
        private set

    private constructor(tagged: ASN1TaggedObject) {
        when (tagged.tagNo) {
            encryptedPrivKey -> value = EncryptedKey.getInstance(tagged.`object`)
            keyGenParameters -> value = ASN1OctetString.getInstance(tagged, false)
            archiveRemGenPrivKey -> value = ASN1Boolean.getInstance(tagged, false)
            else -> throw IllegalArgumentException("unknown tag number: " + tagged.tagNo)
        }
    }

    constructor(encKey: EncryptedKey) {
        this.value = encKey
    }

    constructor(keyGenParameters: ASN1OctetString) {
        this.value = keyGenParameters
    }

    constructor(archiveRemGenPrivKey: Boolean) {
        this.value = ASN1Boolean.getInstance(archiveRemGenPrivKey)
    }

    val type: Int
        get() {
            if (value is EncryptedKey) {
                return encryptedPrivKey
            }

            if (value is ASN1OctetString) {
                return keyGenParameters
            }

            return archiveRemGenPrivKey
        }

    /**
     *
     * PKIArchiveOptions ::= CHOICE {
     * encryptedPrivKey     [0] EncryptedKey,
     * -- the actual value of the private key
     * keyGenParameters     [1] KeyGenParameters,
     * -- parameters which allow the private key to be re-generated
     * archiveRemGenPrivKey [2] BOOLEAN }
     * -- set to TRUE if sender wishes receiver to archive the private
     * -- key of a key pair that the receiver generates in response to
     * -- this request; set to FALSE if no archival is desired.
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (value is EncryptedKey) {
            return DERTaggedObject(true, encryptedPrivKey, value)  // choice
        }

        if (value is ASN1OctetString) {
            return DERTaggedObject(false, keyGenParameters, value)
        }

        return DERTaggedObject(false, archiveRemGenPrivKey, value)
    }

    companion object {
        val encryptedPrivKey = 0
        val keyGenParameters = 1
        val archiveRemGenPrivKey = 2

        fun getInstance(o: Any?): PKIArchiveOptions {
            if (o == null || o is PKIArchiveOptions) {
                return o as PKIArchiveOptions?
            } else if (o is ASN1TaggedObject) {
                return PKIArchiveOptions(o as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("unknown object: " + o)
        }
    }
}
