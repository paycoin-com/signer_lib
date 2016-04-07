package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class EncryptionScheme : ASN1Object {
    private var algId: AlgorithmIdentifier? = null

    constructor(
            objectId: ASN1ObjectIdentifier,
            parameters: ASN1Encodable) {
        this.algId = AlgorithmIdentifier(objectId, parameters)
    }

    private constructor(
            seq: ASN1Sequence) {
        this.algId = AlgorithmIdentifier.getInstance(seq)
    }

    val algorithm: ASN1ObjectIdentifier
        get() = algId!!.algorithm

    val parameters: ASN1Encodable
        get() = algId!!.parameters

    override fun toASN1Primitive(): ASN1Primitive {
        return algId!!.toASN1Primitive()
    }

    companion object {

        fun getInstance(obj: Any?): EncryptionScheme? {
            if (obj is EncryptionScheme) {
                return obj
            } else if (obj != null) {
                return EncryptionScheme(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
