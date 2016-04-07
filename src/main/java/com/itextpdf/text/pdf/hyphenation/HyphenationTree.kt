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

/* $Id: 3c6875599cb2342bc6a68ce055d7b4cb7160b57f $ */

package com.itextpdf.text.pdf.hyphenation

import java.io.InputStream
import java.util.ArrayList
import java.util.HashMap

/**
 * This tree structure stores the hyphenation patterns in an efficient
 * way for fast lookup. It provides the provides the method to
 * hyphenate a word.

 * @author Carlos Villegas @uniscope.co.jp>
 */
class HyphenationTree : TernaryTree(), PatternConsumer {

    /**
     * value space: stores the interletter values
     */
    protected var vspace: ByteVector

    /**
     * This map stores hyphenation exceptions
     */
    protected var stoplist: HashMap<String, ArrayList<Any>>

    /**
     * This map stores the character classes
     */
    protected var classmap: TernaryTree

    /**
     * Temporary map to store interletter values on pattern loading.
     */
    @Transient private var ivalues: TernaryTree? = null

    init {
        stoplist = HashMap<String, ArrayList<Any>>(23)    // usually a small table
        classmap = TernaryTree()
        vspace = ByteVector()
        vspace.alloc(1)    // this reserves index 0, which we don't use
    }

    /**
     * Packs the values by storing them in 4 bits, two values into a byte
     * Values range is from 0 to 9. We use zero as terminator,
     * so we'll add 1 to the value.
     * @param values a string of digits from '0' to '9' representing the
     * * interletter values.
     * *
     * @return the index into the vspace array where the packed values
     * * are stored.
     */
    protected fun packValues(values: String): Int {
        var i: Int
        val n = values.length
        val m = if (n and 1 == 1) (n shr 1) + 2 else (n shr 1) + 1
        val offset = vspace.alloc(m)
        val va = vspace.array
        i = 0
        while (i < n) {
            val j = i shr 1
            val v = (values[i] - '0' + 1 and 0x0f).toByte()
            if (i and 1 == 1) {
                va[j + offset] = (va[j + offset] or v).toByte()
            } else {
                va[j + offset] = (v shl 4).toByte()    // big endian
            }
            i++
        }
        va[m - 1 + offset] = 0    // terminator
        return offset
    }

    protected fun unpackValues(k: Int): String {
        var k = k
        val buf = StringBuffer()
        var v = vspace.get(k++)
        while (v.toInt() != 0) {
            var c = (v.ushr(4) - 1 + '0').toChar()
            buf.append(c)
            c = (v and 0x0f).toChar()
            if (c.toInt() == 0) {
                break
            }
            c = (c.toInt() - 1 + '0').toChar()
            buf.append(c)
            v = vspace.get(k++)
        }
        return buf.toString()
    }

    fun loadSimplePatterns(stream: InputStream) {
        val pp = SimplePatternParser()
        ivalues = TernaryTree()

        pp.parse(stream, this)

        // patterns/values should be now in the tree
        // let's optimize a bit
        trimToSize()
        vspace.trimToSize()
        classmap.trimToSize()

        // get rid of the auxiliary map
        ivalues = null
    }


    fun findPattern(pat: String): String {
        val k = super.find(pat)
        if (k >= 0) {
            return unpackValues(k)
        }
        return ""
    }

    /**
     * String compare, returns 0 if equal or
     * t is a substring of s
     */
    protected fun hstrcmp(s: CharArray, si: Int, t: CharArray, ti: Int): Int {
        var si = si
        var ti = ti
        while (s[si] == t[ti]) {
            if (s[si].toInt() == 0) {
                return 0
            }
            si++
            ti++
        }
        if (t[ti].toInt() == 0) {
            return 0
        }
        return s[si] - t[ti]
    }

    protected fun getValues(k: Int): ByteArray {
        var k = k
        val buf = StringBuffer()
        var v = vspace.get(k++)
        while (v.toInt() != 0) {
            var c = (v.ushr(4) - 1).toChar()
            buf.append(c)
            c = (v and 0x0f).toChar()
            if (c.toInt() == 0) {
                break
            }
            c = (c.toInt() - 1).toChar()
            buf.append(c)
            v = vspace.get(k++)
        }
        val res = ByteArray(buf.length)
        for (i in res.indices) {
            res[i] = buf[i].toByte()
        }
        return res
    }

