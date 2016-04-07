package org.bouncycastle.asn1.cms

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1SetParser
import org.bouncycastle.asn1.ASN1TaggedObjectParser
import org.bouncycastle.asn1.BERTags

/**
 * Parser of [RFC 5652](http://tools.ietf.org/html/rfc5652#section-6.1) [EnvelopedData] object.
 *
 *
 *
 * EnvelopedData ::= SEQUENCE {
 * version CMSVersion,
 * originatorInfo [0] IMPLICIT OriginatorInfo OPTIONAL,
 * recipientInfos RecipientInfos,
 * encryptedContentInfo EncryptedContentInfo,
 * unprotectedAttrs [1] IMPLICIT UnprotectedAttributes OPTIONAL
 * }
 *
 */
class EnvelopedDataParser @Throws(IOException::class)
constructor(
        private val _seq: ASN1SequenceParser) {
    val version: ASN1Integer
    private var _nextObject: ASN1Encodable? = null
    private var _originatorInfoCalled: Boolean = false

    init {
        this.version = ASN1Integer.getInstance(_seq.readObject())
    }

    val originatorInfo: OriginatorInfo?
        @Throws(IOException::class)
        get() {
            _originatorInfoCalled = true

            if (_nextObject == null) {
                _nextObject = _seq.readObject()
            }

            if (_nextObject is ASN1TaggedObjectParser && (_nextObject as ASN1TaggedObjectParser).tagNo == 0) {
                val originatorInfo = (_nextObject as ASN1TaggedObjectParser).getObjectParser(BERTags.SEQUENCE, false) as ASN1SequenceParser
                _nextObject = null
                return OriginatorInfo.getInstance(originatorInfo.toASN1Primitive())
            }

            return null
        }

    val recipientInfos: ASN1SetParser
        @Throws(IOException::class)
        get() {
            if (!_originatorInfoCalled) {
                originatorInfo
            }

            if (_nextObject == null) {
                _nextObject = _seq.readObject()
            }

            val recipientInfos = _nextObject as ASN1SetParser?
            _nextObject = null
            return recipientInfos
        }

    val encryptedContentInfo: EncryptedContentInfoParser?
        @Throws(IOException::class)
        get() {
            if (_nextObject == null) {
                _nextObject = _seq.readObject()
            }


            if (_nextObject != null) {
                val o = _nextObject as ASN1SequenceParser?
                _nextObject = null
                return EncryptedContentInfoParser(o)
            }

            return null
        }

    val unprotectedAttrs: ASN1SetParser?
        @Throws(IOException::class)
        get() {
            if (_nextObject == null) {
                _nextObject = _seq.readObject()
            }


            if (_nextObject != null) {
                val o = _nextObject
                _nextObject = null
                return (o as ASN1TaggedObjectParser).getObjectParser(BERTags.SET, false) as ASN1SetParser
            }

            return null
        }
}
