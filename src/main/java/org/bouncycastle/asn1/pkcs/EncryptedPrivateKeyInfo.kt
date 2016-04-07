package org.bouncycastle.asn1.pkcs

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class EncryptedPrivateKeyInfo : ASN1Object {
    var encryptionAlgorithm: AlgorithmIdentifier? = null
        private set
    private var data: ASN1OctetString? = null

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        encryptionAlgorithm = AlgorithmIdentifier.getInstance(e.nextElement())
        data = ASN1OctetString.getInstance(e.nextElement())
    }

    constructor(
            algId: AlgorithmIdentifier,
            encoding: ByteArray) {
        this.encryptionAlgorithm = algId
        this.data = DEROctetString(encoding)
    }

    val encryptedData: ByteArray
        get() = data!!.octets

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * EncryptedPrivateKeyInfo ::= SEQUENCE {
     * encryptionAlgorithm AlgorithmIdentifier {{KeyEncryptionAlgorithms}},
     * encryptedData EncryptedData
     * }

     * EncryptedData ::= OCTET STRING

     * KeyEncryptionAlgorithms ALGORITHM-IDENTIFIER ::= {
     * ... -- For local profiles
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(encryptionAlgorithm)
        v.add(data)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): EncryptedPrivateKeyInfo? {
            if (obj is EncryptedPrivateKeyInfo) {
                return obj
            } else if (obj != null) {
                return EncryptedPrivateKeyInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
