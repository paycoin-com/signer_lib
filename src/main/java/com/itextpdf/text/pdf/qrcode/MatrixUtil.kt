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
 * @author satorux@google.com (Satoru Takabayashi) - creator
 * *
 * @author dswitkin@google.com (Daniel Switkin) - ported from C++
 * *
 * @since 5.0.2
 */
object MatrixUtil {

    private val POSITION_DETECTION_PATTERN = arrayOf(intArrayOf(1, 1, 1, 1, 1, 1, 1), intArrayOf(1, 0, 0, 0, 0, 0, 1), intArrayOf(1, 0, 1, 1, 1, 0, 1), intArrayOf(1, 0, 1, 1, 1, 0, 1), intArrayOf(1, 0, 1, 1, 1, 0, 1), intArrayOf(1, 0, 0, 0, 0, 0, 1), intArrayOf(1, 1, 1, 1, 1, 1, 1))

    private val HORIZONTAL_SEPARATION_PATTERN = arrayOf(intArrayOf(0, 0, 0, 0, 0, 0, 0, 0))

    private val VERTICAL_SEPARATION_PATTERN = arrayOf(intArrayOf(0), intArrayOf(0), intArrayOf(0), intArrayOf(0), intArrayOf(0), intArrayOf(0), intArrayOf(0))

    private val POSITION_ADJUSTMENT_PATTERN = arrayOf(intArrayOf(1, 1, 1, 1, 1), intArrayOf(1, 0, 0, 0, 1), intArrayOf(1, 0, 1, 0, 1), intArrayOf(1, 0, 0, 0, 1), intArrayOf(1, 1, 1, 1, 1))

    // From Appendix E. Table 1, JIS0510X:2004 (p 71). The table was double-checked by komatsu.
    private val POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE = arrayOf(intArrayOf(-1, -1, -1, -1, -1, -1, -1), // Version 1
            intArrayOf(6, 18, -1, -1, -1, -1, -1), // Version 2
            intArrayOf(6, 22, -1, -1, -1, -1, -1), // Version 3
            intArrayOf(6, 26, -1, -1, -1, -1, -1), // Version 4
            intArrayOf(6, 30, -1, -1, -1, -1, -1), // Version 5
            intArrayOf(6, 34, -1, -1, -1, -1, -1), // Version 6
            intArrayOf(6, 22, 38, -1, -1, -1, -1), // Version 7
            intArrayOf(6, 24, 42, -1, -1, -1, -1), // Version 8
            intArrayOf(6, 26, 46, -1, -1, -1, -1), // Version 9
            intArrayOf(6, 28, 50, -1, -1, -1, -1), // Version 10
            intArrayOf(6, 30, 54, -1, -1, -1, -1), // Version 11
            intArrayOf(6, 32, 58, -1, -1, -1, -1), // Version 12
            intArrayOf(6, 34, 62, -1, -1, -1, -1), // Version 13
            intArrayOf(6, 26, 46, 66, -1, -1, -1), // Version 14
            intArrayOf(6, 26, 48, 70, -1, -1, -1), // Version 15
            intArrayOf(6, 26, 50, 74, -1, -1, -1), // Version 16
            intArrayOf(6, 30, 54, 78, -1, -1, -1), // Version 17
            intArrayOf(6, 30, 56, 82, -1, -1, -1), // Version 18
            intArrayOf(6, 30, 58, 86, -1, -1, -1), // Version 19
            intArrayOf(6, 34, 62, 90, -1, -1, -1), // Version 20
            intArrayOf(6, 28, 50, 72, 94, -1, -1), // Version 21
            intArrayOf(6, 26, 50, 74, 98, -1, -1), // Version 22
            intArrayOf(6, 30, 54, 78, 102, -1, -1), // Version 23
            intArrayOf(6, 28, 54, 80, 106, -1, -1), // Version 24
            intArrayOf(6, 32, 58, 84, 110, -1, -1), // Version 25
            intArrayOf(6, 30, 58, 86, 114, -1, -1), // Version 26
            intArrayOf(6, 34, 62, 90, 118, -1, -1), // Version 27
            intArrayOf(6, 26, 50, 74, 98, 122, -1), // Version 28
            intArrayOf(6, 30, 54, 78, 102, 126, -1), // Version 29
            intArrayOf(6, 26, 52, 78, 104, 130, -1), // Version 30
            intArrayOf(6, 30, 56, 82, 108, 134, -1), // Version 31
            intArrayOf(6, 34, 60, 86, 112, 138, -1), // Version 32
            intArrayOf(6, 30, 58, 86, 114, 142, -1), // Version 33
            intArrayOf(6, 34, 62, 90, 118, 146, -1), // Version 34
            intArrayOf(6, 30, 54, 78, 102, 126, 150), // Version 35
            intArrayOf(6, 24, 50, 76, 102, 128, 154), // Version 36
            intArrayOf(6, 28, 54, 80, 106, 132, 158), // Version 37
            intArrayOf(6, 32, 58, 84, 110, 136, 162), // Version 38
            intArrayOf(6, 26, 54, 82, 110, 138, 166), // Version 39
            intArrayOf(6, 30, 58, 86, 114, 142, 170))// Version 40

