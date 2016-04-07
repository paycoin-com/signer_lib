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
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

import com.itextpdf.xmp.impl.XMPDateTimeImpl


/**
 * A factory to create `XMPDateTime`-instances from a `Calendar` or an
 * ISO 8601 string or for the current time.

 * @since 16.02.2006
 */
object XMPDateTimeFactory {
    /** The UTC TimeZone  */
    private val UTC = TimeZone.getTimeZone("UTC")


    /**
     * Creates an `XMPDateTime` from a `Calendar`-object.

     * @param calendar a `Calendar`-object.
     * *
     * @return An `XMPDateTime`-object.
     */
    fun createFromCalendar(calendar: Calendar): XMPDateTime {
        return XMPDateTimeImpl(calendar)
    }


    /**
     * Creates an empty `XMPDateTime`-object.
     * @return Returns an `XMPDateTime`-object.
     */
    fun create(): XMPDateTime {
        return XMPDateTimeImpl()
    }


    /**
     * Creates an `XMPDateTime`-object from initial values.
     * @param year years
     * *
     * @param month months from 1 to 12
     * * *Note:* Remember that the month in [Calendar] is defined from 0 to 11.
     * *
     * @param day days
     * *
     * @return Returns an `XMPDateTime`-object.
     */
    fun create(year: Int, month: Int, day: Int): XMPDateTime {
        val dt = XMPDateTimeImpl()
        dt.year = year
        dt.month = month
        dt.day = day
        return dt
    }


    /**
     * Creates an `XMPDateTime`-object from initial values.
     * @param year years
     * *
     * @param month months from 1 to 12
     * * *Note:* Remember that the month in [Calendar] is defined from 0 to 11.
     * *
     * @param day days
     * *
     * @param hour hours
     * *
     * @param minute minutes
     * *
     * @param second seconds
     * *
     * @param nanoSecond nanoseconds
     * *
     * @return Returns an `XMPDateTime`-object.
     */
    fun create(year: Int, month: Int, day: Int,
               hour: Int, minute: Int, second: Int, nanoSecond: Int): XMPDateTime {
        val dt = XMPDateTimeImpl()
        dt.year = year
        dt.month = month
        dt.day = day
        dt.hour = hour
        dt.minute = minute
        dt.second = second
        dt.nanoSecond = nanoSecond
        return dt
    }


    /**
     * Creates an `XMPDateTime` from an ISO 8601 string.

     * @param strValue The ISO 8601 string representation of the date/time.
     * *
     * @return An `XMPDateTime`-object.
     * *
     * @throws XMPException When the ISO 8601 string is non-conform
     */
    @Throws(XMPException::class)
    fun createFromISO8601(strValue: String): XMPDateTime {
        return XMPDateTimeImpl(strValue)
    }


    /**
     * Obtain the current date and time.

     * @return Returns The returned time is UTC, properly adjusted for the local time zone. The
     * *         resolution of the time is not guaranteed to be finer than seconds.
     */
    val currentDateTime: XMPDateTime
        get() = XMPDateTimeImpl(GregorianCalendar())


    /**
     * Sets the local time zone without touching any other Any existing time zone value is replaced,
     * the other date/time fields are not adjusted in any way.

     * @param dateTime the `XMPDateTime` variable containing the value to be modified.
     * *
     * @return Returns an updated `XMPDateTime`-object.
     */
    fun setLocalTimeZone(dateTime: XMPDateTime): XMPDateTime {
        val cal = dateTime.calendar
        cal.timeZone = TimeZone.getDefault()
        return XMPDateTimeImpl(cal)
    }


    /**
     * Make sure a time is UTC. If the time zone is not UTC, the time is
     * adjusted and the time zone set to be UTC.

     * @param dateTime
     * *            the `XMPDateTime` variable containing the time to
     * *            be modified.
     * *
     * @return Returns an updated `XMPDateTime`-object.
     */
    fun convertToUTCTime(dateTime: XMPDateTime): XMPDateTime {
        val timeInMillis = dateTime.calendar.timeInMillis
        val cal = GregorianCalendar(UTC)
        cal.gregorianChange = Date(java.lang.Long.MIN_VALUE)
        cal.timeInMillis = timeInMillis
        return XMPDateTimeImpl(cal)
    }


    /**
     * Make sure a time is local. If the time zone is not the local zone, the time is adjusted and
     * the time zone set to be local.

     * @param dateTime the `XMPDateTime` variable containing the time to be modified.
     * *
     * @return Returns an updated `XMPDateTime`-object.
     */
    fun convertToLocalTime(dateTime: XMPDateTime): XMPDateTime {
        val timeInMillis = dateTime.calendar.timeInMillis
        // has automatically local timezone
        val cal = GregorianCalendar()
        cal.timeInMillis = timeInMillis
        return XMPDateTimeImpl(cal)
    }
}
/** Private constructor  */
// EMPTY