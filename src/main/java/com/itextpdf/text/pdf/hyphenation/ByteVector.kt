/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package com.itextpdf.text.pdf.hyphenation

import java.io.Serializable

/**
 * This class implements a simple byte vector with access to the
 * underlying array.

 * @author Carlos Villegas @uniscope.co.jp>
 */
class ByteVector : Serializable {
    private var blockSize: Int = 0

    /**
     * The encapsulated array
     */
    var array: ByteArray? = null
        private set

    /**
     * Points to next free item
     */
    private var n: Int = 0

    @JvmOverloads constructor(capacity: Int = DEFAULT_BLOCK_SIZE) {
        if (capacity > 0) {
            blockSize = capacity
        } else {
            blockSize = DEFAULT_BLOCK_SIZE
        }
        array = ByteArray(blockSize)
        n = 0
    }

    constructor(a: ByteArray) {
        blockSize = DEFAULT_BLOCK_SIZE
        array = a
        n = 0
    }

    constructor(a: ByteArray, capacity: Int) {
        if (capacity > 0) {
            blockSize = capacity
        } else {
            blockSize = DEFAULT_BLOCK_SIZE
        }
        array = a
        n = 0
    }

    /**
     * return number of items in array
     */
    fun length(): Int {
        return n
    }

    /**
     * returns current capacity of array
     */
    fun capacity(): Int {
        return array!!.size
    }

    fun put(index: Int, `val`: Byte) {
        array[index] = `val`
    }

    operator fun get(index: Int): Byte {
        return array!![index]
    }

    /**
     * This is to implement memory allocation in the array. Like malloc().
     */
    fun alloc(size: Int): Int {
        val index = n
        val len = array!!.size
        if (n + size >= len) {
            val aux = ByteArray(len + blockSize)
            System.arraycopy(array, 0, aux, 0, len)
            array = aux
        }
        n += size
        return index
    }

    fun trimToSize() {
        if (n < array!!.size) {
            val aux = ByteArray(n)
            System.arraycopy(array, 0, aux, 0, n)
            array = aux
        }
    }

    companion object {

        private val serialVersionUID = -1096301185375029343L
        /**
         * Capacity increment size
         */
        private val DEFAULT_BLOCK_SIZE = 2048
    }

}
