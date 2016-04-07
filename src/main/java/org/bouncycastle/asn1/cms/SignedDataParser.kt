package org.bouncycastle.asn1.cms

import java.io.IOException

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1SetParser
import org.bouncycastle.asn1.ASN1TaggedObjectParser
import org.bouncycastle.asn1.BERTags

/**
 * Parser for [RFC 5652](http://tools.ietf.org/html/rfc5652#section-5.1): [SignedData] object.
 *
 *
 *
 * SignedData ::= SEQUENCE {
 * version CMSVersion,
 * digestAlgorithms DigestAlgorithmIdentifiers,
 * encapContentInfo EncapsulatedContentInfo,
 * certificates [0] IMPLICIT CertificateSet OPTIONAL,
 * crls [1] IMPLICIT CertificateRevocationLists OPTIONAL,
 * signerInfos SignerInfos
 * }
 *
 */
class SignedDataParser @Throws(IOException::class)
private constructor(
        private val _seq: ASN1SequenceParser) {
    val version: ASN1Integer
    private var _nextObject: Any? = null
    private var _certsCalled: Boolean = false
    private var _crlsCalled: Boolean = false

    init {
        this.version = _seq.readObject() as ASN1Integer
    }

    val digestAlgorithms: ASN1SetParser
        @Throws(IOException::class)
        get() {
            val o = _seq.readObject()

            if (o is ASN1Set) {
                return o.parser()
            }

            return o as ASN1SetParser
        }

    val encapContentInfo: ContentInfoParser
        @Throws(IOException::class)
        get() = ContentInfoParser(_seq.readObject() as ASN1SequenceParser)

    val certificates: ASN1SetParser?
        @Throws(IOException::class)
        get() {
            _certsCalled = true
            _nextObject = _seq.readObject()

            if (_nextObject is ASN1TaggedObjectParser && (_nextObject as ASN1TaggedObjectParser).tagNo == 0) {
                val certs = (_nextObject as ASN1TaggedObjectParser).getObjectParser(BERTags.SET, false) as ASN1SetParser
                _nextObject = null

                return certs
            }

            return null
        }

    val crls: ASN1SetParser?
        @Throws(IOException::class)
        get() {
            if (!_certsCalled) {
                throw IOException("getCerts() has not been called.")
            }

            _crlsCalled = true

            if (_nextObject == null) {
                _nextObject = _seq.readObject()
            }

            if (_nextObject is ASN1TaggedObjectParser && (_nextObject as ASN1TaggedObjectParser).tagNo == 1) {
                val crls = (_nextObject as ASN1TaggedObjectParser).getObjectParser(BERTags.SET, false) as ASN1SetParser
                _nextObject = null

                return crls
            }

            return null
        }

    val signerInfos: ASN1SetParser
        @Throws(IOException::class)
        get() {
            if (!_certsCalled || !_crlsCalled) {
                throw IOException("getCerts() and/or getCrls() has not been called.")
            }

            if (_nextObject == null) {
                _nextObject = _seq.readObject()
            }

            return _nextObject as ASN1SetParser?
        }

    companion object {

        @Throws(IOException::class)
        fun getInstance(
                o: Any): SignedDataParser {
            if (o is ASN1Sequence) {
                return SignedDataParser(o.parser())
            }
            if (o is ASN1SequenceParser) {
                return SignedDataParser(o)
            }

            throw IOException("unknown object encountered: " + o.javaClass.name)
        }
    }
}
