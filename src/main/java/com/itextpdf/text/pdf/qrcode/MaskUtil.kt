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
object MaskUtil {

    // Apply mask penalty rule 1 and return the penalty. Find repetitive cells with the same color and
    // give penalty to them. Example: 00000 or 11111.
    fun applyMaskPenaltyRule1(matrix: ByteMatrix): Int {
        return applyMaskPenaltyRule1Internal(matrix, true) + applyMaskPenaltyRule1Internal(matrix, false)
    }

    // Apply mask penalty rule 2 and return the penalty. Find 2x2 blocks with the same color and give
    // penalty to them.
    fun applyMaskPenaltyRule2(matrix: ByteMatrix): Int {
        var penalty = 0
        val array = matrix.array
        val width = matrix.width
        val height = matrix.height
        for (y in 0..height - 1 - 1) {
            for (x in 0..width - 1 - 1) {
                val value = array[y][x].toInt()
                if (value == array[y][x + 1].toInt() && value == array[y + 1][x].toInt() && value == array[y + 1][x + 1].toInt()) {
                    penalty += 3
                }
            }
        }
        return penalty
    }

    // Apply mask penalty rule 3 and return the penalty. Find consecutive cells of 00001011101 or
    // 10111010000, and give penalty to them.  If we find patterns like 000010111010000, we give
    // penalties twice (i.e. 40 * 2).
    fun applyMaskPenaltyRule3(matrix: ByteMatrix): Int {
        var penalty = 0
        val array = matrix.array
        val width = matrix.width
        val height = matrix.height
        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                // Tried to simplify following conditions but failed.
                if (x + 6 < width &&
                        array[y][x].toInt() == 1 &&
                        array[y][x + 1].toInt() == 0 &&
                        array[y][x + 2].toInt() == 1 &&
                        array[y][x + 3].toInt() == 1 &&
                        array[y][x + 4].toInt() == 1 &&
                        array[y][x + 5].toInt() == 0 &&
                        array[y][x + 6].toInt() == 1 &&
                        (x + 10 < width &&
                                array[y][x + 7].toInt() == 0 &&
                                array[y][x + 8].toInt() == 0 &&
                                array[y][x + 9].toInt() == 0 &&
                                array[y][x + 10].toInt() == 0 || x - 4 >= 0 &&
                                array[y][x - 1].toInt() == 0 &&
                                array[y][x - 2].toInt() == 0 &&
                                array[y][x - 3].toInt() == 0 &&
                                array[y][x - 4].toInt() == 0)) {
                    penalty += 40
                }
                if (y + 6 < height &&
                        array[y][x].toInt() == 1 &&
                        array[y + 1][x].toInt() == 0 &&
                        array[y + 2][x].toInt() == 1 &&
                        array[y + 3][x].toInt() == 1 &&
                        array[y + 4][x].toInt() == 1 &&
                        array[y + 5][x].toInt() == 0 &&
                        array[y + 6][x].toInt() == 1 &&
                        (y + 10 < height &&
                                array[y + 7][x].toInt() == 0 &&
                                array[y + 8][x].toInt() == 0 &&
                                array[y + 9][x].toInt() == 0 &&
                                array[y + 10][x].toInt() == 0 || y - 4 >= 0 &&
                                array[y - 1][x].toInt() == 0 &&
                                array[y - 2][x].toInt() == 0 &&
                                array[y - 3][x].toInt() == 0 &&
                                array[y - 4][x].toInt() == 0)) {
                    penalty += 40
                }
            }
        }
        return penalty
    }

    // Apply mask penalty rule 4 and return the penalty. Calculate the ratio of dark cells and give
    // penalty if the ratio is far from 50%. It gives 10 penalty for 5% distance. Examples:
    // -   0% => 100
    // -  40% =>  20
    // -  45% =>  10
    // -  50% =>   0
    // -  55% =>  10
    // -  55% =>  20
    // - 100% => 100
    fun applyMaskPenaltyRule4(matrix: ByteMatrix): Int {
        var numDarkCells = 0
        val array = matrix.array
        val width = matrix.width
        val height = matrix.height
        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                if (array[y][x].toInt() == 1) {
                    numDarkCells += 1
                }
            }
        }
        val numTotalCells = matrix.height * matrix.width
        val darkRatio = numDarkCells.toDouble() / numTotalCells
        return Math.abs((darkRatio * 100 - 50).toInt()) / 5 * 10
    }

    // Return the mask bit for "getMaskPattern" at "x" and "y". See 8.8 of JISX0510:2004 for mask
    // pattern conditions.
    fun getDataMaskBit(maskPattern: Int, x: Int, y: Int): Boolean {
        if (!QRCode.isValidMaskPattern(maskPattern)) {
            throw IllegalArgumentException("Invalid mask pattern")
        }
        val intermediate: Int
        val temp: Int
        when (maskPattern) {
            0 -> intermediate = y + x and 0x1
            1 -> intermediate = y and 0x1
            2 -> intermediate = x % 3
            3 -> intermediate = (y + x) % 3
            4 -> intermediate = y.ushr(1) + x / 3 and 0x1
            5 -> {
                temp = y * x
                intermediate = (temp and 0x1) + temp % 3
            }
            6 -> {
                temp = y * x
                intermediate = (temp and 0x1) + temp % 3 and 0x1
            }
            7 -> {
                temp = y * x
                intermediate = temp % 3 + (y + x and 0x1) and 0x1
            }
            else -> throw IllegalArgumentException("Invalid mask pattern: " + maskPattern)
        }
        return intermediate == 0
    }

    // Helper function for applyMaskPenaltyRule1. We need this for doing this calculation in both
    // vertical and horizontal orders respectively.
    private fun applyMaskPenaltyRule1Internal(matrix: ByteMatrix, isHorizontal: Boolean): Int {
        var penalty = 0
        var numSameBitCells = 0
        var prevBit = -1
        // Horizontal mode:
        //   for (int i = 0; i < matrix.height(); ++i) {
        //     for (int j = 0; j < matrix.width(); ++j) {
        //       int bit = matrix.get(i, j);
        // Vertical mode:
        //   for (int i = 0; i < matrix.width(); ++i) {
        //     for (int j = 0; j < matrix.height(); ++j) {
        //       int bit = matrix.get(j, i);
        val iLimit = if (isHorizontal) matrix.height else matrix.width
        val jLimit = if (isHorizontal) matrix.width else matrix.height
        val array = matrix.array
        for (i in 0..iLimit - 1) {
            for (j in 0..jLimit - 1) {
                val bit = (if (isHorizontal) array[i][j] else array[j][i]).toInt()
                if (bit == prevBit) {
                    numSameBitCells += 1
                    // Found five repetitive cells with the same color (bit).
                    // We'll give penalty of 3.
                    if (numSameBitCells == 5) {
                        penalty += 3
                    } else if (numSameBitCells > 5) {
                        // After five repetitive cells, we'll add the penalty one
                        // by one.
                        penalty += 1
                    }
                } else {
                    numSameBitCells = 1  // Include the cell itself.
                    prevBit = bit
                }
            }
            numSameBitCells = 0  // Clear at each row/column.
        }
        return penalty
    }

}// do nothing
