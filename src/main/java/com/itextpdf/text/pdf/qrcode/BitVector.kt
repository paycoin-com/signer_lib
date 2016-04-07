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
 * JAVAPORT: This should be combined with BitArray in the future, although that class is not yet
 * dynamically resizeable. This implementation is reasonable but there is a lot of function calling
 * in loops I'd like to get rid of.

 * @author satorux@google.com (Satoru Takabayashi) - creator
 * *
 * @author dswitkin@google.com (Daniel Switkin) - ported from C++
 * *
 * @since 5.0.2
 */
class BitVector {

    private var sizeInBits: Int = 0
    // Callers should not assume that array.length is the exact number of bytes needed to hold
    // sizeInBits - it will typically be larger for efficiency.
    var array: ByteArray? = null
        private set

    init {
        sizeInBits = 0
        array = ByteArray(DEFAULT_SIZE_IN_BYTES)
    }

    // Return the bit value at "index".
    fun at(index: Int): Int {
        if (index < 0 || index >= sizeInBits) {
            throw IllegalArgumentException("Bad index: " + index)
        }
        val value = array!![index shr 3] and 0xff
        return value shr 7 - (index and 0x7) and 1
    }

    // Return the number of bits in the bit vector.
    fun size(): Int {
        return sizeInBits
    }

    // Return the number of bytes in the bit vector.
    fun sizeInBytes(): Int {
        return sizeInBits + 7 shr 3
    }

    // Append one bit to the bit vector.
    fun appendBit(bit: Int) {
        if (!(bit == 0 || bit == 1)) {
            throw IllegalArgumentException("Bad bit")
        }
        val numBitsInLastByte = sizeInBits and 0x7
        // We'll expand array if we don't have bits in the last byte.
        if (numBitsInLastByte == 0) {
            appendByte(0)
            sizeInBits -= 8
        }
        // Modify the last byte.
        array[sizeInBits shr 3] = array[sizeInBits shr 3] or (bit shl 7 - numBitsInLastByte).toByte()
        ++sizeInBits
    }

    // Append "numBits" bits in "value" to the bit vector.
    // REQUIRES: 0<= numBits <= 32.
    //
    // Examples:
    // - appendBits(0x00, 1) adds 0.
    // - appendBits(0x00, 4) adds 0000.
    // - appendBits(0xff, 8) adds 11111111.
    fun appendBits(value: Int, numBits: Int) {
        if (numBits < 0 || numBits > 32) {
            throw IllegalArgumentException("Num bits must be between 0 and 32")
        }
        var numBitsLeft = numBits
        while (numBitsLeft > 0) {
            // Optimization for byte-oriented appending.
            if (sizeInBits and 0x7 == 0 && numBitsLeft >= 8) {
                val newByte = value shr numBitsLeft - 8 and 0xff
                appendByte(newByte)
                numBitsLeft -= 8
            } else {
                val bit = value shr numBitsLeft - 1 and 1
                appendBit(bit)
                --numBitsLeft
            }
        }
    }

    // Append "bits".
    fun appendBitVector(bits: BitVector) {
        val size = bits.size()
        for (i in 0..size - 1) {
            appendBit(bits.at(i))
        }
    }

    // Modify the bit vector by XOR'ing with "other"
    fun xor(other: BitVector) {
        if (sizeInBits != other.size()) {
            throw IllegalArgumentException("BitVector sizes don't match")
        }
        val sizeInBytes = sizeInBits + 7 shr 3
        for (i in 0..sizeInBytes - 1) {
            // The last byte could be incomplete (i.e. not have 8 bits in
            // it) but there is no problem since 0 XOR 0 == 0.
            array[i] = array[i] xor other.array!![i]
        }
    }

    // Return String like "01110111" for debugging.
    override fun toString(): String {
        val result = StringBuffer(sizeInBits)
        for (i in 0..sizeInBits - 1) {
            if (at(i) == 0) {
                result.append('0')
            } else if (at(i) == 1) {
                result.append('1')
            } else {
                throw IllegalArgumentException("Byte isn't 0 or 1")
            }
        }
        return result.toString()
    }

    // Add a new byte to the end, possibly reallocating and doubling the size of the array if we've
    // run out of room.
    private fun appendByte(value: Int) {
        if (sizeInBits shr 3 == array!!.size) {
            val newArray = ByteArray(array!!.size shl 1)
            System.arraycopy(array, 0, newArray, 0, array!!.size)
            array = newArray
        }
        array[sizeInBits shr 3] = value.toByte()
        sizeInBits += 8
    }

    companion object {

        // For efficiency, start out with some room to work.
        private val DEFAULT_SIZE_IN_BYTES = 32
    }

}
