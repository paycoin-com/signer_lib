/*
 * Copyright 2008 ZXing authors
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
 * This object renders a QR Code as a ByteMatrix 2D array of greyscale values.

 * @author dswitkin@google.com (Daniel Switkin)
 * *
 * @since 5.0.2
 */
class QRCodeWriter {

    @Throws(WriterException::class)
    @JvmOverloads fun encode(contents: String?, width: Int, height: Int,
                             hints: Map<EncodeHintType, Any>? = null): ByteMatrix {

        if (contents == null || contents.length == 0) {
            throw IllegalArgumentException("Found empty contents")
        }

        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Requested dimensions are too small: " + width + 'x' +
                    height)
        }

        var errorCorrectionLevel = ErrorCorrectionLevel.L
        if (hints != null) {
            val requestedECLevel = hints[EncodeHintType.ERROR_CORRECTION] as ErrorCorrectionLevel
            if (requestedECLevel != null) {
                errorCorrectionLevel = requestedECLevel
            }
        }

        val code = QRCode()
        Encoder.encode(contents, errorCorrectionLevel, hints, code)
        return renderResult(code, width, height)
    }

    companion object {

        private val QUIET_ZONE_SIZE = 4

        // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
        // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
        private fun renderResult(code: QRCode, width: Int, height: Int): ByteMatrix {
            val input = code.matrix
            val inputWidth = input.width
            val inputHeight = input.height
            val qrWidth = inputWidth + (QUIET_ZONE_SIZE shl 1)
            val qrHeight = inputHeight + (QUIET_ZONE_SIZE shl 1)
            val outputWidth = Math.max(width, qrWidth)
            val outputHeight = Math.max(height, qrHeight)

            val multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight)
            // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
            // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
            // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
            // handle all the padding from 100x100 (the actual QR) up to 200x160.
            val leftPadding = (outputWidth - inputWidth * multiple) / 2
            val topPadding = (outputHeight - inputHeight * multiple) / 2

            val output = ByteMatrix(outputWidth, outputHeight)
            val outputArray = output.array

            // We could be tricky and use the first row in each set of multiple as the temporary storage,
            // instead of allocating this separate array.
            val row = ByteArray(outputWidth)

            // 1. Write the white lines at the top
            for (y in 0..topPadding - 1) {
                setRowColor(outputArray[y], 255.toByte())
            }

            // 2. Expand the QR image to the multiple
            val inputArray = input.array
            for (y in 0..inputHeight - 1) {
                // a. Write the white pixels at the left of each row
                for (x in 0..leftPadding - 1) {
                    row[x] = 255.toByte()
                }

                // b. Write the contents of this row of the barcode
                var offset = leftPadding
                for (x in 0..inputWidth - 1) {
                    val value = if (inputArray[y][x].toInt() == 1) 0 else 255.toByte()
                    for (z in 0..multiple - 1) {
                        row[offset + z] = value
                    }
                    offset += multiple
                }

                // c. Write the white pixels at the right of each row
                offset = leftPadding + inputWidth * multiple
                for (x in offset..outputWidth - 1) {
                    row[x] = 255.toByte()
                }

                // d. Write the completed row multiple times
                offset = topPadding + y * multiple
                for (z in 0..multiple - 1) {
                    System.arraycopy(row, 0, outputArray[offset + z], 0, outputWidth)
                }
            }

            // 3. Write the white lines at the bottom
            val offset = topPadding + inputHeight * multiple
            for (y in offset..outputHeight - 1) {
                setRowColor(outputArray[y], 255.toByte())
            }

            return output
        }

        private fun setRowColor(row: ByteArray, value: Byte) {
            for (x in row.indices) {
                row[x] = value
            }
        }
    }

}
