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

package com.itextpdf.xmp

import java.util.Calendar
import java.util.TimeZone


/**
 * The `XMPDateTime`-class represents a point in time up to a resolution of nano
 * seconds. Dates and time in the serialized XMP are ISO 8601 strings. There are utility functions
 * to convert to the ISO format, a `Calendar` or get the Timezone. The fields of
 * `XMPDateTime` are:
 *
 *  *  month - The month in the range 1..12.
 *  *  day - The day of the month in the range 1..31.
 *  *  minute - The minute in the range 0..59.
 *  *  hour - The time zone hour in the range 0..23.
 *  *  minute - The time zone minute in the range 0..59.
 *  *  nanoSecond - The nano seconds within a second. *Note:* if the XMPDateTime is
 * converted into a calendar, the resolution is reduced to milli seconds.
 *  *  timeZone - a `TimeZone`-object.
 *
 * DateTime values are occasionally used in cases with only a date or only a time component. A date
 * without a time has zeros for all the time fields. A time without a date has zeros for all date
 * fields (year, month, and day).
 */
interface XMPDateTime : Comparable<Any> {
    /** @return Returns the year, can be negative.
     */
    /** @param year Sets the year
     */
    var year: Int

    /** @return Returns The month in the range 1..12.
     */
    /** @param month Sets the month 1..12
     */
    var month: Int

    /** @return Returns the day of the month in the range 1..31.
     */
    /** @param day Sets the day 1..31
     */
    var day: Int

    /** @return Returns hour - The hour in the range 0..23.
     */
    /** @param hour Sets the hour in the range 0..23.
     */
    var hour: Int

    /** @return Returns the minute in the range 0..59.
     */
    /** @param minute Sets the minute in the range 0..59.
     */
    var minute: Int

    /** @return Returns the second in the range 0..59.
     */
    /** @param second Sets the second in the range 0..59.
     */
    var second: Int

    /**
     * @return Returns milli-, micro- and nano seconds.
     * * 		   Nanoseconds within a second, often left as zero?
     */
    /**
     * @param nanoSecond Sets the milli-, micro- and nano seconds.
     * *		Granularity goes down to milli seconds.
     */
    var nanoSecond: Int

    /** @return Returns the time zone.
     */
    /** @param tz a time zone to set
     */
    var timeZone: TimeZone

    /**
     * This flag is set either by parsing or by setting year, month or day.
     * @return Returns true if the XMPDateTime object has a date portion.
     */
    fun hasDate(): Boolean

    /**
     * This flag is set either by parsing or by setting hours, minutes, seconds or milliseconds.
     * @return Returns true if the XMPDateTime object has a time portion.
     */
    fun hasTime(): Boolean

    /**
     * This flag is set either by parsing or by setting hours, minutes, seconds or milliseconds.
     * @return Returns true if the XMPDateTime object has a defined timezone.
     */
    fun hasTimeZone(): Boolean

    /**
     * @return Returns a `Calendar` (only with milli second precision).
     * *  		*Note:* the dates before Oct 15th 1585 (which normally fall into validity of
     * *  		the Julian calendar) are also rendered internally as Gregorian dates.
     */
    val calendar: Calendar

    /**
     * @return Returns the ISO 8601 string representation of the date and time.
     */
    val isO8601String: String
}