    /**
     *
     * Search for all possible partial matches of word starting
     * at index an update interletter values. In other words, it
     * does something like:
     * `
     * for(i=0; i &lt; patterns.length; i++) {
     * if ( word.substring(index).startsWidth(patterns[i]) )
     * update_interletter_values(patterns[i]);
     * }
    ` *
     *
     * But it is done in an efficient way since the patterns are
     * stored in a ternary tree. In fact, this is the whole purpose
     * of having the tree: doing this search without having to test
     * every single pattern. The number of patterns for languages
     * such as English range from 4000 to 10000. Thus, doing thousands
     * of string comparisons for each word to hyphenate would be
     * really slow without the tree. The tradeoff is memory, but
     * using a ternary tree instead of a trie, almost halves the
     * the memory used by Lout or TeX. It's also faster than using
     * a hash table
     * @param word null terminated word to match
     * *
     * @param index start index from word
     * *
     * @param il interletter values array to update
     */
    protected fun searchPatterns(word: CharArray, index: Int, il: ByteArray) {
        var values: ByteArray
        var i = index
        var p: Char
        var q: Char
        var sp = word[i]
        p = root

        while (p.toInt() > 0 && p.toInt() < sc.size) {
            if (sc[p].toInt() == 0xFFFF) {
                if (hstrcmp(word, i, kv.array, lo[p].toInt()) == 0) {
                    values = getValues(eq[p].toInt())    // data pointer is in eq[]
                    var j = index
                    for (value in values) {
                        if (j < il.size && value > il[j]) {
                            il[j] = value
                        }
                        j++
                    }
                }
                return
            }
            val d = sp - sc[p]
            if (d == 0) {
                if (sp.toInt() == 0) {
                    break
                }
                sp = word[++i]
                p = eq[p]
                q = p

                // look for a pattern ending at this position by searching for
                // the null char ( splitchar == 0 )
                while (q.toInt() > 0 && q.toInt() < sc.size) {
                    if (sc[q].toInt() == 0xFFFF) {
                        // stop at compressed branch
                        break
                    }
                    if (sc[q].toInt() == 0) {
                        values = getValues(eq[q].toInt())
                        var j = index
                        for (value in values) {
                            if (j < il.size && value > il[j]) {
                                il[j] = value
                            }
                            j++
                        }
                        break
                    } else {
                        q = lo[q]

                        /**
                         * actually the code should be:
                         * q = sc[q] < 0 ? hi[q] : lo[q];
                         * but java chars are unsigned
                         */
                    }
                }
            } else {
                p = if (d < 0) lo[p] else hi[p]
            }
        }
    }

    /**
     * Hyphenate word and return a Hyphenation object.
     * @param word the word to be hyphenated
     * *
     * @param remainCharCount Minimum number of characters allowed
     * * before the hyphenation point.
     * *
     * @param pushCharCount Minimum number of characters allowed after
     * * the hyphenation point.
     * *
     * @return a [Hyphenation] object representing
     * * the hyphenated word or null if word is not hyphenated.
     */
    fun hyphenate(word: String, remainCharCount: Int,
                  pushCharCount: Int): Hyphenation {
        val w = word.toCharArray()
        return hyphenate(w, 0, w.size, remainCharCount, pushCharCount)
    }

    /**
     * w = "****nnllllllnnn*****",
     * where n is a non-letter, l is a letter,
     * all n may be absent, the first n is at offset,
     * the first l is at offset + iIgnoreAtBeginning;
     * word = ".llllll.'\0'***",
     * where all l in w are copied into word.
     * In the first part of the routine len = w.length,
     * in the second part of the routine len = word.length.
     * Three indices are used:
     * index(w), the index in w,
     * index(word), the index in word,
     * letterindex(word), the index in the letter part of word.
     * The following relations exist:
     * index(w) = offset + i - 1
     * index(word) = i - iIgnoreAtBeginning
     * letterindex(word) = index(word) - 1
     * (see first loop).
     * It follows that:
     * index(w) - index(word) = offset - 1 + iIgnoreAtBeginning
     * index(w) = letterindex(word) + offset + iIgnoreAtBeginning
     */

