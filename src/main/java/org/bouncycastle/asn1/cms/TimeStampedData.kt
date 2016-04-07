package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.DERIA5String

/**
 * [RFC 5544](http://tools.ietf.org/html/rfc5544):
 * Binding Documents with Time-Stamps; TimeStampedData object.
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
class TimeStampedData : ASN1Object {
    private var version: ASN1Integer? = null
    var dataUri: DERIA5String? = null
        private set
    var metaData: MetaData? = null
        private set
    var content: ASN1OctetString? = null
        private set
    var temporalEvidence: Evidence? = null
        private set

    constructor(dataUri: DERIA5String, metaData: MetaData, content: ASN1OctetString, temporalEvidence: Evidence) {
        this.version = ASN1Integer(1)
        this.dataUri = dataUri
        this.metaData = metaData
        this.content = content
        this.temporalEvidence = temporalEvidence
    }

    private constructor(seq: ASN1Sequence) {
        this.version = ASN1Integer.getInstance(seq.getObjectAt(0))

        var index = 1
        if (seq.getObjectAt(index) is DERIA5String) {
            this.dataUri = DERIA5String.getInstance(seq.getObjectAt(index++))
        }
        if (seq.getObjectAt(index) is MetaData || seq.getObjectAt(index) is ASN1Sequence) {
            this.metaData = MetaData.getInstance(seq.getObjectAt(index++))
        }
        if (seq.getObjectAt(index) is ASN1OctetString) {
            this.content = ASN1OctetString.getInstance(seq.getObjectAt(index++))
        }
        this.temporalEvidence = Evidence.getInstance(seq.getObjectAt(index))
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)

        if (dataUri != null) {
            v.add(dataUri)
        }

        if (metaData != null) {
            v.add(metaData)
        }

        if (content != null) {
            v.add(content)
        }

        v.add(temporalEvidence)

        return BERSequence(v)
    }

    companion object {

        /**
         * Return a TimeStampedData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [RecipientKeyIdentifier] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with TimeStampedData structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(obj: Any?): TimeStampedData {
            if (obj == null || obj is TimeStampedData) {
                return obj as TimeStampedData?
            }
            return TimeStampedData(ASN1Sequence.getInstance(obj))
        }
    }
}
