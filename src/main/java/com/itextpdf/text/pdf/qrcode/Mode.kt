/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itextpdf.text.pdf.qrcode

/**
 *
 * See ISO 18004:2006, 6.4.1, Tables 2 and 3. This enum encapsulates the various modes in which
 * data can be encoded to bits in the QR code standard.

 * @author Sean Owen
 * *
 * @since 5.0.2
 */
class Mode private constructor(private val characterCountBitsForVersions: IntArray?, val bits: Int, val name: String) {

    /**
     * @param version version in question
     * *
     * @return number of bits used, in this QR Code symbol [Version], to encode the
     * *         count of characters that will follow encoded in this [Mode]
     */
    fun getCharacterCountBits(version: Version): Int {
        if (characterCountBitsForVersions == null) {
            throw IllegalArgumentException("Character count doesn't apply to this mode")
        }
        val number = version.versionNumber
        val offset: Int
        if (number <= 9) {
            offset = 0
        } else if (number <= 26) {
            offset = 1
        } else {
            offset = 2
        }
        return characterCountBitsForVersions[offset]
    }

    override fun toString(): String {
        return name
    }

    companion object {

        // No, we can't use an enum here. J2ME doesn't support it.

        val TERMINATOR = Mode(intArrayOf(0, 0, 0), 0x00, "TERMINATOR") // Not really a mode...
        val NUMERIC = Mode(intArrayOf(10, 12, 14), 0x01, "NUMERIC")
        val ALPHANUMERIC = Mode(intArrayOf(9, 11, 13), 0x02, "ALPHANUMERIC")
        val STRUCTURED_APPEND = Mode(intArrayOf(0, 0, 0), 0x03, "STRUCTURED_APPEND") // Not supported
        val BYTE = Mode(intArrayOf(8, 16, 16), 0x04, "BYTE")
        val ECI = Mode(null, 0x07, "ECI") // character counts don't apply
        val KANJI = Mode(intArrayOf(8, 10, 12), 0x08, "KANJI")
        val FNC1_FIRST_POSITION = Mode(null, 0x05, "FNC1_FIRST_POSITION")
        val FNC1_SECOND_POSITION = Mode(null, 0x09, "FNC1_SECOND_POSITION")

        /**
         * @param bits four bits encoding a QR Code data mode
         * *
         * @return [Mode] encoded by these bits
         * *
         * @throws IllegalArgumentException if bits do not correspond to a known mode
         */
        fun forBits(bits: Int): Mode {
            when (bits) {
                0x0 -> return TERMINATOR
                0x1 -> return NUMERIC
                0x2 -> return ALPHANUMERIC
                0x3 -> return STRUCTURED_APPEND
                0x4 -> return BYTE
                0x5 -> return FNC1_FIRST_POSITION
                0x7 -> return ECI
                0x8 -> return KANJI
                0x9 -> return FNC1_SECOND_POSITION
                else -> throw IllegalArgumentException()
            }
        }
    }

}
