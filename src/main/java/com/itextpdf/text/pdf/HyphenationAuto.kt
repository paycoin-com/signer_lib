/*
 * $Id: e8e0067d7710fdc75ef952644f5d4a43fdbf14a7 $
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

import com.itextpdf.text.pdf.hyphenation.Hyphenation
import com.itextpdf.text.pdf.hyphenation.Hyphenator

/** Hyphenates words automatically accordingly to the language and country.
 * The hyphenator engine was taken from FOP and uses the TEX patterns. If a language
 * is not provided and a TEX pattern for it exists, it can be easily adapted.

 * @author Paulo Soares
 */
class HyphenationAuto
/** Creates a new hyphenation instance usable in Chunk.
 * @param lang the language ("en" for English, for example)
 * *
 * @param country the country ("GB" for Great-Britain or "none" for no country, for example)
 * *
 * @param leftMin the minimum number of letters before the hyphen
 * *
 * @param rightMin the minimum number of letters after the hyphen
 */
(lang: String, country: String, leftMin: Int, rightMin: Int) : HyphenationEvent {

    /** The hyphenator engine.
     */
    protected var hyphenator: Hyphenator
    /** The second part of the hyphenated word.
     */
    /** Gets the second part of the hyphenated word. Must be called
     * after getHyphenatedWordPre().
     * @return the second part of the hyphenated word
     */
    override var hyphenatedWordPost: String
        protected set(value: String) {
            super.hyphenatedWordPost = value
        }

    init {
        hyphenator = Hyphenator(lang, country, leftMin, rightMin)
    }

    /** Gets the hyphen symbol.
     * @return the hyphen symbol
     */
    override val hyphenSymbol: String
        get() = "-"

    /** Hyphenates a word and returns the first part of it. To get
     * the second part of the hyphenated word call getHyphenatedWordPost().
     * @param word the word to hyphenate
     * *
     * @param font the font used by this word
     * *
     * @param fontSize the font size used by this word
     * *
     * @param remainingWidth the width available to fit this word in
     * *
     * @return the first part of the hyphenated word including
     * * the hyphen symbol, if any
     */
    override fun getHyphenatedWordPre(word: String, font: BaseFont, fontSize: Float, remainingWidth: Float): String {
        hyphenatedWordPost = word
        val hyphen = hyphenSymbol
        val hyphenWidth = font.getWidthPoint(hyphen, fontSize)
        if (hyphenWidth > remainingWidth)
            return ""
        val hyphenation = hyphenator.hyphenate(word) ?: return ""
        val len = hyphenation.length()
        var k: Int
        k = 0
        while (k < len) {
            if (font.getWidthPoint(hyphenation.getPreHyphenText(k), fontSize) + hyphenWidth > remainingWidth)
                break
            ++k
        }
        --k
        if (k < 0)
            return ""
        hyphenatedWordPost = hyphenation.getPostHyphenText(k)
        return hyphenation.getPreHyphenText(k) + hyphen
    }

}
