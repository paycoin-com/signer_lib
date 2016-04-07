package org.bouncycastle.asn1.cms

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.CertificateList

/**
 * [RFC 5544](http://tools.ietf.org/html/rfc5544)
 * Binding Documents with Time-Stamps; TimeStampAndCRL object.
 *
 * TimeStampAndCRL ::= SEQUENCE {
 * timeStamp   TimeStampToken,          -- according to RFC 3161
 * crl         CertificateList OPTIONAL -- according to RFC 5280
 * }
 *
 */
class TimeStampAndCRL : ASN1Object {
    var timeStampToken: ContentInfo? = null
        private set

    var certificateList: CertificateList? = null
        private set

    constructor(timeStamp: ContentInfo) {
        this.timeStampToken = timeStamp
    }

    private constructor(seq: ASN1Sequence) {
        this.timeStampToken = ContentInfo.getInstance(seq.getObjectAt(0))
        if (seq.size() == 2) {
            this.certificateList = CertificateList.getInstance(seq.getObjectAt(1))
        }
    }

    val crl: CertificateList
        get() = this.certificateList

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(timeStampToken)

        if (certificateList != null) {
            v.add(certificateList)
        }

        return DERSequence(v)
    }

    companion object {

        /**
         * Return a TimeStampAndCRL object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [TimeStampAndCRL] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with TimeStampAndCRL structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(obj: Any?): TimeStampAndCRL? {
            if (obj is TimeStampAndCRL) {
                return obj
            } else if (obj != null) {
                return TimeStampAndCRL(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