    // Type info cells at the left top corner.
    private val TYPE_INFO_COORDINATES = arrayOf(intArrayOf(8, 0), intArrayOf(8, 1), intArrayOf(8, 2), intArrayOf(8, 3), intArrayOf(8, 4), intArrayOf(8, 5), intArrayOf(8, 7), intArrayOf(8, 8), intArrayOf(7, 8), intArrayOf(5, 8), intArrayOf(4, 8), intArrayOf(3, 8), intArrayOf(2, 8), intArrayOf(1, 8), intArrayOf(0, 8))

    // From Appendix D in JISX0510:2004 (p. 67)
    private val VERSION_INFO_POLY = 0x1f25  // 1 1111 0010 0101

    // From Appendix C in JISX0510:2004 (p.65).
    private val TYPE_INFO_POLY = 0x537
    private val TYPE_INFO_MASK_PATTERN = 0x5412

    // Set all cells to -1.  -1 means that the cell is empty (not set yet).
    //
    // JAVAPORT: We shouldn't need to do this at all. The code should be rewritten to begin encoding
    // with the ByteMatrix initialized all to zero.
    fun clearMatrix(matrix: ByteMatrix) {
        matrix.clear((-1).toByte())
    }

    // Build 2D matrix of QR Code from "dataBits" with "ecLevel", "version" and "getMaskPattern". On
    // success, store the result in "matrix" and return true.
    @Throws(WriterException::class)
    fun buildMatrix(dataBits: BitVector, ecLevel: ErrorCorrectionLevel, version: Int,
                    maskPattern: Int, matrix: ByteMatrix) {
        clearMatrix(matrix)
        embedBasicPatterns(version, matrix)
        // Type information appear with any version.
        embedTypeInfo(ecLevel, maskPattern, matrix)
        // Version info appear if version >= 7.
        maybeEmbedVersionInfo(version, matrix)
        // Data should be embedded at end.
        embedDataBits(dataBits, maskPattern, matrix)
    }

    // Embed basic patterns. On success, modify the matrix and return true.
    // The basic patterns are:
    // - Position detection patterns
    // - Timing patterns
    // - Dark dot at the left bottom corner
    // - Position adjustment patterns, if need be
    @Throws(WriterException::class)
    fun embedBasicPatterns(version: Int, matrix: ByteMatrix) {
        // Let's get started with embedding big squares at corners.
        embedPositionDetectionPatternsAndSeparators(matrix)
        // Then, embed the dark dot at the left bottom corner.
        embedDarkDotAtLeftBottomCorner(matrix)

        // Position adjustment patterns appear if version >= 2.
        maybeEmbedPositionAdjustmentPatterns(version, matrix)
        // Timing patterns should be embedded after position adj. patterns.
        embedTimingPatterns(matrix)
    }

    // Embed type information. On success, modify the matrix.
    @Throws(WriterException::class)
    fun embedTypeInfo(ecLevel: ErrorCorrectionLevel, maskPattern: Int, matrix: ByteMatrix) {
        val typeInfoBits = BitVector()
        makeTypeInfoBits(ecLevel, maskPattern, typeInfoBits)

        for (i in 0..typeInfoBits.size() - 1) {
            // Place bits in LSB to MSB order.  LSB (least significant bit) is the last value in
            // "typeInfoBits".
            val bit = typeInfoBits.at(typeInfoBits.size() - 1 - i)

            // Type info bits at the left top corner. See 8.9 of JISX0510:2004 (p.46).
            val x1 = TYPE_INFO_COORDINATES[i][0]
            val y1 = TYPE_INFO_COORDINATES[i][1]
            matrix.set(x1, y1, bit)

            if (i < 8) {
                // Right top corner.
                val x2 = matrix.width - i - 1
                val y2 = 8
                matrix.set(x2, y2, bit)
            } else {
                // Left bottom corner.
                val x2 = 8
                val y2 = matrix.height - 7 + (i - 8)
                matrix.set(x2, y2, bit)
            }
        }
    }

