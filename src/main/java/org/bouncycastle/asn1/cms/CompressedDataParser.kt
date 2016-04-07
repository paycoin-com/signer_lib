package org.bouncycastle.asn1.cms

import java.io.IOException

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * Parser of [RFC 3274](http://tools.ietf.org/html/rfc3274) [CompressedData] object.
 *
 *
 *
 * CompressedData ::= SEQUENCE {
 * version CMSVersion,
 * compressionAlgorithm CompressionAlgorithmIdentifier,
 * encapContentInfo EncapsulatedContentInfo
 * }
 *
 */
class CompressedDataParser @Throws(IOException::class)
constructor(
        seq: ASN1SequenceParser) {
    val version: ASN1Integer
    val compressionAlgorithmIdentifier: AlgorithmIdentifier
    val encapContentInfo: ContentInfoParser

    init {
        this.version = seq.readObject() as ASN1Integer
        this.compressionAlgorithmIdentifier = AlgorithmIdentifier.getInstance(seq.readObject().toASN1Primitive())
        this.encapContentInfo = ContentInfoParser(seq.readObject() as ASN1SequenceParser)
    }
}
