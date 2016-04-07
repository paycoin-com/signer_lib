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
 * This class implements an array of unsigned bytes.

 * @author dswitkin@google.com (Daniel Switkin)
 * *
 * @since 5.0.2
 */
class ByteArray {

    private var bytes: ByteArray? = null
    private var size: Int = 0

    constructor() {
        bytes = null
        size = 0
    }

    constructor(size: Int) {
        bytes = ByteArray(size)
        this.size = size
    }

    constructor(byteArray: ByteArray) {
        bytes = byteArray
        size = bytes!!.size
    }

    /**
     * Access an unsigned byte at location index.
     * @param index The index in the array to access.
     * *
     * @return The unsigned value of the byte as an int.
     */
    fun at(index: Int): Int {
        return bytes!![index] and 0xff
    }

    operator fun set(index: Int, value: Int) {
        bytes[index] = value.toByte()
    }

    fun size(): Int {
        return size
    }

    val isEmpty: Boolean
        get() = size == 0

    fun appendByte(value: Int) {
        if (size == 0 || size >= bytes!!.size) {
            val newSize = Math.max(INITIAL_SIZE, size shl 1)
            reserve(newSize)
        }
        bytes[size] = value.toByte()
        size++
    }

    fun reserve(capacity: Int) {
        if (bytes == null || bytes!!.size < capacity) {
            val newArray = ByteArray(capacity)
            if (bytes != null) {
                System.arraycopy(bytes, 0, newArray, 0, bytes!!.size)
            }
            bytes = newArray
        }
    }

    // Copy count bytes from array source starting at offset.
    operator fun set(source: ByteArray, offset: Int, count: Int) {
        bytes = ByteArray(count)
        size = count
        for (x in 0..count - 1) {
            bytes[x] = source[offset + x]
        }
    }

    companion object {

        private val INITIAL_SIZE = 32
    }

}
