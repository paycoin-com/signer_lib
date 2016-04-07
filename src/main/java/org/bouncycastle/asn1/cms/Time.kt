package org.bouncycastle.asn1.cms

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTCTime
import org.bouncycastle.asn1.DERGeneralizedTime
import org.bouncycastle.asn1.DERUTCTime

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-11.3):
 * Dual-mode timestamp format producing either UTCTIme or GeneralizedTime.
 *
 *
 *
 * Time ::= CHOICE {
 * utcTime        UTCTime,
 * generalTime    GeneralizedTime }
 *
 *
 *
 * This has a constructor using java.util.Date for input which generates
 * a [DERUTCTime][org.bouncycastle.asn1.DERUTCTime] object if the
 * supplied datetime is in range 1950-01-01-00:00:00 UTC until 2049-12-31-23:59:60 UTC.
 * If the datetime value is outside that range, the generated object will be
 * [DERGeneralizedTime][org.bouncycastle.asn1.DERGeneralizedTime].
 */
class Time : ASN1Object, ASN1Choice {
    internal var time: ASN1Primitive


    @Deprecated("use getInstance()")
    constructor(
            time: ASN1Primitive) {
        if (time !is ASN1UTCTime && time !is ASN1GeneralizedTime) {
            throw IllegalArgumentException("unknown object passed to Time")
        }

        this.time = time
    }

    /**
     * Creates a time object from a given date - if the date is between 1950
     * and 2049 a UTCTime object is generated, otherwise a GeneralizedTime
     * is used.

     * @param time a date object representing the time of interest.
     */
    constructor(
            time: Date) {
        val tz = SimpleTimeZone(0, "Z")
        val dateF = SimpleDateFormat("yyyyMMddHHmmss")

        dateF.timeZone = tz

        val d = dateF.format(time) + "Z"
        val year = Integer.parseInt(d.substring(0, 4))

        if (year < 1950 || year > 2049) {
            this.time = DERGeneralizedTime(d)
        } else {
            this.time = DERUTCTime(d.substring(2))
        }
    }

    /**
     * Creates a time object from a given date and locale - if the date is between 1950
     * and 2049 a UTCTime object is generated, otherwise a GeneralizedTime
     * is used. You may need to use this constructor if the default locale
     * doesn't use a Gregorian calender so that the GeneralizedTime produced is compatible with other ASN.1 implementations.

     * @param time a date object representing the time of interest.
     * *
     * @param locale an appropriate Locale for producing an ASN.1 GeneralizedTime value.
     */
    constructor(
            time: Date,
            locale: Locale) {
        val tz = SimpleTimeZone(0, "Z")
        val dateF = SimpleDateFormat("yyyyMMddHHmmss", locale)

        dateF.timeZone = tz

        val d = dateF.format(time) + "Z"
        val year = Integer.parseInt(d.substring(0, 4))

        if (year < 1950 || year > 2049) {
            this.time = DERGeneralizedTime(d)
        } else {
            this.time = DERUTCTime(d.substring(2))
        }
    }

    /**
     * Get the date+tine as a String in full form century format.
     */
    fun getTime(): String {
        if (time is ASN1UTCTime) {
            return (time as ASN1UTCTime).adjustedTime
        } else {
            return (time as ASN1GeneralizedTime).time
        }
    }

    /**
     * Get java.util.Date version of date+time.
     */
    // this should never happen
    val date: Date
        get() {
            try {
                if (time is ASN1UTCTime) {
                    return (time as ASN1UTCTime).adjustedDate
                } else {
                    return (time as ASN1GeneralizedTime).date
                }
            } catch (e: ParseException) {
                throw IllegalStateException("invalid date string: " + e.message)
            }

        }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return time
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): Time {
            return getInstance(obj.`object`)
        }

        /**
         * Return a Time object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [Time] object
         *  *  [DERUTCTime][org.bouncycastle.asn1.DERUTCTime] object
         *  *  [DERGeneralizedTime][org.bouncycastle.asn1.DERGeneralizedTime] object
         *

         * @param obj the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): Time {
            if (obj == null || obj is Time) {
                return obj as Time?
            } else if (obj is ASN1UTCTime) {
                return Time(obj as ASN1UTCTime?)
            } else if (obj is ASN1GeneralizedTime) {
                return Time(obj as ASN1GeneralizedTime?)
            }

            throw IllegalArgumentException("unknown object in factory: " + obj.javaClass.name)
        }
    }
}
