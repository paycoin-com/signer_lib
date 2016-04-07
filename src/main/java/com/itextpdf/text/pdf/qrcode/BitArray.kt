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
 * A simple, fast array of bits, represented compactly by an array of ints internally.

 * @author Sean Owen
 * *
 * @since 5.0.2
 */
class BitArray(val size: Int) {

    // TODO: I have changed these members to be public so ProGuard can inline get() and set(). Ideally
    // they'd be private and we'd use the -allowaccessmodification flag, but Dalvik rejects the
    // resulting binary at runtime on Android. If we find a solution to this, these should be changed
    // back to private.
    /**
     * @return underlying array of ints. The first element holds the first 32 bits, and the least
     * *         significant bit is bit 0.
     */
    var bitArray: IntArray

    init {
        if (size < 1) {
            throw IllegalArgumentException("size must be at least 1")
        }
        this.bitArray = makeArray(size)
    }

    /**
     * @param i bit to get
     * *
     * @return true iff bit i is set
     */
    operator fun get(i: Int): Boolean {
        return bitArray[i shr 5] and (1 shl (i and 0x1F)) != 0
    }

    /**
     * Sets bit i.

     * @param i bit to set
     */
    fun set(i: Int) {
        bitArray[i shr 5] = bitArray[i shr 5] or (1 shl (i and 0x1F))
    }

    /**
     * Flips bit i.

     * @param i bit to set
     */
    fun flip(i: Int) {
        bitArray[i shr 5] = bitArray[i shr 5] xor (1 shl (i and 0x1F))
    }

    /**
     * Sets a block of 32 bits, starting at bit i.

     * @param i first bit to set
     * *
     * @param newBits the new value of the next 32 bits. Note again that the least-significant bit
     * * corresponds to bit i, the next-least-significant to i+1, and so on.
     */
    fun setBulk(i: Int, newBits: Int) {
        bitArray[i shr 5] = newBits
    }

    /**
     * Clears all bits (sets to false).
     */
    fun clear() {
        val max = bitArray.size
        for (i in 0..max - 1) {
            bitArray[i] = 0
        }
    }

    /**
     * Efficient method to check if a range of bits is set, or not set.

     * @param start start of range, inclusive.
     * *
     * @param end end of range, exclusive
     * *
     * @param value if true, checks that bits in range are set, otherwise checks that they are not set
     * *
     * @return true iff all bits are set or not set in range, according to value argument
     * *
     * @throws IllegalArgumentException if end is less than or equal to start
     */
    fun isRange(start: Int, end: Int, value: Boolean): Boolean {
        var end = end
        if (end < start) {
            throw IllegalArgumentException()
        }
        if (end == start) {
            return true // empty range matches
        }
        end-- // will be easier to treat this as the last actually set bit -- inclusive    
        val firstInt = start shr 5
        val lastInt = end shr 5
        for (i in firstInt..lastInt) {
            val firstBit = if (i > firstInt) 0 else start and 0x1F
            val lastBit = if (i < lastInt) 31 else end and 0x1F
            var mask: Int
            if (firstBit == 0 && lastBit == 31) {
                mask = -1
            } else {
                mask = 0
                for (j in firstBit..lastBit) {
                    mask = mask or (1 shl j)
                }
            }

            // Return false if we're looking for 1s and the masked bits[i] isn't all 1s (that is,
            // equals the mask, or we're looking for 0s and the masked portion is not all 0s
            if (bitArray[i] and mask != (if (value) mask else 0)) {
                return false
            }
        }
        return true
    }

    /**
     * Reverses all bits in the array.
     */
    fun reverse() {
        val newBits = IntArray(bitArray.size)
        val size = this.size
        for (i in 0..size - 1) {
            if (get(size - i - 1)) {
                newBits[i shr 5] = newBits[i shr 5] or (1 shl (i and 0x1F))
            }
        }
        bitArray = newBits
    }

    private fun makeArray(size: Int): IntArray {
        var arraySize = size shr 5
        if (size and 0x1F != 0) {
            arraySize++
        }
        return IntArray(arraySize)
    }

    override fun toString(): String {
        val result = StringBuffer(size)
        for (i in 0..size - 1) {
            if (i and 0x07 == 0) {
                result.append(' ')
            }
            result.append(if (get(i)) 'X' else '.')
        }
        return result.toString()
    }

}