/*
 * $Id: ca67218c421f187de7b7633741e9a6a302ed82a6 $
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
package com.itextpdf.text.factories

/**
 * This class can produce String combinations representing a roman number.
 */
object RomanNumberFactory {
    /**
     * Helper class for Roman Digits
     */
    private class RomanDigit
    /**
     * Constructs a roman digit
     * @param digit the roman digit
     * *
     * @param value the value
     * *
     * @param pre can it be used as a prefix
     */
    internal constructor(
            /** part of a roman number  */
            var digit: Char,
            /** value of the roman digit  */
            var value: Int,
            /** can the digit be used as a prefix  */
            var pre: Boolean)

    /**
     * Array with Roman digits.
     */
    private val roman = arrayOf(RomanDigit('m', 1000, false), RomanDigit('d', 500, false), RomanDigit('c', 100, true), RomanDigit('l', 50, false), RomanDigit('x', 10, true), RomanDigit('v', 5, false), RomanDigit('i', 1, true))

    /**
     * Changes an int into a lower case roman number.
     * @param index the original number
     * *
     * @return the roman number (lower case)
     */
    fun getString(index: Int): String {
        var index = index
        val buf = StringBuffer()

        // lower than 0 ? Add minus
        if (index < 0) {
            buf.append('-')
            index = -index
        }

        // greater than 3000
        if (index > 3000) {
            buf.append('|')
            buf.append(getString(index / 1000))
            buf.append('|')
            // remainder
            index = index - index / 1000 * 1000
        }

        // number between 1 and 3000
        var pos = 0
        while (true) {
            // loop over the array with values for m-d-c-l-x-v-i
            val dig = roman[pos]
            // adding as many digits as we can
            while (index >= dig.value) {
                buf.append(dig.digit)
                index -= dig.value
            }
            // we have the complete number
            if (index <= 0) {
                break
            }
            // look for the next digit that can be used in a special way
            var j = pos
            while (!roman[++j].pre)

            // does the special notation apply?
                if (index + roman[j].value >= dig.value) {
                    buf.append(roman[j].digit).append(dig.digit)
                    index -= dig.value - roman[j].value
                }
            pos++
        }
        return buf.toString()
    }

    /**
     * Changes an int into a lower case roman number.
     * @param index the original number
     * *
     * @return the roman number (lower case)
     */
    fun getLowerCaseString(index: Int): String {
        return getString(index)
    }

    /**
     * Changes an int into an upper case roman number.
     * @param index the original number
     * *
     * @return the roman number (lower case)
     */
    fun getUpperCaseString(index: Int): String {
        return getString(index).toUpperCase()
    }

    /**
     * Changes an int into a roman number.
     * @param index the original number
     * *
     * @param lowercase true for lowercase, false otherwise
     * *
     * @return the roman number (lower case)
     */
    fun getString(index: Int, lowercase: Boolean): String {
        if (lowercase) {
            return getLowerCaseString(index)
        } else {
            return getUpperCaseString(index)
        }
    }
}
