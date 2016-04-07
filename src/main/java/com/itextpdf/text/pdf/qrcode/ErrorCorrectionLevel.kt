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
 * See ISO 18004:2006, 6.5.1. This enum encapsulates the four error correction levels
 * defined by the QR code standard.

 * @author Sean Owen
 * *
 * @since 5.0.2
 */
class ErrorCorrectionLevel private constructor(private val ordinal: Int, val bits: Int, val name: String) {

    fun ordinal(): Int {
        return ordinal
    }

    override fun toString(): String {
        return name
    }

    companion object {

        // No, we can't use an enum here. J2ME doesn't support it.

        /**
         * L = ~7% correction
         */
        val L = ErrorCorrectionLevel(0, 0x01, "L")
        /**
         * M = ~15% correction
         */
        val M = ErrorCorrectionLevel(1, 0x00, "M")
        /**
         * Q = ~25% correction
         */
        val Q = ErrorCorrectionLevel(2, 0x03, "Q")
        /**
         * H = ~30% correction
         */
        val H = ErrorCorrectionLevel(3, 0x02, "H")

        private val FOR_BITS = arrayOf(M, L, H, Q)

        /**
         * @param bits int containing the two bits encoding a QR Code's error correction level
         * *
         * @return [ErrorCorrectionLevel] representing the encoded error correction level
         */
        fun forBits(bits: Int): ErrorCorrectionLevel {
            if (bits < 0 || bits >= FOR_BITS.size) {
                throw IllegalArgumentException()
            }
            return FOR_BITS[bits]
        }
    }


}
