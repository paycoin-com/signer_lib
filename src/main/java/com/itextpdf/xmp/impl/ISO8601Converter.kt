//Copyright (c) 2006, Adobe Systems Incorporated
//All rights reserved.
//
//        Redistribution and use in source and binary forms, with or without
//        modification, are permitted provided that the following conditions are met:
//        1. Redistributions of source code must retain the above copyright
//        notice, this list of conditions and the following disclaimer.
//        2. Redistributions in binary form must reproduce the above copyright
//        notice, this list of conditions and the following disclaimer in the
//        documentation and/or other materials provided with the distribution.
//        3. All advertising materials mentioning features or use of this software
//        must display the following acknowledgement:
//        This product includes software developed by the Adobe Systems Incorporated.
//        4. Neither the name of the Adobe Systems Incorporated nor the
//        names of its contributors may be used to endorse or promote products
//        derived from this software without specific prior written permission.
//
//        THIS SOFTWARE IS PROVIDED BY ADOBE SYSTEMS INCORPORATED ''AS IS'' AND ANY
//        EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//        WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//        DISCLAIMED. IN NO EVENT SHALL ADOBE SYSTEMS INCORPORATED BE LIABLE FOR ANY
//        DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//        (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//        LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
//        ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//        (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//        SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//        http://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html

package com.itextpdf.xmp.impl

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.SimpleTimeZone

import com.itextpdf.xmp.XMPDateTime
import com.itextpdf.xmp.XMPError
import com.itextpdf.xmp.XMPException


/**
 * Converts between ISO 8601 Strings and `Calendar` with millisecond resolution.

 * @since   16.02.2006
 */
object ISO8601Converter {


    /**
     * @param iso8601String a date string that is ISO 8601 conform.
     * *
     * @param binValue an existing XMPDateTime to set with the parsed date
     * *
     * @return Returns an XMPDateTime-object containing the ISO8601-date.
     * *
     * @throws XMPException Is thrown when the string is non-conform.
     */
    @Throws(XMPException::class)
    @JvmOverloads fun parse(iso8601String: String?, binValue: XMPDateTime = XMPDateTimeImpl()): XMPDateTime {
        if (iso8601String == null) {
            throw XMPException("Parameter must not be null", XMPError.BADPARAM)
        } else if (iso8601String.length == 0) {
            return binValue
        }

        val input = ParseState(iso8601String)
        var value: Int

        if (input.ch(0) == '-') {
            input.skip()
        }

        // Extract the year.
        value = input.gatherInt("Invalid year in date string", 9999)
        if (input.hasNext() && input.ch() != '-') {
            throw XMPException("Invalid date string, after year", XMPError.BADVALUE)
        }

        if (input.ch(0) == '-') {
            value = -value
        }
        binValue.year = value
        if (!input.hasNext()) {
            return binValue
        }
        input.skip()


        // Extract the month.
        value = input.gatherInt("Invalid month in date string", 12)
        if (input.hasNext() && input.ch() != '-') {
            throw XMPException("Invalid date string, after month", XMPError.BADVALUE)
        }
        binValue.month = value
        if (!input.hasNext()) {
            return binValue
        }
        input.skip()


        // Extract the day.
        value = input.gatherInt("Invalid day in date string", 31)
        if (input.hasNext() && input.ch() != 'T') {
            throw XMPException("Invalid date string, after day", XMPError.BADVALUE)
        }
        binValue.day = value
        if (!input.hasNext()) {
            return binValue
        }
        input.skip()

        // Extract the hour.
        value = input.gatherInt("Invalid hour in date string", 23)
        binValue.hour = value
        if (!input.hasNext()) {
            return binValue
        }

        // Extract the minute.
        if (input.ch() == ':') {
            input.skip()
            value = input.gatherInt("Invalid minute in date string", 59)
            if (input.hasNext() &&
                    input.ch() != ':' && input.ch() != 'Z' && input.ch() != '+' && input.ch() != '-') {
                throw XMPException("Invalid date string, after minute", XMPError.BADVALUE)
            }
            binValue.minute = value
        }

        if (!input.hasNext()) {
            return binValue
        } else if (input.hasNext() && input.ch() == ':') {
            input.skip()
            value = input.gatherInt("Invalid whole seconds in date string", 59)
            if (input.hasNext() && input.ch() != '.' && input.ch() != 'Z' &&
                    input.ch() != '+' && input.ch() != '-') {
                throw XMPException("Invalid date string, after whole seconds",
                        XMPError.BADVALUE)
            }
            binValue.second = value
            if (input.ch() == '.') {
                input.skip()
                var digits = input.pos()
                value = input.gatherInt("Invalid fractional seconds in date string", 999999999)
                if (input.hasNext() && input.ch() != 'Z' && input.ch() != '+' && input.ch() != '-') {
                    throw XMPException("Invalid date string, after fractional second",
                            XMPError.BADVALUE)
                }
                digits = input.pos() - digits
                while (digits > 9) {
                    value = value / 10
                    --digits
                }
                while (digits < 9) {
                    value = value * 10
                    ++digits
                }
                binValue.nanoSecond = value
            }
        } else if (input.ch() != 'Z' && input.ch() != '+' && input.ch() != '-') {
            throw XMPException("Invalid date string, after time", XMPError.BADVALUE)
        }


        var tzSign = 0
        var tzHour = 0
        var tzMinute = 0

        if (!input.hasNext()) {
            // no Timezone at all
            return binValue
        } else if (input.ch() == 'Z') {
            input.skip()
        } else if (input.hasNext()) {
            if (input.ch() == '+') {
                tzSign = 1
            } else if (input.ch() == '-') {
                tzSign = -1
            } else {
                throw XMPException("Time zone must begin with 'Z', '+', or '-'",
                        XMPError.BADVALUE)
            }

            input.skip()
            // Extract the time zone hour.
            tzHour = input.gatherInt("Invalid time zone hour in date string", 23)
            if (input.hasNext()) {
                if (input.ch() == ':') {
                    input.skip()

                    // Extract the time zone minute.
                    tzMinute = input.gatherInt("Invalid time zone minute in date string", 59)
                } else {
                    throw XMPException("Invalid date string, after time zone hour",
                            XMPError.BADVALUE)
                }
            }
        }

        // create a corresponding TZ and set it time zone
        val offset = (tzHour * 3600 * 1000 + tzMinute * 60 * 1000) * tzSign
        binValue.timeZone = SimpleTimeZone(offset, "")

        if (input.hasNext()) {
            throw XMPException(
                    "Invalid date string, extra chars at end", XMPError.BADVALUE)
        }

        return binValue
    }


