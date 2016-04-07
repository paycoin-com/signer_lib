package org.bouncycastle.asn1.cms

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

/**
 * [RFC 5544](http://tools.ietf.org/html/rfc5544)
 * Binding Documents with Time-Stamps; TimeStampTokenEvidence object.
 *
 * TimeStampTokenEvidence ::=
 * SEQUENCE SIZE(1..MAX) OF TimeStampAndCRL
 *
 */
class TimeStampTokenEvidence : ASN1Object {
    private var timeStampAndCRLs: Array<TimeStampAndCRL>? = null

    constructor(timeStampAndCRLs: Array<TimeStampAndCRL>) {
        this.timeStampAndCRLs = timeStampAndCRLs
    }

    constructor(timeStampAndCRL: TimeStampAndCRL) {
        this.timeStampAndCRLs = arrayOfNulls<TimeStampAndCRL>(1)

        timeStampAndCRLs[0] = timeStampAndCRL
    }

    private constructor(seq: ASN1Sequence) {
        this.timeStampAndCRLs = arrayOfNulls<TimeStampAndCRL>(seq.size())

        var count = 0

        val en = seq.objects
        while (en.hasMoreElements()) {
            timeStampAndCRLs[count++] = TimeStampAndCRL.getInstance(en.nextElement())
        }
    }

    fun toTimeStampAndCRLArray(): Array<TimeStampAndCRL> {
        return timeStampAndCRLs
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        for (i in timeStampAndCRLs!!.indices) {
            v.add(timeStampAndCRLs!![i])
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(tagged: ASN1TaggedObject, explicit: Boolean): TimeStampTokenEvidence {
            return getInstance(ASN1Sequence.getInstance(tagged, explicit))
        }

        /**
         * Return a TimeStampTokenEvidence object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [TimeStampTokenEvidence] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with TimeStampTokenEvidence structure inside
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(obj: Any?): TimeStampTokenEvidence? {
            if (obj is TimeStampTokenEvidence) {
                return obj
            } else if (obj != null) {
                return TimeStampTokenEvidence(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }

}
