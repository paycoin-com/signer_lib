package org.bouncycastle.asn1.eac

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Sequence

abstract class PublicKeyDataObject : ASN1Object() {

    abstract val usage: ASN1ObjectIdentifier

    companion object {
        fun getInstance(obj: Any?): PublicKeyDataObject? {
            if (obj is PublicKeyDataObject) {
                return obj
            }
            if (obj != null) {
                val seq = ASN1Sequence.getInstance(obj)
                val usage = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0))

                if (usage.on(EACObjectIdentifiers.id_TA_ECDSA)) {
                    return ECDSAPublicKey(seq)
                } else {
                    return RSAPublicKey(seq)
                }
            }

            return null
        }
    }
}
