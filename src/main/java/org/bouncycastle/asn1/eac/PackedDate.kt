package org.bouncycastle.asn1.eac

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone

import org.bouncycastle.util.Arrays

/**
 * EAC encoding date object
 */
class PackedDate {
    var encoding: ByteArray? = null
        private set

    constructor(
            time: String) {
        this.encoding = convert(time)
    }

    /**
     * Base constructor from a java.util.date object.

     * @param time a date object representing the time of interest.
     */
    constructor(
            time: Date) {
        val dateF = SimpleDateFormat("yyMMdd'Z'")

        dateF.timeZone = SimpleTimeZone(0, "Z")

        this.encoding = convert(dateF.format(time))
    }

    /**
     * Base constructor from a java.util.date object. You may need to use this constructor if the default locale
     * doesn't use a Gregorian calender so that the PackedDate produced is compatible with other ASN.1 implementations.

     * @param time a date object representing the time of interest.
     * *
     * @param locale an appropriate Locale for producing an ASN.1 GeneralizedTime value.
     */
    constructor(
            time: Date,
            locale: Locale) {
        val dateF = SimpleDateFormat("yyMMdd'Z'", locale)

        dateF.timeZone = SimpleTimeZone(0, "Z")

        this.encoding = convert(dateF.format(time))
    }

    private fun convert(sTime: String): ByteArray {
        val digs = sTime.toCharArray()
        val date = ByteArray(6)

        for (i in 0..5) {
            date[i] = (digs[i] - '0').toByte()
        }

        return date
    }

    internal constructor(
            bytes: ByteArray) {
        this.encoding = bytes
    }

    /**
     * return the time as a date based on whatever a 2 digit year will return. For
     * standardised processing use getAdjustedDate().

     * @return the resulting date
     * *
     * @exception java.text.ParseException if the date string cannot be parsed.
     */
    val date: Date
        @Throws(ParseException::class)
        get() {
            val dateF = SimpleDateFormat("yyyyMMdd")

            return dateF.parse("20" + toString())
        }

    override fun hashCode(): Int {
        return Arrays.hashCode(encoding)
    }

    override fun equals(o: Any?): Boolean {
        if (o !is PackedDate) {
            return false
        }

        return Arrays.areEqual(encoding, o.encoding)
    }

    override fun toString(): String {
        val dateC = CharArray(encoding!!.size)

        for (i in dateC.indices) {
            dateC[i] = ((encoding!![i] and 0xff) + '0').toChar()
        }

        return String(dateC)
    }
}
