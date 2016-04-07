package org.bouncycastle.asn1.cms

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1SetParser
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1TaggedObjectParser
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * Parse [AuthenticatedData] stream.
 *
 * AuthenticatedData ::= SEQUENCE {
 * version CMSVersion,
 * originatorInfo [0] IMPLICIT OriginatorInfo OPTIONAL,
 * recipientInfos RecipientInfos,
 * macAlgorithm MessageAuthenticationCodeAlgorithm,
 * digestAlgorithm [1] DigestAlgorithmIdentifier OPTIONAL,
 * encapContentInfo EncapsulatedContentInfo,
 * authAttrs [2] IMPLICIT AuthAttributes OPTIONAL,
 * mac MessageAuthenticationCode,
 * unauthAttrs [3] IMPLICIT UnauthAttributes OPTIONAL }

 * AuthAttributes ::= SET SIZE (1..MAX) OF Attribute

 * UnauthAttributes ::= SET SIZE (1..MAX) OF Attribute

 * MessageAuthenticationCode ::= OCTET STRING
 *
 */
class AuthenticatedDataParser @Throws(IOException::class)
constructor(
        private val seq: ASN1SequenceParser) {
    val version: ASN1Integer
    private var nextObject: ASN1Encodable? = null
    private var originatorInfoCalled: Boolean = false

    init {
        this.version = ASN1Integer.getInstance(seq.readObject())
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

    val macAlgorithm: AlgorithmIdentifier?
        @Throws(IOException::class)
        get() {
            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            if (nextObject != null) {
                val o = nextObject as ASN1SequenceParser?
                nextObject = null
                return AlgorithmIdentifier.getInstance(o.toASN1Primitive())
            }

            return null
        }

    val digestAlgorithm: AlgorithmIdentifier?
        @Throws(IOException::class)
        get() {
            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            if (nextObject is ASN1TaggedObjectParser) {
                val obj = AlgorithmIdentifier.getInstance(nextObject!!.toASN1Primitive() as ASN1TaggedObject, false)
                nextObject = null
                return obj
            }

            return null
        }


    val enapsulatedContentInfo: ContentInfoParser
        @Deprecated("use getEncapsulatedContentInfo()")
        @Throws(IOException::class)
        get() = encapsulatedContentInfo

    val encapsulatedContentInfo: ContentInfoParser?
        @Throws(IOException::class)
        get() {
            if (nextObject == null) {
                nextObject = seq.readObject()
            }

            if (nextObject != null) {
                val o = nextObject as ASN1SequenceParser?
                nextObject = null
                return ContentInfoParser(o)
            }

            return null
        }

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
