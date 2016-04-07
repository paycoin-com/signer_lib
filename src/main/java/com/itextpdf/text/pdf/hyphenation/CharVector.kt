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
 * This class implements a simple char vector with access to the
 * underlying array.

 * @author Carlos Villegas @uniscope.co.jp>
 */
class CharVector : Cloneable, Serializable {
    private var blockSize: Int = 0

    /**
     * The encapsulated array
     */
    var array: CharArray? = null
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
        array = CharArray(blockSize)
        n = 0
    }

    constructor(a: CharArray) {
        blockSize = DEFAULT_BLOCK_SIZE
        array = a
        n = a.size
    }

    constructor(a: CharArray, capacity: Int) {
        if (capacity > 0) {
            blockSize = capacity
        } else {
            blockSize = DEFAULT_BLOCK_SIZE
        }
        array = a
        n = a.size
    }

    /**
     * Reset Vector but don't resize or clear elements
     */
    fun clear() {
        n = 0
    }

    public override fun clone(): Any {
        val cv = CharVector(array!!.clone(), blockSize)
        cv.n = this.n
        return cv
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

    fun put(index: Int, `val`: Char) {
        array[index] = `val`
    }

    operator fun get(index: Int): Char {
        return array!![index]
    }

    fun alloc(size: Int): Int {
        val index = n
        val len = array!!.size
        if (n + size >= len) {
            val aux = CharArray(len + blockSize)
            System.arraycopy(array, 0, aux, 0, len)
            array = aux
        }
        n += size
        return index
    }

    fun trimToSize() {
        if (n < array!!.size) {
            val aux = CharArray(n)
            System.arraycopy(array, 0, aux, 0, n)
            array = aux
        }
    }

    companion object {

        private val serialVersionUID = -4875768298308363544L
        /**
         * Capacity increment size
         */
        private val DEFAULT_BLOCK_SIZE = 2048
    }

}
