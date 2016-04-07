/*
 * $Id: d9f4a907448b80434ba590d02a7153784dd5d51d $
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
package com.itextpdf.text

import com.itextpdf.text.Font.FontFamily

/**
 * This class contains the symbols that correspond with special symbols.
 *
 * When you construct a Phrase with Phrase.getInstance using a String,
 * this String can contain special Symbols. These are characters with an int value
 * between 913 and 937 (except 930) and between 945 and 969. With this class the value of the
 * corresponding character of the Font Symbol, can be retrieved.

 * @see Phrase


 * @author  Bruno Lowagie
 * *
 * @author  Evelyne De Cordier
 */

object SpecialSymbol {

    /**
     * Returns the first occurrence of a special symbol in a String.

     * @param    string        a String
     * *
     * @return    an index of -1 if no special symbol was found
     */
    fun index(string: String): Int {
        val length = string.length
        for (i in 0..length - 1) {
            if (getCorrespondingSymbol(string[i]) != ' ') {
                return i
            }
        }
        return -1
    }

    /**
     * Gets a chunk with a symbol character.
     * @param c a character that has to be changed into a symbol
     * *
     * @param font Font if there is no SYMBOL character corresponding with c
     * *
     * @return a SYMBOL version of a character
     */
    operator fun get(c: Char, font: Font): Chunk {
        val greek = SpecialSymbol.getCorrespondingSymbol(c)
        if (greek == ' ') {
            return Chunk(c.toString(), font)
        }
        val symbol = Font(FontFamily.SYMBOL, font.size, font.style, font.color)
        val s = greek.toString()
        return Chunk(s, symbol)
    }

    /**
     * Looks for the corresponding symbol in the font Symbol.

     * @param    c    the original ASCII-char
     * *
     * @return    the corresponding symbol in font Symbol
     */
    fun getCorrespondingSymbol(c: Char): Char {
        when (c) {
            913 -> return 'A' // ALFA
            914 -> return 'B' // BETA
            915 -> return 'G' // GAMMA
            916 -> return 'D' // DELTA
            917 -> return 'E' // EPSILON
            918 -> return 'Z' // ZETA
            919 -> return 'H' // ETA
            920 -> return 'Q' // THETA
            921 -> return 'I' // IOTA
            922 -> return 'K' // KAPPA
            923 -> return 'L' // LAMBDA
            924 -> return 'M' // MU
            925 -> return 'N' // NU
            926 -> return 'X' // XI
            927 -> return 'O' // OMICRON
            928 -> return 'P' // PI
            929 -> return 'R' // RHO
            931 -> return 'S' // SIGMA
            932 -> return 'T' // TAU
            933 -> return 'U' // UPSILON
            934 -> return 'F' // PHI
            935 -> return 'C' // CHI
            936 -> return 'Y' // PSI
            937 -> return 'W' // OMEGA
            945 -> return 'a' // alfa
            946 -> return 'b' // beta
            947 -> return 'g' // gamma
            948 -> return 'd' // delta
            949 -> return 'e' // epsilon
            950 -> return 'z' // zeta
            951 -> return 'h' // eta
            952 -> return 'q' // theta
            953 -> return 'i' // iota
            954 -> return 'k' // kappa
            955 -> return 'l' // lambda
            956 -> return 'm' // mu
            957 -> return 'n' // nu
            958 -> return 'x' // xi
            959 -> return 'o' // omicron
            960 -> return 'p' // pi
            961 -> return 'r' // rho
            962 -> return 'V' // sigma
            963 -> return 's' // sigma
            964 -> return 't' // tau
            965 -> return 'u' // upsilon
            966 -> return 'f' // phi
            967 -> return 'c' // chi
            968 -> return 'y' // psi
            969 -> return 'w' // omega
            else -> return ' '
        }
    }
}
