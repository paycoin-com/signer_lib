package org.bouncycastle.asn1.pkcs

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

class PBES2Parameters : ASN1Object, PKCSObjectIdentifiers {
    var keyDerivationFunc: KeyDerivationFunc? = null
        private set
    var encryptionScheme: EncryptionScheme? = null
        private set

    constructor(keyDevFunc: KeyDerivationFunc, encScheme: EncryptionScheme) {
        this.keyDerivationFunc = keyDevFunc
        this.encryptionScheme = encScheme
    }

    private constructor(
            obj: ASN1Sequence) {
        val e = obj.objects
        val funcSeq = ASN1Sequence.getInstance((e.nextElement() as ASN1Encodable).toASN1Primitive())

        if (funcSeq.getObjectAt(0) == PKCSObjectIdentifiers.id_PBKDF2) {
            keyDerivationFunc = KeyDerivationFunc(PKCSObjectIdentifiers.id_PBKDF2, PBKDF2Params.getInstance(funcSeq.getObjectAt(1)))
        } else {
            keyDerivationFunc = KeyDerivationFunc.getInstance(funcSeq)
        }

        encryptionScheme = EncryptionScheme.getInstance(e.nextElement())
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(keyDerivationFunc)
        v.add(encryptionScheme)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): PBES2Parameters? {
            if (obj is PBES2Parameters) {
                return obj
            }
            if (obj != null) {
                return PBES2Parameters(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
