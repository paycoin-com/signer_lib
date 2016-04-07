package org.bouncycastle.asn1

import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * - * UTC time object.
 * Internal facade of [ASN1UTCTime].
 *
 *
 * This datatype is valid only from 1950-01-01 00:00:00 UTC until 2049-12-31 23:59:59 UTC.
 *
 *
 *
 *
 * **X.690**
 *
 * **11: Restrictions on BER employed by both CER and DER**
 *
 * **11.8 UTCTime **
 * **11.8.1** The encoding shall terminate with "Z",
 * as described in the ITU-T X.680 | ISO/IEC 8824-1 clause on UTCTime.
 *
 *
 * **11.8.2** The seconds element shall always be present.
 *
 *
 * **11.8.3** Midnight (GMT) shall be represented in the form:
 *
 * "YYMMDD000000Z"
 *
 * where "YYMMDD" represents the day following the midnight in question.
 */
open class ASN1UTCTime : ASN1Primitive {
    private var time: ByteArray? = null

    /**
     * The correct format for this is YYMMDDHHMMSSZ (it used to be that seconds were
     * never encoded. When you're creating one of these objects from scratch, that's
     * what you want to use, otherwise we'll try to deal with whatever gets read from
     * the input stream... (this is why the input format is different from the getTime()
     * method output).
     *
     *

     * @param time the time string.
     */
    constructor(
            time: String) {
        this.time = Strings.toByteArray(time)
        try {
            this.date
        } catch (e: ParseException) {
            throw IllegalArgumentException("invalid date string: " + e.message)
        }

    }

    /**
     * base constructor from a java.util.date object
     * @param time the Date to build the time from.
     */
    constructor(
            time: Date) {
        val dateF = SimpleDateFormat("yyMMddHHmmss'Z'")

        dateF.timeZone = SimpleTimeZone(0, "Z")

        this.time = Strings.toByteArray(dateF.format(time))
    }

    /**
     * Base constructor from a java.util.date and Locale - you may need to use this if the default locale
     * doesn't use a Gregorian calender so that the GeneralizedTime produced is compatible with other ASN.1 implementations.

     * @param time a date object representing the time of interest.
     * *
     * @param locale an appropriate Locale for producing an ASN.1 UTCTime value.
     */
    constructor(
            time: Date,
            locale: Locale) {
        val dateF = SimpleDateFormat("yyMMddHHmmss'Z'", locale)

        dateF.timeZone = SimpleTimeZone(0, "Z")

        this.time = Strings.toByteArray(dateF.format(time))
    }

    internal constructor(
            time: ByteArray) {
        this.time = time
    }

    /**
     * return the time as a date based on whatever a 2 digit year will return. For
     * standardised processing use getAdjustedDate().

     * @return the resulting date
     * *
     * @exception ParseException if the date string cannot be parsed.
     */
    val date: Date
        @Throws(ParseException::class)
        get() {
            val dateF = SimpleDateFormat("yyMMddHHmmssz")

            return dateF.parse(getTime())
        }

    /**
     * return the time as an adjusted date
     * in the range of 1950 - 2049.

     * @return a date in the range of 1950 to 2049.
     * *
     * @exception ParseException if the date string cannot be parsed.
     */
    val adjustedDate: Date
        @Throws(ParseException::class)
        get() {
            val dateF = SimpleDateFormat("yyyyMMddHHmmssz")

            dateF.timeZone = SimpleTimeZone(0, "Z")

            return dateF.parse(adjustedTime)
        }

    /**
     * return the time - always in the form of
     * YYMMDDhhmmssGMT(+hh:mm|-hh:mm).
     *
     *
     * Normally in a certificate we would expect "Z" rather than "GMT",
     * however adding the "GMT" means we can just use:
     *
     * dateF = new SimpleDateFormat("yyMMddHHmmssz");
     *
     * To read in the time and get a date which is compatible with our local
     * time zone.
     *
     *
     * **Note:** In some cases, due to the local date processing, this
     * may lead to unexpected results. If you want to stick the normal
     * convention of 1950 to 2049 use the getAdjustedTime() method.
     */
    fun getTime(): String {
        val stime = Strings.fromByteArray(time)

        //
        // standardise the format.
        //
        if (stime.indexOf('-') < 0 && stime.indexOf('+') < 0) {
            if (stime.length == 11) {
                return stime.substring(0, 10) + "00GMT+00:00"
            } else {
                return stime.substring(0, 12) + "GMT+00:00"
            }
        } else {
            var index = stime.indexOf('-')
            if (index < 0) {
                index = stime.indexOf('+')
            }
            var d = stime

            if (index == stime.length - 3) {
                d += "00"
            }

            if (index == 10) {
                return d.substring(0, 10) + "00GMT" + d.substring(10, 13) + ":" + d.substring(13, 15)
            } else {
                return d.substring(0, 12) + "GMT" + d.substring(12, 15) + ":" + d.substring(15, 17)
            }
        }
    }

    /**
     * return a time string as an adjusted date with a 4 digit year. This goes
     * in the range of 1950 - 2049.
     */
    val adjustedTime: String
        get() {
            val d = this.getTime()

            if (d[0] < '5') {
                return "20" + d
            } else {
                return "19" + d
            }
        }

    internal override val isConstructed: Boolean
        get() = false

    internal override fun encodedLength(): Int {
        val length = time!!.size

        return 1 + StreamUtil.calculateBodyLength(length) + length
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        out.write(BERTags.UTC_TIME)

        val length = time!!.size

        out.writeLength(length)

        for (i in 0..length - 1) {
            out.write(time!![i].toByte().toInt())
        }
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1UTCTime) {
            return false
        }

        return Arrays.areEqual(time, o.time)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(time)
    }

    override fun toString(): String {
        return Strings.fromByteArray(time)
    }

    companion object {

        /**
         * return an UTC Time from the passed in object.

         * @param obj an ASN1UTCTime or an object that can be converted into one.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         * *
         * @return an ASN1UTCTime instance, or null.
         */
        fun getInstance(
                obj: Any?): ASN1UTCTime {
            if (obj == null || obj is ASN1UTCTime) {
                return obj as ASN1UTCTime?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as ASN1UTCTime
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an UTC Time from a tagged object.

         * @param obj the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *              tagged false otherwise.
         * *
         * @exception IllegalArgumentException if the tagged object cannot
         * *               be converted.
         * *
         * @return an ASN1UTCTime instance, or null.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1UTCTime {
            val o = obj.`object`

            if (explicit || o is ASN1UTCTime) {
                return getInstance(o)
            } else {
                return ASN1UTCTime((o as ASN1OctetString).octets)
            }
        }
    }
}
