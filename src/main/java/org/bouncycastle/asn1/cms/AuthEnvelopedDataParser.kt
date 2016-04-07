package org.bouncycastle.asn1.cms

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1ParsingException
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1SetParser
import org.bouncycastle.asn1.ASN1TaggedObjectParser
import org.bouncycastle.asn1.BERTags

/**
 * Parse [AuthEnvelopedData] input stream.

 *
 * AuthEnvelopedData ::= SEQUENCE {
 * version CMSVersion,
 * originatorInfo [0] IMPLICIT OriginatorInfo OPTIONAL,
 * recipientInfos RecipientInfos,
 * authEncryptedContentInfo EncryptedContentInfo,
 * authAttrs [1] IMPLICIT AuthAttributes OPTIONAL,
 * mac MessageAuthenticationCode,
 * unauthAttrs [2] IMPLICIT UnauthAttributes OPTIONAL }
 *
 */
class AuthEnvelopedDataParser @Throws(IOException::class)
constructor(private val seq: ASN1SequenceParser) {
    val version: ASN1Integer
    private var nextObject: ASN1Encodable? = null
    private var originatorInfoCalled: Boolean = false
    private var authEncryptedContentInfoParser: EncryptedContentInfoParser? = null

    init {

        // "It MUST be set to 0."
        this.version = ASN1Integer.getInstance(seq.readObject())
        if (this.version.value.toInt() != 0) {
            throw ASN1ParsingException("AuthEnvelopedData version number must be 0")
        }
    }

    val originatorInfo: OriginatorInfo?
        @Throws(IOException::class)
        get() {
            originatorInfoCalled = true

            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            if (nextObject is ASN1TaggedObjectParser && (nextObject as ASN1TaggedObjectParser).tagNo == 0) {
                val originatorInfo = (nextObject as ASN1TaggedObjectParser).getObjectParser(BERTags.SEQUENCE, false) as ASN1SequenceParser
                nextObject = null
                return OriginatorInfo.getInstance(originatorInfo.toASN1Primitive())
            }

            return null
        }

    val recipientInfos: ASN1SetParser
        @Throws(IOException::class)
        get() {
            if (!originatorInfoCalled) {
                originatorInfo
            }

            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            val recipientInfos = nextObject as ASN1SetParser?
            nextObject = null
            return recipientInfos
        }

    val authEncryptedContentInfo: EncryptedContentInfoParser?
        @Throws(IOException::class)
        get() {
            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            if (nextObject != null) {
                val o = nextObject as ASN1SequenceParser?
                nextObject = null
                authEncryptedContentInfoParser = EncryptedContentInfoParser(o)
                return authEncryptedContentInfoParser
            }

            return null
        }

    // "The authAttrs MUST be present if the content type carried in
    // EncryptedContentInfo is not id-data."
    val authAttrs: ASN1SetParser?
        @Throws(IOException::class)
        get() {
            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            if (nextObject is ASN1TaggedObjectParser) {
                val o = nextObject
                nextObject = null
                return (o as ASN1TaggedObjectParser).getObjectParser(BERTags.SET, false) as ASN1SetParser
            }
            if (authEncryptedContentInfoParser!!.contentType != CMSObjectIdentifiers.data) {
                throw ASN1ParsingException("authAttrs must be present with non-data content")
            }

            return null
        }

    val mac: ASN1OctetString
        @Throws(IOException::class)
        get() {
            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            val o = nextObject
            nextObject = null

            return ASN1OctetString.getInstance(o.toASN1Primitive())
        }

    val unauthAttrs: ASN1SetParser?
        @Throws(IOException::class)
        get() {
            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            if (nextObject != null) {
                val o = nextObject
                nextObject = null
                return (o as ASN1TaggedObjectParser).getObjectParser(BERTags.SET, false) as ASN1SetParser
            }

            return null
        }
}