    // Embed version information if need be. On success, modify the matrix and return true.
    // See 8.10 of JISX0510:2004 (p.47) for how to embed version information.
    @Throws(WriterException::class)
    fun maybeEmbedVersionInfo(version: Int, matrix: ByteMatrix) {
        if (version < 7) {
            // Version info is necessary if version >= 7.
            return   // Don't need version info.
        }
        val versionInfoBits = BitVector()
        makeVersionInfoBits(version, versionInfoBits)

        var bitIndex = 6 * 3 - 1  // It will decrease from 17 to 0.
        for (i in 0..5) {
            for (j in 0..2) {
                // Place bits in LSB (least significant bit) to MSB order.
                val bit = versionInfoBits.at(bitIndex)
                bitIndex--
                // Left bottom corner.
                matrix.set(i, matrix.height - 11 + j, bit)
                // Right bottom corner.
                matrix.set(matrix.height - 11 + j, i, bit)
            }
        }
    }

    // Embed "dataBits" using "getMaskPattern". On success, modify the matrix and return true.
    // For debugging purposes, it skips masking process if "getMaskPattern" is -1.
    // See 8.7 of JISX0510:2004 (p.38) for how to embed data bits.
    @Throws(WriterException::class)
    fun embedDataBits(dataBits: BitVector, maskPattern: Int, matrix: ByteMatrix) {
        var bitIndex = 0
        var direction = -1
        // Start from the right bottom cell.
        var x = matrix.width - 1
        var y = matrix.height - 1
        while (x > 0) {
            // Skip the vertical timing pattern.
            if (x == 6) {
                x -= 1
            }
            while (y >= 0 && y < matrix.height) {
                for (i in 0..1) {
                    val xx = x - i
                    // Skip the cell if it's not empty.
                    if (!isEmpty(matrix.get(xx, y).toInt())) {
                        continue
                    }
                    var bit: Int
                    if (bitIndex < dataBits.size()) {
                        bit = dataBits.at(bitIndex)
                        ++bitIndex
                    } else {
                        // Padding bit. If there is no bit left, we'll fill the left cells with 0, as described
                        // in 8.4.9 of JISX0510:2004 (p. 24).
                        bit = 0
                    }

                    // Skip masking if mask_pattern is -1.
                    if (maskPattern != -1) {
                        if (MaskUtil.getDataMaskBit(maskPattern, xx, y)) {
                            bit = bit xor 0x1
                        }
                    }
                    matrix.set(xx, y, bit)
                }
                y += direction
            }
            direction = -direction  // Reverse the direction.
            y += direction
            x -= 2  // Move to the left.
        }
        // All bits should be consumed.
        if (bitIndex != dataBits.size()) {
            throw WriterException("Not all bits consumed: " + bitIndex + '/' + dataBits.size())
        }
    }

    // Return the position of the most significant bit set (to one) in the "value". The most
    // significant bit is position 32. If there is no bit set, return 0. Examples:
    // - findMSBSet(0) => 0
    // - findMSBSet(1) => 1
    // - findMSBSet(255) => 8
    fun findMSBSet(value: Int): Int {
        var value = value
        var numDigits = 0
        while (value != 0) {
            value = value ushr 1
            ++numDigits
        }
        return numDigits
    }

    // Calculate BCH (Bose-Chaudhuri-Hocquenghem) code for "value" using polynomial "poly". The BCH
    // code is used for encoding type information and version information.
    // Example: Calculation of version information of 7.
    // f(x) is created from 7.
    //   - 7 = 000111 in 6 bits
    //   - f(x) = x^2 + x^2 + x^1
    // g(x) is given by the standard (p. 67)
    //   - g(x) = x^12 + x^11 + x^10 + x^9 + x^8 + x^5 + x^2 + 1
    // Multiply f(x) by x^(18 - 6)
    //   - f'(x) = f(x) * x^(18 - 6)
    //   - f'(x) = x^14 + x^13 + x^12
    // Calculate the remainder of f'(x) / g(x)
    //         x^2
    //         __________________________________________________
    //   g(x) )x^14 + x^13 + x^12
    //         x^14 + x^13 + x^12 + x^11 + x^10 + x^7 + x^4 + x^2
    //         --------------------------------------------------
    //                              x^11 + x^10 + x^7 + x^4 + x^2
    //
    // The remainder is x^11 + x^10 + x^7 + x^4 + x^2
    // Encode it in binary: 110010010100
    // The return value is 0xc94 (1100 1001 0100)
    //
    // Since all coefficients in the polynomials are 1 or 0, we can do the calculation by bit
    // operations. We don't care if cofficients are positive or negative.
    fun calculateBCHCode(value: Int, poly: Int): Int {
        var value = value
        // If poly is "1 1111 0010 0101" (version info poly), msbSetInPoly is 13. We'll subtract 1
        // from 13 to make it 12.
        val msbSetInPoly = findMSBSet(poly)
        value = value shl msbSetInPoly - 1
        // Do the division business using exclusive-or operations.
        while (findMSBSet(value) >= msbSetInPoly) {
            value = value xor (poly shl findMSBSet(value) - msbSetInPoly)
        }
        // Now the "value" is the remainder (i.e. the BCH code)
        return value
    }

