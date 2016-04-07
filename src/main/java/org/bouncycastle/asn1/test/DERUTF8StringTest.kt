package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.util.Arrays
import org.bouncycastle.util.Strings
import org.bouncycastle.util.test.SimpleTestResult
import org.bouncycastle.util.test.Test
import org.bouncycastle.util.test.TestResult

class DERUTF8StringTest : Test {

    override fun perform(): TestResult {
        try {
            for (i in glyphs_utf16.indices) {
                val s = String(glyphs_utf16[i])
                val b1 = DERUTF8String(s).encoded
                val temp = ByteArray(b1.size - 2)
                System.arraycopy(b1, 2, temp, 0, b1.size - 2)
                val b2 = DERUTF8String(Strings.fromUTF8ByteArray(DEROctetString(temp).octets)).encoded
                if (!Arrays.areEqual(b1, b2)) {
                    return SimpleTestResult(false, name + ": failed UTF-8 encoding and decoding")
                }
                if (!Arrays.areEqual(temp, glyphs_utf8[i])) {
                    return SimpleTestResult(false, name + ": failed UTF-8 encoding and decoding")
                }
            }
        } catch (e: Exception) {
            return SimpleTestResult(false, name + ": failed with Exception " + e.message)
        }

        return SimpleTestResult(true, name + ": Okay")
    }

    override fun getName(): String {
        return "DERUTF8String"
    }

    companion object {

        /**
         * Unicode code point U+10400 coded as surrogate in two native Java UTF-16
         * code units
         */
        private val glyph1_utf16 = charArrayOf(0xd801.toChar(), 0xdc00.toChar())

        /**
         * U+10400 coded in UTF-8
         */
        private val glyph1_utf8 = byteArrayOf(0xF0.toByte(), 0x90.toByte(), 0x90.toByte(), 0x80.toByte())

        /**
         * Unicode code point U+6771 in native Java UTF-16
         */
        private val glyph2_utf16 = charArrayOf(0x6771.toChar())

        /**
         * U+6771 coded in UTF-8
         */
        private val glyph2_utf8 = byteArrayOf(0xE6.toByte(), 0x9D.toByte(), 0xB1.toByte())

        /**
         * Unicode code point U+00DF in native Java UTF-16
         */
        private val glyph3_utf16 = charArrayOf(0x00DF.toChar())

        /**
         * U+00DF coded in UTF-8
         */
        private val glyph3_utf8 = byteArrayOf(0xC3.toByte(), 0x9f.toByte())

        /**
         * Unicode code point U+0041 in native Java UTF-16
         */
        private val glyph4_utf16 = charArrayOf(0x0041.toChar())

        /**
         * U+0041 coded in UTF-8
         */
        private val glyph4_utf8 = byteArrayOf(0x41)

        private val glyphs_utf8 = arrayOf(glyph1_utf8, glyph2_utf8, glyph3_utf8, glyph4_utf8)

        private val glyphs_utf16 = arrayOf(glyph1_utf16, glyph2_utf16, glyph3_utf16, glyph4_utf16)

        @JvmStatic fun main(args: Array<String>) {
            val test = DERUTF8StringTest()
            val result = test.perform()

            println(result)
        }
    }
}
