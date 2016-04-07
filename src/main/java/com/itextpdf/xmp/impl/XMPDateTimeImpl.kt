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

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

import com.itextpdf.xmp.XMPDateTime
import com.itextpdf.xmp.XMPException


/**
 * The implementation of `XMPDateTime`. Internally a `calendar` is used
 * plus an additional nano seconds field, because `Calendar` supports only milli
 * seconds. The `nanoSeconds` convers only the resolution beyond a milli second.

 * @since 16.02.2006
 */
class XMPDateTimeImpl : XMPDateTime {
    /**  */
    private var year = 0
    /**  */
    private var month = 0
    /**  */
    private var day = 0
    /**  */
    private var hour = 0
    /**  */
    private var minute = 0
    /**  */
    private var second = 0
    /** Use NO time zone as default  */
    private var timeZone: TimeZone? = null
    /**
     * The nano seconds take micro and nano seconds, while the milli seconds are in the calendar.
     */
    private var nanoSeconds: Int = 0
    /**  */
    private var hasDate = false
    /**  */
    private var hasTime = false
    /**  */
    private var hasTimeZone = false


    /**
     * Creates an `XMPDateTime`-instance with the current time in the default time
     * zone.
     */
    constructor() {
        // EMPTY
    }


    /**
     * Creates an `XMPDateTime`-instance from a calendar.

     * @param calendar a `Calendar`
     */
    constructor(calendar: Calendar) {
        // extract the date and timezone from the calendar provided
        val date = calendar.time
        val zone = calendar.timeZone

        // put that date into a calendar the pretty much represents ISO8601
        // I use US because it is close to the "locale" for the ISO8601 spec
        val intCalendar = Calendar.getInstance(Locale.US) as GregorianCalendar
        intCalendar.gregorianChange = Date(java.lang.Long.MIN_VALUE)
        intCalendar.timeZone = zone
        intCalendar.time = date

        this.year = intCalendar.get(Calendar.YEAR)
        this.month = intCalendar.get(Calendar.MONTH) + 1 // cal is from 0..12
        this.day = intCalendar.get(Calendar.DAY_OF_MONTH)
        this.hour = intCalendar.get(Calendar.HOUR_OF_DAY)
        this.minute = intCalendar.get(Calendar.MINUTE)
        this.second = intCalendar.get(Calendar.SECOND)
        this.nanoSeconds = intCalendar.get(Calendar.MILLISECOND) * 1000000
        this.timeZone = intCalendar.timeZone

        // object contains all date components
        hasDate = hasTime = hasTimeZone = true
    }


    /**
     * Creates an `XMPDateTime`-instance from
     * a `Date` and a `TimeZone`.

     * @param date a date describing an absolute point in time
     * *
     * @param timeZone a TimeZone how to interpret the date
     */
    constructor(date: Date, timeZone: TimeZone) {
        val calendar = GregorianCalendar(timeZone)
        calendar.time = date

        this.year = calendar.get(Calendar.YEAR)
        this.month = calendar.get(Calendar.MONTH) + 1 // cal is from 0..12
        this.day = calendar.get(Calendar.DAY_OF_MONTH)
        this.hour = calendar.get(Calendar.HOUR_OF_DAY)
        this.minute = calendar.get(Calendar.MINUTE)
        this.second = calendar.get(Calendar.SECOND)
        this.nanoSeconds = calendar.get(Calendar.MILLISECOND) * 1000000
        this.timeZone = timeZone

        // object contains all date components
        hasDate = hasTime = hasTimeZone = true
    }


    /**
     * Creates an `XMPDateTime`-instance from an ISO 8601 string.

     * @param strValue an ISO 8601 string
     * *
     * @throws XMPException If the string is a non-conform ISO 8601 string, an exception is thrown
     */
    @Throws(XMPException::class)
    constructor(strValue: String) {
        ISO8601Converter.parse(strValue, this)
    }


    /**
     * @see XMPDateTime.getYear
     */
    override fun getYear(): Int {
        return year
    }