    // Make bit vector of type information. On success, store the result in "bits" and return true.
    // Encode error correction level and mask pattern. See 8.9 of
    // JISX0510:2004 (p.45) for details.
    @Throws(WriterException::class)
    fun makeTypeInfoBits(ecLevel: ErrorCorrectionLevel, maskPattern: Int, bits: BitVector) {
        if (!QRCode.isValidMaskPattern(maskPattern)) {
            throw WriterException("Invalid mask pattern")
        }
        val typeInfo = ecLevel.bits shl 3 or maskPattern
        bits.appendBits(typeInfo, 5)

        val bchCode = calculateBCHCode(typeInfo, TYPE_INFO_POLY)
        bits.appendBits(bchCode, 10)

        val maskBits = BitVector()
        maskBits.appendBits(TYPE_INFO_MASK_PATTERN, 15)
        bits.xor(maskBits)

        if (bits.size() != 15) {
            // Just in case.
            throw WriterException("should not happen but we got: " + bits.size())
        }
    }

    // Make bit vector of version information. On success, store the result in "bits" and return true.
    // See 8.10 of JISX0510:2004 (p.45) for details.
    @Throws(WriterException::class)
    fun makeVersionInfoBits(version: Int, bits: BitVector) {
        bits.appendBits(version, 6)
        val bchCode = calculateBCHCode(version, VERSION_INFO_POLY)
        bits.appendBits(bchCode, 12)

        if (bits.size() != 18) {
            // Just in case.
            throw WriterException("should not happen but we got: " + bits.size())
        }
    }

    // Check if "value" is empty.
    private fun isEmpty(value: Int): Boolean {
        return value == -1
    }

    // Check if "value" is valid.
    private fun isValidValue(value: Int): Boolean {
        return value == -1 || // Empty.

                value == 0 || // Light (white).

                value == 1  // Dark (black).
    }

    @Throws(WriterException::class)
    private fun embedTimingPatterns(matrix: ByteMatrix) {
        // -8 is for skipping position detection patterns (size 7), and two horizontal/vertical
        // separation patterns (size 1). Thus, 8 = 7 + 1.
        for (i in 8..matrix.width - 8 - 1) {
            val bit = (i + 1) % 2
            // Horizontal line.
            if (!isValidValue(matrix.get(i, 6).toInt())) {
                throw WriterException()
            }
            if (isEmpty(matrix.get(i, 6).toInt())) {
                matrix.set(i, 6, bit)
            }
            // Vertical line.
            if (!isValidValue(matrix.get(6, i).toInt())) {
                throw WriterException()
            }
            if (isEmpty(matrix.get(6, i).toInt())) {
                matrix.set(6, i, bit)
            }
        }
    }

    // Embed the lonely dark dot at left bottom corner. JISX0510:2004 (p.46)
    @Throws(WriterException::class)
    private fun embedDarkDotAtLeftBottomCorner(matrix: ByteMatrix) {
        if (matrix.get(8, matrix.height - 8).toInt() == 0) {
            throw WriterException()
        }
        matrix.set(8, matrix.height - 8, 1)
    }

    @Throws(WriterException::class)
    private fun embedHorizontalSeparationPattern(xStart: Int, yStart: Int,
                                                 matrix: ByteMatrix) {
        // We know the width and height.
        if (HORIZONTAL_SEPARATION_PATTERN[0].size != 8 || HORIZONTAL_SEPARATION_PATTERN.size != 1) {
            throw WriterException("Bad horizontal separation pattern")
        }
        for (x in 0..7) {
            if (!isEmpty(matrix.get(xStart + x, yStart).toInt())) {
                throw WriterException()
            }
            matrix.set(xStart + x, yStart, HORIZONTAL_SEPARATION_PATTERN[0][x])
        }
    }

