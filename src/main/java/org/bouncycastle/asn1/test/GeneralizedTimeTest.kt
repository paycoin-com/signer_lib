package org.bouncycastle.asn1.test

import java.text.SimpleDateFormat
import java.util.Date
import java.util.SimpleTimeZone
import java.util.TimeZone

import org.bouncycastle.asn1.DERGeneralizedTime
import org.bouncycastle.util.test.SimpleTest

/**
 * X.690 test example
 */
class GeneralizedTimeTest : SimpleTest() {
    internal var input = arrayOf("20020122122220", "20020122122220Z", "20020122122220-1000", "20020122122220+00", "20020122122220.1", "20020122122220.1Z", "20020122122220.1-1000", "20020122122220.1+00", "20020122122220.01", "20020122122220.01Z", "20020122122220.01-1000", "20020122122220.01+00", "20020122122220.001", "20020122122220.001Z", "20020122122220.001-1000", "20020122122220.001+00", "20020122122220.0001", "20020122122220.0001Z", "20020122122220.0001-1000", "20020122122220.0001+00", "20020122122220.0001+1000")

    internal var output = arrayOf("20020122122220", "20020122122220GMT+00:00", "20020122122220GMT-10:00", "20020122122220GMT+00:00", "20020122122220.1", "20020122122220.1GMT+00:00", "20020122122220.1GMT-10:00", "20020122122220.1GMT+00:00", "20020122122220.01", "20020122122220.01GMT+00:00", "20020122122220.01GMT-10:00", "20020122122220.01GMT+00:00", "20020122122220.001", "20020122122220.001GMT+00:00", "20020122122220.001GMT-10:00", "20020122122220.001GMT+00:00", "20020122122220.0001", "20020122122220.0001GMT+00:00", "20020122122220.0001GMT-10:00", "20020122122220.0001GMT+00:00", "20020122122220.0001GMT+10:00")

    internal var zOutput = arrayOf("20020122122220Z", "20020122122220Z", "20020122222220Z", "20020122122220Z", "20020122122220Z", "20020122122220Z", "20020122222220Z", "20020122122220Z", "20020122122220Z", "20020122122220Z", "20020122222220Z", "20020122122220Z", "20020122122220Z", "20020122122220Z", "20020122222220Z", "20020122122220Z", "20020122122220Z", "20020122122220Z", "20020122222220Z", "20020122122220Z", "20020122022220Z")

    internal var mzOutput = arrayOf("20020122122220.000Z", "20020122122220.000Z", "20020122222220.000Z", "20020122122220.000Z", "20020122122220.100Z", "20020122122220.100Z", "20020122222220.100Z", "20020122122220.100Z", "20020122122220.010Z", "20020122122220.010Z", "20020122222220.010Z", "20020122122220.010Z", "20020122122220.001Z", "20020122122220.001Z", "20020122222220.001Z", "20020122122220.001Z", "20020122122220.000Z", "20020122122220.000Z", "20020122222220.000Z", "20020122122220.000Z", "20020122022220.000Z")

    override fun getName(): String {
        return "GeneralizedTime"
    }

    @Throws(Exception::class)
    override fun performTest() {
        var dateF = SimpleDateFormat("yyyyMMddHHmmss'Z'")

        dateF.timeZone = SimpleTimeZone(0, "Z")

        for (i in input.indices) {
            val t = DERGeneralizedTime(input[i])

            if (output[i].indexOf('G') > 0)
            // don't check local time the same way
            {
                if (t.time != output[i]) {
                    fail("failed conversion test")
                }
                if (dateF.format(t.date) != zOutput[i]) {
                    fail("failed date conversion test")
                }
            } else {
                val offset = calculateGMTOffset(t.date)
                if (t.time != output[i] + offset) {
                    fail("failed conversion test")
                }
            }
        }

        dateF = SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'")

        dateF.timeZone = SimpleTimeZone(0, "Z")

        for (i in input.indices) {
            val t = DERGeneralizedTime(input[i])

            if (dateF.format(t.date) != mzOutput[i]) {
                fail("failed long date conversion test")
            }
        }
    }

    private fun calculateGMTOffset(date: Date): String {
        var sign = "+"
        val timeZone = TimeZone.getDefault()
        var offset = timeZone.rawOffset
        if (offset < 0) {
            sign = "-"
            offset = -offset
        }
        var hours = offset / (60 * 60 * 1000)
        val minutes = (offset - hours * 60 * 60 * 1000) / (60 * 1000)

        if (timeZone.useDaylightTime() && timeZone.inDaylightTime(date)) {
            hours += if (sign == "+") 1 else -1
        }

        return "GMT" + sign + convert(hours) + ":" + convert(minutes)
    }

    private fun convert(time: Int): String {
        if (time < 10) {
            return "0" + time
        }

        return Integer.toString(time)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(GeneralizedTimeTest())
        }
    }
}
