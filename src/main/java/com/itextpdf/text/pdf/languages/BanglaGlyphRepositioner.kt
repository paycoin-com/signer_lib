/*
 * $Id: ee792a779b42435bca31b06258cd37daef60cd4c $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, et al.
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
package com.itextpdf.text.pdf.languages

import java.util.Arrays

import com.itextpdf.text.pdf.Glyph

/**

 * @author [Palash Ray](mailto:paawak@gmail.com)
 */
class BanglaGlyphRepositioner(private val cmap31: Map<Int, IntArray>, private val glyphSubstitutionMap: Map<String, Glyph>) : IndicGlyphRepositioner() {

    override fun repositionGlyphs(glyphList: MutableList<Glyph>) {

        for (i in glyphList.indices) {
            val glyph = glyphList[i]

            if (glyph.chars == "\u09CB") {
                handleOKaarAndOUKaar(i, glyphList, '\u09C7', '\u09BE')
            } else if (glyph.chars == "\u09CC") {
                handleOKaarAndOUKaar(i, glyphList, '\u09C7', '\u09D7')
            }
        }

        super.repositionGlyphs(glyphList)
    }

    override val charactersToBeShiftedLeftByOnePosition: List<String>
        get() = Arrays.asList(*CHARCTERS_TO_BE_SHIFTED_LEFT_BY_1)

    /**
     * This is a dirty hack to display O-Kar (\u09cb) and Ou-Kar (\u09cc). Since this spans before and after
     * a Byanjan Borno like Ka (\u0995), the O-kar is split into two characters: the E-Kar (\u09C7) and the A-Kar (\u09BE).
     * Similar the Ou-Kar is split into two characters: the E-Kar (\u09C7) and the char (\u09D7).

     */
    private fun handleOKaarAndOUKaar(currentIndex: Int, glyphList: MutableList<Glyph>, first: Char, second: Char) {
        val g1 = getGlyph(first)
        val g2 = getGlyph(second)
        glyphList[currentIndex] = g1
        glyphList.add(currentIndex + 1, g2)
    }

    private fun getGlyph(c: Char): Glyph {

        val glyph = glyphSubstitutionMap[c.toString()]

        if (glyph != null) {
            return glyph
        }

        val metrics = cmap31[Integer.valueOf(c.toInt())]
        val glyphCode = metrics[0]
        val glyphWidth = metrics[1]
        return Glyph(glyphCode, glyphWidth, c.toString())
    }

    companion object {

        private val CHARCTERS_TO_BE_SHIFTED_LEFT_BY_1 = arrayOf("\u09BF", "\u09C7", "\u09C8")
    }

}
