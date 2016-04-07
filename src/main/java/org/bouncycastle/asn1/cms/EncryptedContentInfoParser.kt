package org.bouncycastle.asn1.cms

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1TaggedObjectParser
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * Parser for [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.1) EncryptedContentInfo object.
 *
 *
 *
 * EncryptedContentInfo ::= SEQUENCE {
 * contentType ContentType,
 * contentEncryptionAlgorithm ContentEncryptionAlgorithmIdentifier,
 * encryptedContent [0] IMPLICIT EncryptedContent OPTIONAL
 * }
 *
 */
class EncryptedContentInfoParser @Throws(IOException::class)
constructor(
        seq: ASN1SequenceParser) {
    val contentType: ASN1ObjectIdentifier
    val contentEncryptionAlgorithm: AlgorithmIdentifier
    private val _encryptedContent: ASN1TaggedObjectParser

    init {
        contentType = seq.readObject() as ASN1ObjectIdentifier
        contentEncryptionAlgorithm = AlgorithmIdentifier.getInstance(seq.readObject().toASN1Primitive())
        _encryptedContent = seq.readObject() as ASN1TaggedObjectParser
    }

    @Throws(IOException::class)
    fun getEncryptedContent(
            tag: Int): ASN1Encodable {
        return _encryptedContent.getObjectParser(tag, false)
    }
}
