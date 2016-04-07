/*
 * $Id: bfc6b8d0892c5ec6ac5f91a6f34d0822924268c7 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf

import java.util.LinkedList

/**
 * This class expands a string into a list of numbers. The main use is to select a
 * range of pages.
 *
 *
 * The general syntax is:
 * [!][o][odd][e][even]start-end
 *
 *
 * You can have multiple ranges separated by commas ','. The '!' modifier removes the
 * range from what is already selected. The range changes are incremental, that is,
 * numbers are added or deleted as the range appears. The start or the end, but not both, can be omitted.
 */
class SequenceList protected constructor(range: String) {

    protected var text: CharArray
    protected var ptr: Int = 0
    protected var number: Int = 0
    protected var other: String

    protected var low: Int = 0
    protected var high: Int = 0
    protected var odd: Boolean = false
    protected var even: Boolean = false
    protected var inverse: Boolean = false

    init {
        ptr = 0
        text = range.toCharArray()
    }

    protected fun nextChar(): Char {
        while (true) {
            if (ptr >= text.size)
                return EOT
            val c = text[ptr++]
            if (c > ' ')
                return c
        }
    }

    protected fun putBack() {
        --ptr
        if (ptr < 0)
            ptr = 0
    }

    protected val type: Int
        get() {
            val buf = StringBuffer()
            var state = FIRST
            while (true) {
                val c = nextChar()
                if (c == EOT) {
                    if (state == DIGIT) {
                        number = Integer.parseInt(other = buf.toString())
                        return NUMBER
                    } else if (state == OTHER) {
                        other = buf.toString().toLowerCase()
                        return TEXT
                    }
                    return END
                }
                when (state) {
                    FIRST -> {
                        when (c) {
                            '!' -> return NOT
                            '-' -> return MINUS
                            ',' -> return COMMA
                        }
                        buf.append(c)
                        if (c >= '0' && c <= '9')
                            state = DIGIT
                        else
                            state = OTHER
                    }
                    DIGIT -> if (c >= '0' && c <= '9')
                        buf.append(c)
                    else {
                        putBack()
                        number = Integer.parseInt(other = buf.toString())
                        return NUMBER
                    }
                    OTHER -> if (NOT_OTHER.indexOf(c.toInt()) < 0)
                        buf.append(c)
                    else {
                        putBack()
                        other = buf.toString().toLowerCase()
                        return TEXT
                    }
                }
            }
        }

    private fun otherProc() {
        if (other == "odd" || other == "o") {
            odd = true
            even = false
        } else if (other == "even" || other == "e") {
            odd = false
            even = true
        }
    }

    protected val attributes: Boolean
        get() {
            low = -1
            high = -1
            odd = even = inverse = false
            var state = OTHER
            while (true) {
                val type = type
                if (type == END || type == COMMA) {
                    if (state == DIGIT)
                        high = low
                    return type == END
                }
                when (state) {
                    OTHER -> when (type) {
                        NOT -> inverse = true
                        MINUS -> state = DIGIT2
                        else -> if (type == NUMBER) {
                            low = number
                            state = DIGIT
                        } else
                            otherProc()
                    }
                    DIGIT -> when (type) {
                        NOT -> {
                            inverse = true
                            state = OTHER
                            high = low
                        }
                        MINUS -> state = DIGIT2
                        else -> {
                            high = low
                            state = OTHER
                            otherProc()
                        }
                    }
                    DIGIT2 -> when (type) {
                        NOT -> {
                            inverse = true
                            state = OTHER
                        }
                        MINUS -> {
                        }
                        NUMBER -> {
                            high = number
                            state = OTHER
                        }
                        else -> {
                            state = OTHER
                            otherProc()
                        }
                    }
                }
            }
        }

    companion object {
        protected val COMMA = 1
        protected val MINUS = 2
        protected val NOT = 3
        protected val TEXT = 4
        protected val NUMBER = 5
        protected val END = 6
        protected val EOT = '\uffff'

        private val FIRST = 0
        private val DIGIT = 1
        private val OTHER = 2
        private val DIGIT2 = 3
        private val NOT_OTHER = "-,!0123456789"

        /**
         * Generates a list of numbers from a string.
         * @param ranges the comma separated ranges
         * *
         * @param maxNumber the maximum number in the range
         * *
         * @return a list with the numbers as Integer
         */
        fun expand(ranges: String, maxNumber: Int): List<Int> {
            val parse = SequenceList(ranges)
            val list = LinkedList<Int>()
            var sair = false
            while (!sair) {
                sair = parse.attributes
                if (parse.low == -1 && parse.high == -1 && !parse.even && !parse.odd)
                    continue
                if (parse.low < 1)
                    parse.low = 1
                if (parse.high < 1 || parse.high > maxNumber)
                    parse.high = maxNumber
                if (parse.low > maxNumber)
                    parse.low = maxNumber

                //System.out.println("low="+parse.low+",high="+parse.high+",odd="+parse.odd+",even="+parse.even+",inverse="+parse.inverse);
                var inc = 1
                if (parse.inverse) {
                    if (parse.low > parse.high) {
                        val t = parse.low
                        parse.low = parse.high
                        parse.high = t
                    }
                    val it = list.listIterator()
                    while (it.hasNext()) {
                        val n = it.next().toInt()
                        if (parse.even && n and 1 == 1)
                            continue
                        if (parse.odd && n and 1 == 0)
                            continue
                        if (n >= parse.low && n <= parse.high)
                            it.remove()
                    }
                } else {
                    if (parse.low > parse.high) {
                        inc = -1
                        if (parse.odd || parse.even) {
                            --inc
                            if (parse.even)
                                parse.low = parse.low and 1.inv()
                            else
                                parse.low -= if (parse.low and 1 == 1) 0 else 1
                        }
                        var k = parse.low
                        while (k >= parse.high) {
                            list.add(Integer.valueOf(k))
                            k += inc
                        }
                    } else {
                        if (parse.odd || parse.even) {
                            ++inc
                            if (parse.odd)
                                parse.low = parse.low or 1
                            else
                                parse.low += if (parse.low and 1 == 1) 1 else 0
                        }
                        var k = parse.low
                        while (k <= parse.high) {
                            list.add(Integer.valueOf(k))
                            k += inc
                        }
                    }
                }
                //            for (int k = 0; k < list.size(); ++k)
                //                System.out.print(((Integer)list.get(k)).intValue() + ",");
                //            System.out.println();
            }
            return list
        }
    }
}
