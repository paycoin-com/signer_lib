package org.bouncycastle.asn1

import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone
import java.util.TimeZone

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings

/**
 * Base class representing the ASN.1 GeneralizedTime type.
 *
 *
 * The main difference between these and UTC time is a 4 digit year.
 *
 */
open class ASN1GeneralizedTime : ASN1Primitive {
    private var time: ByteArray? = null

    /**
     * The correct format for this is YYYYMMDDHHMMSS[.f]Z, or without the Z
     * for local time, or Z+-HHMM on the end, for difference between local
     * time and UTC time. The fractional second amount f must consist of at
     * least one number with trailing zeroes removed.

     * @param time the time string.
     * *
     * @throws IllegalArgumentException if String is an illegal format.
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
     * Base constructor from a java.util.date object

     * @param time a date object representing the time of interest.
     */
    constructor(
            time: Date) {
        val dateF = SimpleDateFormat("yyyyMMddHHmmss'Z'")

        dateF.timeZone = SimpleTimeZone(0, "Z")

        this.time = Strings.toByteArray(dateF.format(time))
    }

    /**
     * Base constructor from a java.util.date and Locale - you may need to use this if the default locale
     * doesn't use a Gregorian calender so that the GeneralizedTime produced is compatible with other ASN.1 implementations.

     * @param time a date object representing the time of interest.
     * *
     * @param locale an appropriate Locale for producing an ASN.1 GeneralizedTime value.
     */
    constructor(
            time: Date,
            locale: Locale) {
        val dateF = SimpleDateFormat("yyyyMMddHHmmss'Z'", locale)

        dateF.timeZone = SimpleTimeZone(0, "Z")

        this.time = Strings.toByteArray(dateF.format(time))
    }

    internal constructor(
            bytes: ByteArray) {
        this.time = bytes
    }

    /**
     * Return the time.

     * @return The time string as it appeared in the encoded object.
     */
    val timeString: String
        get() = Strings.fromByteArray(time)

    /**
     * return the time - always in the form of
     * YYYYMMDDhhmmssGMT(+hh:mm|-hh:mm).
     *
     *
     * Normally in a certificate we would expect "Z" rather than "GMT",
     * however adding the "GMT" means we can just use:
     *
     * dateF = new SimpleDateFormat("yyyyMMddHHmmssz");
     *
     * To read in the time and get a date which is compatible with our local
     * time zone.
     *
     * @return a String representation of the time.
     */
    fun getTime(): String {
        val stime = Strings.fromByteArray(time)

        //
        // standardise the format.
        //
        if (stime[stime.length - 1] == 'Z') {
            return stime.substring(0, stime.length - 1) + "GMT+00:00"
        } else {
            var signPos = stime.length - 5
            var sign = stime[signPos]
            if (sign == '-' || sign == '+') {
                return stime.substring(0, signPos)
                +"GMT"
                +stime.substring(signPos, signPos + 3)
                +":"
                +stime.substring(signPos + 3)
            } else {
                signPos = stime.length - 3
                sign = stime[signPos]
                if (sign == '-' || sign == '+') {
                    return stime.substring(0, signPos)
                    +"GMT"
                    +stime.substring(signPos)
                    +":00"
                }
            }
        }
        return stime + calculateGMTOffset()
    }

    private fun calculateGMTOffset(): String {
        var sign = "+"
        val timeZone = TimeZone.getDefault()
        var offset = timeZone.rawOffset
        if (offset < 0) {
            sign = "-"
            offset = -offset
        }
        var hours = offset / (60 * 60 * 1000)
        val minutes = (offset - hours * 60 * 60 * 1000) / (60 * 1000)

        try {
            if (timeZone.useDaylightTime() && timeZone.inDaylightTime(this.date)) {
                hours += if (sign == "+") 1 else -1
            }
        } catch (e: ParseException) {
            // we'll do our best and ignore daylight savings
        }

        return "GMT" + sign + convert(hours) + ":" + convert(minutes)
    }

    private fun convert(time: Int): String {
        if (time < 10) {
            return "0" + time
        }

        return Integer.toString(time)
    }

    // java misinterprets extra digits as being milliseconds...
    val date: Date
        @Throws(ParseException::class)
        get() {
            val dateF: SimpleDateFormat
            val stime = Strings.fromByteArray(time)
            var d = stime

            if (stime.endsWith("Z")) {
                if (hasFractionalSeconds()) {
                    dateF = SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'")
                } else {
                    dateF = SimpleDateFormat("yyyyMMddHHmmss'Z'")
                }

                dateF.timeZone = SimpleTimeZone(0, "Z")
            } else if (stime.indexOf('-') > 0 || stime.indexOf('+') > 0) {
                d = this.getTime()
                if (hasFractionalSeconds()) {
                    dateF = SimpleDateFormat("yyyyMMddHHmmss.SSSz")
                } else {
                    dateF = SimpleDateFormat("yyyyMMddHHmmssz")
                }

                dateF.timeZone = SimpleTimeZone(0, "Z")
            } else {
                if (hasFractionalSeconds()) {
                    dateF = SimpleDateFormat("yyyyMMddHHmmss.SSS")
                } else {
                    dateF = SimpleDateFormat("yyyyMMddHHmmss")
                }

                dateF.timeZone = SimpleTimeZone(0, TimeZone.getDefault().id)
            }

            if (hasFractionalSeconds()) {
                var frac = d.substring(14)
                var index: Int
                index = 1
                while (index < frac.length) {
                    val ch = frac[index]
                    if (!('0' <= ch && ch <= '9')) {
                        break
                    }
                    index++
                }

                if (index - 1 > 3) {
                    frac = frac.substring(0, 4) + frac.substring(index)
                    d = d.substring(0, 14) + frac
                } else if (index - 1 == 1) {
                    frac = frac.substring(0, index) + "00" + frac.substring(index)
                    d = d.substring(0, 14) + frac
                } else if (index - 1 == 2) {
                    frac = frac.substring(0, index) + "0" + frac.substring(index)
                    d = d.substring(0, 14) + frac
                }
            }

            return dateF.parse(d)
        }

    private fun hasFractionalSeconds(): Boolean {
        for (i in time!!.indices) {
            if (time!![i] == '.') {
                if (i == 14) {
                    return true
                }
            }
        }
        return false
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
        out.writeEncoded(BERTags.GENERALIZED_TIME, time)
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1GeneralizedTime) {
            return false
        }

        return Arrays.areEqual(time, o.time)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(time)
    }

    companion object {

        /**
         * return a generalized time from the passed in object

         * @param obj an ASN1GeneralizedTime or an object that can be converted into one.
         * *
         * @return an ASN1GeneralizedTime instance, or null.
         * *
         * @throws IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                obj: Any?): ASN1GeneralizedTime {
            if (obj == null || obj is ASN1GeneralizedTime) {
                return obj as ASN1GeneralizedTime?
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj as ByteArray?) as ASN1GeneralizedTime
                } catch (e: Exception) {
                    throw IllegalArgumentException("encoding error in getInstance: " + e.toString())
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return a Generalized Time object from a tagged object.

         * @param obj      the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *                 tagged false otherwise.
         * *
         * @return an ASN1GeneralizedTime instance.
         * *
         * @throws IllegalArgumentException if the tagged object cannot
         * * be converted.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1GeneralizedTime {
            val o = obj.`object`

            if (explicit || o is ASN1GeneralizedTime) {
                return getInstance(o)
            } else {
                return ASN1GeneralizedTime((o as ASN1OctetString).octets)
            }
        }
    }
}
