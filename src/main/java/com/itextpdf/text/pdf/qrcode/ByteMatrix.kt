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
 * A class which wraps a 2D array of bytes. The default usage is signed. If you want to use it as a
 * unsigned container, it's up to you to do byteValue & 0xff at each location.

 * JAVAPORT: The original code was a 2D array of ints, but since it only ever gets assigned
 * -1, 0, and 1, I'm going to use less memory and go with bytes.

 * @author dswitkin@google.com (Daniel Switkin)
 * *
 * @since 5.0.2
 */
class ByteMatrix(val width: Int, val height: Int) {

    val array: Array<ByteArray>

    init {
        array = Array(height) { ByteArray(width) }
    }

    operator fun get(x: Int, y: Int): Byte {
        return array[y][x]
    }

    operator fun set(x: Int, y: Int, value: Byte) {
        array[y][x] = value
    }

    operator fun set(x: Int, y: Int, value: Int) {
        array[y][x] = value.toByte()
    }

    fun clear(value: Byte) {
        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                array[y][x] = value
            }
        }
    }

    override fun toString(): String {
        val result = StringBuffer(2 * width * height + 2)
        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                when (array[y][x]) {
                    0 -> result.append(" 0")
                    1 -> result.append(" 1")
                    else -> result.append("  ")
                }
            }
            result.append('\n')
        }
        return result.toString()
    }

}