    /**
     * Converts a `Calendar` into an ISO 8601 string.
     * Format a date according to ISO 8601 and http://www.w3.org/TR/NOTE-datetime:
     *
     *  * YYYY
     *  * YYYY-MM
     *  * YYYY-MM-DD
     *  * YYYY-MM-DDThh:mmTZD
     *  * YYYY-MM-DDThh:mm:ssTZD
     *  * YYYY-MM-DDThh:mm:ss.sTZD
     *

     * Data fields:
     *
     *  * YYYY = four-digit year
     *  * MM	 = two-digit month (01=January, etc.)
     *  * DD	 = two-digit day of month (01 through 31)
     *  * hh	 = two digits of hour (00 through 23)
     *  * mm	 = two digits of minute (00 through 59)
     *  * ss	 = two digits of second (00 through 59)
     *  * s	 = one or more digits representing a decimal fraction of a second
     *  * TZD	 = time zone designator (Z or +hh:mm or -hh:mm)
     *
     *
     *
     * *Note:* ISO 8601 does not seem to allow years less than 1000 or greater than 9999.
     * We allow any year, even negative ones. The year is formatted as "%.4d".
     *
     *
     * *Note:* Fix for bug 1269463 (silently fix out of range values) included in parsing.
     * The quasi-bogus "time only" values from Photoshop CS are not supported.

     * @param dateTime an XMPDateTime-object.
     * *
     * @return Returns an ISO 8601 string.
     */
    fun render(dateTime: XMPDateTime): String {
        val buffer = StringBuffer()

        if (dateTime.hasDate()) {
            // year is rendered in any case, even 0000
            val df = DecimalFormat("0000", DecimalFormatSymbols(Locale.ENGLISH))
            buffer.append(df.format(dateTime.year.toLong()))
            if (dateTime.month == 0) {
                return buffer.toString()
            }

            // month
            df.applyPattern("'-'00")
            buffer.append(df.format(dateTime.month.toLong()))
            if (dateTime.day == 0) {
                return buffer.toString()
            }

            // day
            buffer.append(df.format(dateTime.day.toLong()))

            // time, rendered if any time field is not zero
            if (dateTime.hasTime()) {
                // hours and minutes
                buffer.append('T')
                df.applyPattern("00")
                buffer.append(df.format(dateTime.hour.toLong()))
                buffer.append(':')
                buffer.append(df.format(dateTime.minute.toLong()))

                // seconds and nanoseconds
                if (dateTime.second != 0 || dateTime.nanoSecond != 0) {
                    val seconds = dateTime.second + dateTime.nanoSecond / 1e9.0

                    df.applyPattern(":00.#########")
                    buffer.append(df.format(seconds))
                }

                // time zone
                if (dateTime.hasTimeZone()) {
                    // used to calculate the time zone offset incl. Daylight Savings
                    val timeInMillis = dateTime.calendar.timeInMillis
                    val offset = dateTime.timeZone.getOffset(timeInMillis)
                    if (offset == 0) {
                        // UTC
                        buffer.append('Z')
                    } else {
                        val thours = offset / 3600000
                        val tminutes = Math.abs(offset % 3600000 / 60000)
                        df.applyPattern("+00;-00")
                        buffer.append(df.format(thours.toLong()))
                        df.applyPattern(":00")
                        buffer.append(df.format(tminutes.toLong()))
                    }
                }
            }
        }
        return buffer.toString()
    }


}
/** Hides public constructor  */
// EMPTY
/**
 * Converts an ISO 8601 string to an `XMPDateTime`.

 * Parse a date according to ISO 8601 and
 * http://www.w3.org/TR/NOTE-datetime:
 *
 *  * YYYY
 *  * YYYY-MM
 *  * YYYY-MM-DD
 *  * YYYY-MM-DDThh:mmTZD
 *  * YYYY-MM-DDThh:mm:ssTZD
 *  * YYYY-MM-DDThh:mm:ss.sTZD
 *

 * Data fields:
 *
 *  * YYYY = four-digit year
 *  * MM = two-digit month (01=January, etc.)
 *  * DD = two-digit day of month (01 through 31)
 *  * hh = two digits of hour (00 through 23)
 *  * mm = two digits of minute (00 through 59)
 *  * ss = two digits of second (00 through 59)
 *  * s = one or more digits representing a decimal fraction of a second
 *  * TZD = time zone designator (Z or +hh:mm or -hh:mm)
 *

 * Note that ISO 8601 does not seem to allow years less than 1000 or greater
 * than 9999. We allow any year, even negative ones. The year is formatted
 * as "%.4d".
 *
 *
 * *Note:* Tolerate missing TZD, assume is UTC. Photoshop 8 writes
 * dates like this for exif:GPSTimeStamp.
 * *Note:* DOES NOT APPLY ANYMORE.
 * Tolerate missing date portion, in case someone foolishly
 * writes a time-only value that way.

 * @param iso8601String a date string that is ISO 8601 conform.
 * *
 * @return Returns a `Calendar`.
 * *
 * @throws XMPException Is thrown when the string is non-conform.
 */


