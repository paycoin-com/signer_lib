package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x509.GeneralName

class EncKeyWithID : ASN1Object {
    val privateKey: PrivateKeyInfo
    val identifier: ASN1Encodable?

    private constructor(seq: ASN1Sequence) {
        this.privateKey = PrivateKeyInfo.getInstance(seq.getObjectAt(0))

        if (seq.size() > 1) {
            if (seq.getObjectAt(1) !is DERUTF8String) {
                this.identifier = GeneralName.getInstance(seq.getObjectAt(1))
            } else {
                this.identifier = seq.getObjectAt(1) as ASN1Encodable
            }
        } else {
            this.identifier = null
        }
    }

    constructor(privKeyInfo: PrivateKeyInfo) {
        this.privateKey = privKeyInfo
        this.identifier = null
    }

    constructor(privKeyInfo: PrivateKeyInfo, str: DERUTF8String) {
        this.privateKey = privKeyInfo
        this.identifier = str
    }

    constructor(privKeyInfo: PrivateKeyInfo, generalName: GeneralName) {
        this.privateKey = privKeyInfo
        this.identifier = generalName
    }

    fun hasIdentifier(): Boolean {
        return identifier != null
    }

    val isIdentifierUTF8String: Boolean
        get() = identifier is DERUTF8String

    /**
     *
     * EncKeyWithID ::= SEQUENCE {
     * privateKey           PrivateKeyInfo,
     * identifier CHOICE {
     * string               UTF8String,
     * generalName          GeneralName
     * } OPTIONAL
     * }
     *
     * @return a DERSequence representing the value in this object.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(privateKey)

        if (identifier != null) {
            v.add(identifier)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): EncKeyWithID? {
            if (o is EncKeyWithID) {
                return o
            } else if (o != null) {
                return EncKeyWithID(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}
