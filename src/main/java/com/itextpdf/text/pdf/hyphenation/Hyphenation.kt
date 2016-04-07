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

/**
 * This class represents a hyphenated word.

 * @author Carlos Villegas @uniscope.co.jp>
 */
class Hyphenation
/**
 * rawWord as made of alternating strings and [Hyphen]
 * instances
 */
internal constructor(private val word: String,
                     /**
                      * @return the hyphenation points
                      */
                     val hyphenationPoints: IntArray) {

    /**
     * number of hyphenation points in word
     */
    private val len: Int

    init {
        len = hyphenationPoints.size
    }

    /**
     * @return the number of hyphenation points in the word
     */
    fun length(): Int {
        return len
    }

    /**
     * @return the pre-break text, not including the hyphen character
     */
    fun getPreHyphenText(index: Int): String {
        return word.substring(0, hyphenationPoints[index])
    }

    /**
     * @return the post-break text
     */
    fun getPostHyphenText(index: Int): String {
        return word.substring(hyphenationPoints[index])
    }

    override fun toString(): String {
        val str = StringBuffer()
        var start = 0
        for (i in 0..len - 1) {
            str.append(word.substring(start, hyphenationPoints[i])).append('-')
            start = hyphenationPoints[i]
        }
        str.append(word.substring(start))
        return str.toString()
    }

}