/**
 * @since   22.08.2006
 */
internal class ParseState
/**
 * @param str initializes the parser container
 */
(
        /**  */
        private val str: String) {
    /**  */
    private var pos = 0


    /**
     * @return Returns the length of the input.
     */
    fun length(): Int {
        return str.length
    }


    /**
     * @return Returns whether there are more chars to come.
     */
    operator fun hasNext(): Boolean {
        return pos < str.length
    }


    /**
     * @param index index of char
     * *
     * @return Returns char at a certain index.
     */
    fun ch(index: Int): Char {
        return if (index < str.length)
            str[index]
        else
            0x0000
    }


    /**
     * @return Returns the current char or 0x0000 if there are no more chars.
     */
    fun ch(): Char {
        return if (pos < str.length)
            str[pos]
        else
            0x0000
    }


    /**
     * Skips the next char.
     */
    fun skip() {
        pos++
    }


    /**
     * @return Returns the current position.
     */
    fun pos(): Int {
        return pos
    }


    /**
     * Parses a integer from the source and sets the pointer after it.
     * @param errorMsg Error message to put in the exception if no number can be found
     * *
     * @param maxValue the max value of the number to return
     * *
     * @return Returns the parsed integer.
     * *
     * @throws XMPException Thrown if no integer can be found.
     */
    @Throws(XMPException::class)
    fun gatherInt(errorMsg: String, maxValue: Int): Int {
        var value = 0
        var success = false
        var ch = ch(pos)
        while ('0' <= ch && ch <= '9') {
            value = value * 10 + (ch - '0')
            success = true
            pos++
            ch = ch(pos)
        }

        if (success) {
            if (value > maxValue) {
                return maxValue
            } else if (value < 0) {
                return 0
            } else {
                return value
            }
        } else {
            throw XMPException(errorMsg, XMPError.BADVALUE)
        }
    }
}	


