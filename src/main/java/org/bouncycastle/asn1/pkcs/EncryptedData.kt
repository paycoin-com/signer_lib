package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.BERTaggedObject
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * The EncryptedData object.
 *
 * EncryptedData ::= SEQUENCE {
 * version Version,
 * encryptedContentInfo EncryptedContentInfo
 * }


 * EncryptedContentInfo ::= SEQUENCE {
 * contentType ContentType,
 * contentEncryptionAlgorithm  ContentEncryptionAlgorithmIdentifier,
 * encryptedContent [0] IMPLICIT EncryptedContent OPTIONAL
 * }

 * EncryptedContent ::= OCTET STRING
 *
 */
class EncryptedData : ASN1Object {
    internal var data: ASN1Sequence
    internal var bagId: ASN1ObjectIdentifier
    internal var bagValue: ASN1Primitive

    private constructor(
            seq: ASN1Sequence) {
        val version = (seq.getObjectAt(0) as ASN1Integer).value.toInt()

        if (version != 0) {
            throw IllegalArgumentException("sequence not version 0")
        }

        this.data = ASN1Sequence.getInstance(seq.getObjectAt(1))
    }

    constructor(
            contentType: ASN1ObjectIdentifier,
            encryptionAlgorithm: AlgorithmIdentifier,
            content: ASN1Encodable) {
        val v = ASN1EncodableVector()

        v.add(contentType)
        v.add(encryptionAlgorithm.toASN1Primitive())
        v.add(BERTaggedObject(false, 0, content))

        data = BERSequence(v)
    }

    val contentType: ASN1ObjectIdentifier
        get() = ASN1ObjectIdentifier.getInstance(data.getObjectAt(0))

    val encryptionAlgorithm: AlgorithmIdentifier
        get() = AlgorithmIdentifier.getInstance(data.getObjectAt(1))

    val content: ASN1OctetString?
        get() {
            if (data.size() == 3) {
                val o = ASN1TaggedObject.getInstance(data.getObjectAt(2))

                return ASN1OctetString.getInstance(o, false)
            }

            return null
        }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(ASN1Integer(0))
        v.add(data)

        return BERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): EncryptedData? {
            if (obj is EncryptedData) {
                return obj
            }

            if (obj != null) {
                return EncryptedData(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
