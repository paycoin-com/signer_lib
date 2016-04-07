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

import com.itextpdf.text.io.StreamUtil

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Hashtable

/**
 * This class is the main entry point to the hyphenation package.
 * You can use only the static methods or create an instance.

 * @author Carlos Villegas @uniscope.co.jp>
 */
class Hyphenator
/**
 * @param lang
 * *
 * @param country
 * *
 * @param leftMin
 * *
 * @param rightMin
 */
(lang: String, country: String, leftMin: Int,
 rightMin: Int) {

    private var hyphenTree: HyphenationTree? = null
    private var remainCharCount = 2
    private var pushCharCount = 2

    init {
        hyphenTree = getHyphenationTree(lang, country)
        remainCharCount = leftMin
        pushCharCount = rightMin
    }

    /**
     * @param min
     */
    fun setMinRemainCharCount(min: Int) {
        remainCharCount = min
    }

    /**
     * @param min
     */
    fun setMinPushCharCount(min: Int) {
        pushCharCount = min
    }

    /**
     * @param lang
     * *
     * @param country
     */
    fun setLanguage(lang: String, country: String) {
        hyphenTree = getHyphenationTree(lang, country)
    }

    /**
     * @param word
     * *
     * @param offset
     * *
     * @param len
     * *
     * @return a hyphenation object
     */
    fun hyphenate(word: CharArray, offset: Int, len: Int): Hyphenation? {
        if (hyphenTree == null) {
            return null
        }
        return hyphenTree!!.hyphenate(word, offset, len, remainCharCount,
                pushCharCount)
    }

    /**
     * @param word
     * *
     * @return a hyphenation object
     */
    fun hyphenate(word: String): Hyphenation? {
        if (hyphenTree == null) {
            return null
        }
        return hyphenTree!!.hyphenate(word, remainCharCount, pushCharCount)
    }

    companion object {

        /** TODO: Don't use statics  */
        private val hyphenTrees = Hashtable<String, HyphenationTree>()
        private val defaultHyphLocation = "com/itextpdf/text/pdf/hyphenation/hyph/"

        /** Holds value of property hyphenDir.  */
        /** Getter for property hyphenDir.
         * @return Value of property hyphenDir.
         */
        /** Setter for property hyphenDir.
         * @param _hyphenDir New value of property hyphenDir.
         */
        var hyphenDir: String? = ""

        /**
         * @param lang
         * *
         * @param country
         * *
         * @return the hyphenation tree
         */
        fun getHyphenationTree(lang: String,
                               country: String?): HyphenationTree? {
            var key = lang
            // check whether the country code has been used
            if (country != null && country != "none") {
                key += "_" + country
            }
            // first try to find it in the cache
            if (hyphenTrees.containsKey(key)) {
                return hyphenTrees[key]
            }
            if (hyphenTrees.containsKey(lang)) {
                return hyphenTrees[lang]
            }

            var hTree = getResourceHyphenationTree(key)
            if (hTree == null)
                hTree = getFileHyphenationTree(key)
            // put it into the pattern cache
            if (hTree != null) {
                hyphenTrees.put(key, hTree)
            }
            return hTree
        }

        /**
         * @param key
         * *
         * @return a hyphenation tree
         */
        fun getResourceHyphenationTree(key: String): HyphenationTree? {
            try {
                var stream: InputStream? = StreamUtil.getResourceStream(defaultHyphLocation + key + ".xml")
                if (stream == null && key.length > 2)
                    stream = StreamUtil.getResourceStream(defaultHyphLocation + key.substring(0, 2) + ".xml")
                if (stream == null)
                    return null
                val hTree = HyphenationTree()
                hTree.loadSimplePatterns(stream)
                return hTree
            } catch (e: Exception) {
                return null
            }

        }

        /**
         * @param key
         * *
         * @return a hyphenation tree
         */
        fun getFileHyphenationTree(key: String): HyphenationTree? {
            try {
                if (hyphenDir == null)
                    return null
                var stream: InputStream? = null
                var hyphenFile = File(hyphenDir, key + ".xml")
                if (hyphenFile.canRead())
                    stream = FileInputStream(hyphenFile)
                if (stream == null && key.length > 2) {
                    hyphenFile = File(hyphenDir, key.substring(0, 2) + ".xml")
                    if (hyphenFile.canRead())
                        stream = FileInputStream(hyphenFile)
                }
                if (stream == null)
                    return null
                val hTree = HyphenationTree()
                hTree.loadSimplePatterns(stream)
                return hTree
            } catch (e: Exception) {
                return null
            }

        }

        /**
         * @param lang
         * *
         * @param country
         * *
         * @param word
         * *
         * @param leftMin
         * *
         * @param rightMin
         * *
         * @return a hyphenation object
         */
        fun hyphenate(lang: String, country: String,
                      word: String, leftMin: Int,
                      rightMin: Int): Hyphenation? {
            val hTree = getHyphenationTree(lang, country) ?: return null //log.error("Error building hyphenation tree for language "
                    //                       + lang);
            return hTree.hyphenate(word, leftMin, rightMin)
        }

        /**
         * @param lang
         * *
         * @param country
         * *
         * @param word
         * *
         * @param offset
         * *
         * @param len
         * *
         * @param leftMin
         * *
         * @param rightMin
         * *
         * @return a hyphenation object
         */
        fun hyphenate(lang: String, country: String,
                      word: CharArray, offset: Int, len: Int,
                      leftMin: Int, rightMin: Int): Hyphenation? {
            val hTree = getHyphenationTree(lang, country) ?: return null //log.error("Error building hyphenation tree for language "
                    //                       + lang);
            return hTree.hyphenate(word, offset, len, leftMin, rightMin)
        }
    }

}
