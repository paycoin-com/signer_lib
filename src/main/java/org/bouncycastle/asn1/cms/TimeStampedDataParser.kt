package org.bouncycastle.asn1.cms

import java.io.IOException

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1OctetStringParser
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.DERIA5String

/**
 * Parser for [RFC 5544](http://tools.ietf.org/html/rfc5544):
 * [TimeStampedData] object.
 *
 *
 *
 * TimeStampedData ::= SEQUENCE {
 * version              INTEGER { v1(1) },
 * dataUri              IA5String OPTIONAL,
 * metaData             MetaData OPTIONAL,
 * content              OCTET STRING OPTIONAL,
 * temporalEvidence     Evidence
 * }
 *
 */
class TimeStampedDataParser @Throws(IOException::class)
private constructor(private val parser: ASN1SequenceParser) {
    private val version: ASN1Integer
    var dataUri: DERIA5String? = null
        private set
    var metaData: MetaData? = null
        private set
    var content: ASN1OctetStringParser? = null
        private set
    private var temporalEvidence: Evidence? = null

    init {
        this.version = ASN1Integer.getInstance(parser.readObject())

        var obj = parser.readObject()

        if (obj is DERIA5String) {
            this.dataUri = DERIA5String.getInstance(obj)
            obj = parser.readObject()
        }
        if (obj is MetaData || obj is ASN1SequenceParser) {
            this.metaData = MetaData.getInstance(obj.toASN1Primitive())
            obj = parser.readObject()
        }
        if (obj is ASN1OctetStringParser) {
            this.content = obj
        }
    }

    @Throws(IOException::class)
    fun getTemporalEvidence(): Evidence {
        if (temporalEvidence == null) {
            temporalEvidence = Evidence.getInstance(parser.readObject().toASN1Primitive())
        }

        return temporalEvidence
    }

    companion object {

        @Throws(IOException::class)
        fun getInstance(obj: Any): TimeStampedDataParser? {
            if (obj is ASN1Sequence) {
                return TimeStampedDataParser(obj.parser())
            }
            if (obj is ASN1SequenceParser) {
                return TimeStampedDataParser(obj)
            }

            return null
        }
    }
}
