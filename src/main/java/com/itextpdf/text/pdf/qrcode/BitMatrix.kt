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
 * Represents a 2D matrix of bits. In function arguments below, and throughout the common
 * module, x is the column position, and y is the row position. The ordering is always x, y.
 * The origin is at the top-left.

 *
 * Internally the bits are represented in a 1-D array of 32-bit ints. However, each row begins
 * with a new int. This is done intentionally so that we can copy out a row into a BitArray very
 * efficiently.

 *
 * The ordering of bits is row-major. Within each int, the least significant bits are used first,
 * meaning they represent lower x values. This is compatible with BitArray's implementation.

 * @author Sean Owen
 * *
 * @author dswitkin@google.com (Daniel Switkin)
 * *
 * @since 5.0.2
 */
class BitMatrix(// TODO: Just like BitArray, these need to be public so ProGuard can inline them.
        /**
         * @return The width of the matrix
         */
        val width: Int,
        /**
         * @return The height of the matrix
         */
        val height: Int) {
    val rowSize: Int
    val bits: IntArray

    // A helper to construct a square matrix.
    constructor(dimension: Int) : this(dimension, dimension) {
    }

    init {
        if (width < 1 || height < 1) {
            throw IllegalArgumentException("Both dimensions must be greater than 0")
        }
        var rowSize = width shr 5
        if (width and 0x1f != 0) {
            rowSize++
        }
        this.rowSize = rowSize
        bits = IntArray(rowSize * height)
    }

    /**
     *
     * Gets the requested bit, where true means black.

     * @param x The horizontal component (i.e. which column)
     * *
     * @param y The vertical component (i.e. which row)
     * *
     * @return value of given bit in matrix
     */
    operator fun get(x: Int, y: Int): Boolean {
        val offset = y * rowSize + (x shr 5)
        return bits[offset].ushr(x and 0x1f) and 1 != 0
    }

    /**
     *
     * Sets the given bit to true.

     * @param x The horizontal component (i.e. which column)
     * *
     * @param y The vertical component (i.e. which row)
     */
    operator fun set(x: Int, y: Int) {
        val offset = y * rowSize + (x shr 5)
        bits[offset] = bits[offset] or (1 shl (x and 0x1f))
    }

    /**
     *
     * Flips the given bit.

     * @param x The horizontal component (i.e. which column)
     * *
     * @param y The vertical component (i.e. which row)
     */
    fun flip(x: Int, y: Int) {
        val offset = y * rowSize + (x shr 5)
        bits[offset] = bits[offset] xor (1 shl (x and 0x1f))
    }

    /**
     * Clears all bits (sets to false).
     */
    fun clear() {
        val max = bits.size
        for (i in 0..max - 1) {
            bits[i] = 0
        }
    }

    /**
     *
     * Sets a square region of the bit matrix to true.

     * @param left The horizontal position to begin at (inclusive)
     * *
     * @param top The vertical position to begin at (inclusive)
     * *
     * @param width The width of the region
     * *
     * @param height The height of the region
     */
    fun setRegion(left: Int, top: Int, width: Int, height: Int) {
        if (top < 0 || left < 0) {
            throw IllegalArgumentException("Left and top must be nonnegative")
        }
        if (height < 1 || width < 1) {
            throw IllegalArgumentException("Height and width must be at least 1")
        }
        val right = left + width
        val bottom = top + height
        if (bottom > this.height || right > this.width) {
            throw IllegalArgumentException("The region must fit inside the matrix")
        }
        for (y in top..bottom - 1) {
            val offset = y * rowSize
            for (x in left..right - 1) {
                bits[offset + (x shr 5)] = bits[offset + (x shr 5)] or (1 shl (x and 0x1f))
            }
        }
    }

    /**
     * A fast method to retrieve one row of data from the matrix as a BitArray.

     * @param y The row to retrieve
     * *
     * @param row An optional caller-allocated BitArray, will be allocated if null or too small
     * *
     * @return The resulting BitArray - this reference should always be used even when passing
     * *         your own row
     */
    fun getRow(y: Int, row: BitArray?): BitArray {
        var row = row
        if (row == null || row.size < width) {
            row = BitArray(width)
        }
        val offset = y * rowSize
        for (x in 0..rowSize - 1) {
            row.setBulk(x shl 5, bits[offset + x])
        }
        return row
    }

    /**
     * This method is for compatibility with older code. It's only logical to call if the matrix
     * is square, so I'm throwing if that's not the case.

     * @return row/column dimension of this matrix
     */
    val dimension: Int
        get() {
            if (width != height) {
                throw RuntimeException("Can't call getDimension() on a non-square matrix")
            }
            return width
        }

    override fun toString(): String {
        val result = StringBuffer(height * (width + 1))
        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                result.append(if (get(x, y)) "X " else "  ")
            }
            result.append('\n')
        }
        return result.toString()
    }

}
