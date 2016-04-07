package org.bouncycastle.asn1.cms

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1TaggedObjectParser

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-3) [ContentInfo] object parser.

 *
 * ContentInfo ::= SEQUENCE {
 * contentType ContentType,
 * content [0] EXPLICIT ANY DEFINED BY contentType OPTIONAL }
 *
 */
class ContentInfoParser @Throws(IOException::class)
constructor(
        seq: ASN1SequenceParser) {
    val contentType: ASN1ObjectIdentifier
    private val content: ASN1TaggedObjectParser?

    init {
        contentType = seq.readObject() as ASN1ObjectIdentifier
        content = seq.readObject() as ASN1TaggedObjectParser
    }

    @Throws(IOException::class)
    fun getContent(
            tag: Int): ASN1Encodable? {
        if (content != null) {
            return content.getObjectParser(tag, true)
        }

        return null
    }
}