    @Throws(WriterException::class)
    private fun embedVerticalSeparationPattern(xStart: Int, yStart: Int,
                                               matrix: ByteMatrix) {
        // We know the width and height.
        if (VERTICAL_SEPARATION_PATTERN[0].size != 1 || VERTICAL_SEPARATION_PATTERN.size != 7) {
            throw WriterException("Bad vertical separation pattern")
        }
        for (y in 0..6) {
            if (!isEmpty(matrix.get(xStart, yStart + y).toInt())) {
                throw WriterException()
            }
            matrix.set(xStart, yStart + y, VERTICAL_SEPARATION_PATTERN[y][0])
        }
    }

    // Note that we cannot unify the function with embedPositionDetectionPattern() despite they are
    // almost identical, since we cannot write a function that takes 2D arrays in different sizes in
    // C/C++. We should live with the fact.
    @Throws(WriterException::class)
    private fun embedPositionAdjustmentPattern(xStart: Int, yStart: Int,
                                               matrix: ByteMatrix) {
        // We know the width and height.
        if (POSITION_ADJUSTMENT_PATTERN[0].size != 5 || POSITION_ADJUSTMENT_PATTERN.size != 5) {
            throw WriterException("Bad position adjustment")
        }
        for (y in 0..4) {
            for (x in 0..4) {
                if (!isEmpty(matrix.get(xStart + x, yStart + y).toInt())) {
                    throw WriterException()
                }
                matrix.set(xStart + x, yStart + y, POSITION_ADJUSTMENT_PATTERN[y][x])
            }
        }
    }

    @Throws(WriterException::class)
    private fun embedPositionDetectionPattern(xStart: Int, yStart: Int,
                                              matrix: ByteMatrix) {
        // We know the width and height.
        if (POSITION_DETECTION_PATTERN[0].size != 7 || POSITION_DETECTION_PATTERN.size != 7) {
            throw WriterException("Bad position detection pattern")
        }
        for (y in 0..6) {
            for (x in 0..6) {
                if (!isEmpty(matrix.get(xStart + x, yStart + y).toInt())) {
                    throw WriterException()
                }
                matrix.set(xStart + x, yStart + y, POSITION_DETECTION_PATTERN[y][x])
            }
        }
    }

    // Embed position detection patterns and surrounding vertical/horizontal separators.
    @Throws(WriterException::class)
    private fun embedPositionDetectionPatternsAndSeparators(matrix: ByteMatrix) {
        // Embed three big squares at corners.
        val pdpWidth = POSITION_DETECTION_PATTERN[0].size
        // Left top corner.
        embedPositionDetectionPattern(0, 0, matrix)
        // Right top corner.
        embedPositionDetectionPattern(matrix.width - pdpWidth, 0, matrix)
        // Left bottom corner.
        embedPositionDetectionPattern(0, matrix.width - pdpWidth, matrix)

        // Embed horizontal separation patterns around the squares.
        val hspWidth = HORIZONTAL_SEPARATION_PATTERN[0].size
        // Left top corner.
        embedHorizontalSeparationPattern(0, hspWidth - 1, matrix)
        // Right top corner.
        embedHorizontalSeparationPattern(matrix.width - hspWidth,
                hspWidth - 1, matrix)
        // Left bottom corner.
        embedHorizontalSeparationPattern(0, matrix.width - hspWidth, matrix)

        // Embed vertical separation patterns around the squares.
        val vspSize = VERTICAL_SEPARATION_PATTERN.size
        // Left top corner.
        embedVerticalSeparationPattern(vspSize, 0, matrix)
        // Right top corner.
        embedVerticalSeparationPattern(matrix.height - vspSize - 1, 0, matrix)
        // Left bottom corner.
        embedVerticalSeparationPattern(vspSize, matrix.height - vspSize,
                matrix)
    }

    // Embed position adjustment patterns if need be.
    @Throws(WriterException::class)
    private fun maybeEmbedPositionAdjustmentPatterns(version: Int, matrix: ByteMatrix) {
        if (version < 2) {
            // The patterns appear if version >= 2
            return
        }
        val index = version - 1
        val coordinates = POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index]
        val numCoordinates = POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index].size
        for (i in 0..numCoordinates - 1) {
            for (j in 0..numCoordinates - 1) {
                val y = coordinates[i]
                val x = coordinates[j]
                if (x == -1 || y == -1) {
                    continue
                }
                // If the cell is unset, we embed the position adjustment pattern here.
                if (isEmpty(matrix.get(x, y).toInt())) {
                    // -2 is necessary since the x/y coordinates point to the center of the pattern, not the
                    // left top corner.
                    embedPositionAdjustmentPattern(x - 2, y - 2, matrix)
                }
            }
        }
    }

}// do nothing