    /**
     * Hyphenate word and return an array of hyphenation points.
     * @param w char array that contains the word
     * *
     * @param offset Offset to first character in word
     * *
     * @param len Length of word
     * *
     * @param remainCharCount Minimum number of characters allowed
     * * before the hyphenation point.
     * *
     * @param pushCharCount Minimum number of characters allowed after
     * * the hyphenation point.
     * *
     * @return a [Hyphenation] object representing
     * * the hyphenated word or null if word is not hyphenated.
     */
    fun hyphenate(w: CharArray, offset: Int, len: Int,
                  remainCharCount: Int, pushCharCount: Int): Hyphenation? {
        var len = len
        var i: Int
        val word = CharArray(len + 3)

        // normalize word
        val c = CharArray(2)
        var iIgnoreAtBeginning = 0
        var iLength = len
        var bEndOfLetters = false
        i = 1
        while (i <= len) {
            c[0] = w[offset + i - 1]
            val nc = classmap.find(c, 0)
            if (nc < 0) {
                // found a non-letter character ...
                if (i == 1 + iIgnoreAtBeginning) {
                    // ... before any letter character
                    iIgnoreAtBeginning++
                } else {
                    // ... after a letter character
                    bEndOfLetters = true
                }
                iLength--
            } else {
                if (!bEndOfLetters) {
                    word[i - iIgnoreAtBeginning] = nc.toChar()
                } else {
                    return null
                }
            }
            i++
        }
        val origlen = len
        len = iLength
        if (len < remainCharCount + pushCharCount) {
            // word is too short to be hyphenated
            return null
        }
        val result = IntArray(len + 1)
        var k = 0

        // check exception list first
        val sw = String(word, 1, len)
        if (stoplist.containsKey(sw)) {
            // assume only simple hyphens (Hyphen.pre="-", Hyphen.post = Hyphen.no = null)
            val hw = stoplist[sw]
            var j = 0
            i = 0
            while (i < hw.size) {
                val o = hw.get(i)
                // j = index(sw) = letterindex(word)?
                // result[k] = corresponding index(w)
                if (o is String) {
                    j += o.length
                    if (j >= remainCharCount && j < len - pushCharCount) {
                        result[k++] = j + iIgnoreAtBeginning
                    }
                }
                i++
            }
        } else {
            // use algorithm to get hyphenation points
            word[0] = '.'                    // word start marker
            word[len + 1] = '.'              // word end marker
            word[len + 2] = 0.toChar()                // null terminated
            val il = ByteArray(len + 3)    // initialized to zero
            i = 0
            while (i < len + 1) {
                searchPatterns(word, i, il)
                i++
            }

            // hyphenation points are located where interletter value is odd
            // i is letterindex(word),
            // i + 1 is index(word),
            // result[k] = corresponding index(w)
            i = 0
            while (i < len) {
                if (il[i + 1] and 1 == 1 && i >= remainCharCount
                        && i <= len - pushCharCount) {
                    result[k++] = i + iIgnoreAtBeginning
                }
                i++
            }
        }


        if (k > 0) {
            // trim result array
            val res = IntArray(k)
            System.arraycopy(result, 0, res, 0, k)
            return Hyphenation(String(w, offset, origlen), res)
        } else {
            return null
        }
    }

    /**
     * Add a character class to the tree. It is used by
     * [SimplePatternParser] as callback to
     * add character classes. Character classes define the
     * valid word characters for hyphenation. If a word contains
     * a character not defined in any of the classes, it is not hyphenated.
     * It also defines a way to normalize the characters in order
     * to compare them with the stored patterns. Usually pattern
     * files use only lower case characters, in this case a class
     * for letter 'a', for example, should be defined as "aA", the first
     * character being the normalization char.
     */
    override fun addClass(chargroup: String) {
        if (chargroup.length > 0) {
            val equivChar = chargroup[0]
            val key = CharArray(2)
            key[1] = 0.toChar()
            for (i in 0..chargroup.length - 1) {
                key[0] = chargroup[i]
                classmap.insert(key, 0, equivChar)
            }
        }
    }

    /**
     * Add an exception to the tree. It is used by
     * [SimplePatternParser] class as callback to
     * store the hyphenation exceptions.
     * @param word normalized word
     * *
     * @param hyphenatedword a vector of alternating strings and
     * * [hyphen][Hyphen] objects.
     */
    override fun addException(word: String, hyphenatedword: ArrayList<Any>) {
        stoplist.put(word, hyphenatedword)
    }

    /**
     * Add a pattern to the tree. Mainly, to be used by
     * [SimplePatternParser] class as callback to
     * add a pattern to the tree.
     * @param pattern the hyphenation pattern
     * *
     * @param ivalue interletter weight values indicating the
     * * desirability and priority of hyphenating at a given point
     * * within the pattern. It should contain only digit characters.
     * * (i.e. '0' to '9').
     */
    override fun addPattern(pattern: String, ivalue: String) {
        var k = ivalues!!.find(ivalue)
        if (k <= 0) {
            k = packValues(ivalue)
            ivalues!!.insert(ivalue, k.toChar())
        }
        insert(pattern, k.toChar())
    }

    override fun printStats() {
        println("Value space size = " + Integer.toString(vspace.length()))
        super.printStats()
    }

    companion object {

        private val serialVersionUID = -7763254239309429432L
    }
}
