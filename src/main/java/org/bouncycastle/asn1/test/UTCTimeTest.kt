package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.DERUTCTime
import org.bouncycastle.util.test.SimpleTest

import java.text.SimpleDateFormat
import java.util.SimpleTimeZone

/**
 * X.690 test example
 */
class UTCTimeTest : SimpleTest() {
    internal var input = arrayOf("020122122220Z", "020122122220-1000", "020122122220+1000", "020122122220+00", "0201221222Z", "0201221222-1000", "0201221222+1000", "0201221222+00", "550122122220Z", "5501221222Z")

    internal var output = arrayOf("20020122122220GMT+00:00", "20020122122220GMT-10:00", "20020122122220GMT+10:00", "20020122122220GMT+00:00", "20020122122200GMT+00:00", "20020122122200GMT-10:00", "20020122122200GMT+10:00", "20020122122200GMT+00:00", "19550122122220GMT+00:00", "19550122122200GMT+00:00")

    internal var zOutput1 = arrayOf("20020122122220Z", "20020122222220Z", "20020122022220Z", "20020122122220Z", "20020122122200Z", "20020122222200Z", "20020122022200Z", "20020122122200Z", "19550122122220Z", "19550122122200Z")

    internal var zOutput2 = arrayOf("20020122122220Z", "20020122222220Z", "20020122022220Z", "20020122122220Z", "20020122122200Z", "20020122222200Z", "20020122022200Z", "20020122122200Z", "19550122122220Z", "19550122122200Z")

    override fun getName(): String {
        return "UTCTime"
    }

    @Throws(Exception::class)
    override fun performTest() {
        val yyyyF = SimpleDateFormat("yyyyMMddHHmmss'Z'")
        val yyF = SimpleDateFormat("yyyyMMddHHmmss'Z'")

        yyyyF.timeZone = SimpleTimeZone(0, "Z")
        yyF.timeZone = SimpleTimeZone(0, "Z")

        for (i in input.indices) {
            val t = DERUTCTime(input[i])

            if (t.adjustedTime != output[i]) {
                fail("failed conversion test " + i)
            }

            if (yyyyF.format(t.adjustedDate) != zOutput1[i]) {
                fail("failed date conversion test " + i)
            }

            if (yyF.format(t.date) != zOutput2[i]) {
                fail("failed date shortened conversion test " + i)
            }
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(UTCTimeTest())
        }
    }
}