    /**
     * @see XMPDateTime.setYear
     */
    override fun setYear(year: Int) {
        this.year = Math.min(Math.abs(year), 9999)
        this.hasDate = true
    }


    /**
     * @see XMPDateTime.getMonth
     */
    override fun getMonth(): Int {
        return month
    }


    /**
     * @see XMPDateTime.setMonth
     */
    override fun setMonth(month: Int) {
        if (month < 1) {
            this.month = 1
        } else if (month > 12) {
            this.month = 12
        } else {
            this.month = month
        }

        this.hasDate = true
    }


    /**
     * @see XMPDateTime.getDay
     */
    override fun getDay(): Int {
        return day
    }


    /**
     * @see XMPDateTime.setDay
     */
    override fun setDay(day: Int) {
        if (day < 1) {
            this.day = 1
        } else if (day > 31) {
            this.day = 31
        } else {
            this.day = day
        }

        this.hasDate = true
    }


    /**
     * @see XMPDateTime.getHour
     */
    override fun getHour(): Int {
        return hour
    }


    /**
     * @see XMPDateTime.setHour
     */
    override fun setHour(hour: Int) {
        this.hour = Math.min(Math.abs(hour), 23)
        this.hasTime = true
    }


    /**
     * @see XMPDateTime.getMinute
     */
    override fun getMinute(): Int {
        return minute
    }


    /**
     * @see XMPDateTime.setMinute
     */
    override fun setMinute(minute: Int) {
        this.minute = Math.min(Math.abs(minute), 59)
        this.hasTime = true
    }


    /**
     * @see XMPDateTime.getSecond
     */
    override fun getSecond(): Int {
        return second
    }


    /**
     * @see XMPDateTime.setSecond
     */
    override fun setSecond(second: Int) {
        this.second = Math.min(Math.abs(second), 59)
        this.hasTime = true
    }


    /**
     * @see XMPDateTime.getNanoSecond
     */
    override fun getNanoSecond(): Int {
        return nanoSeconds
    }


    /**
     * @see XMPDateTime.setNanoSecond
     */
    override fun setNanoSecond(nanoSecond: Int) {
        this.nanoSeconds = nanoSecond
        this.hasTime = true
    }


    /**
     * @see Comparable.compareTo
     */
    override operator fun compareTo(dt: Any): Int {
        var d = calendar.timeInMillis - (dt as XMPDateTime).calendar.timeInMillis
        if (d != 0) {
            return Math.signum(d.toFloat()).toInt()
        } else {
            // if millis are equal, compare nanoseconds
            d = (nanoSeconds - dt.nanoSecond).toLong()
            return Math.signum(d.toFloat()).toInt()
        }
    }


    /**
     * @see XMPDateTime.getTimeZone
     */
    override fun getTimeZone(): TimeZone {
        return timeZone
    }


    /**
     * @see XMPDateTime.setTimeZone
     */
    override fun setTimeZone(timeZone: TimeZone) {
        this.timeZone = timeZone
        this.hasTime = true
        this.hasTimeZone = true
    }


    /**
     * @see XMPDateTime.hasDate
     */
    override fun hasDate(): Boolean {
        return this.hasDate
    }


    /**
     * @see XMPDateTime.hasTime
     */
    override fun hasTime(): Boolean {
        return this.hasTime
    }


    /**
     * @see XMPDateTime.hasTimeZone
     */
    override fun hasTimeZone(): Boolean {
        return this.hasTimeZone
    }


    /**
     * @see XMPDateTime.getCalendar
     */
    override fun getCalendar(): Calendar {
        val calendar = Calendar.getInstance(Locale.US) as GregorianCalendar
        calendar.gregorianChange = Date(java.lang.Long.MIN_VALUE)
        if (hasTimeZone) {
            calendar.timeZone = timeZone
        }
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        calendar.set(Calendar.MILLISECOND, nanoSeconds / 1000000)

        return calendar
    }


    /**
     * @see XMPDateTime.getISO8601String
     */
    override fun getISO8601String(): String {
        return ISO8601Converter.render(this)
    }


    /**
     * @return Returns the ISO string representation.
     */
    override fun toString(): String {
        return isO8601String
    }
}