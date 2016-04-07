/*
 * $Id: b7672bd487ba8c2ae2c42c52346ed829dd03d8df $
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

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Utilities
import com.itextpdf.text.pdf.fonts.otf.Language
import com.itextpdf.text.pdf.languages.BanglaGlyphRepositioner
import com.itextpdf.text.pdf.languages.GlyphRepositioner
import com.itextpdf.text.pdf.languages.IndicCompositeCharacterComparator

import java.io.UnsupportedEncodingException
import java.util.*

/**
 * Each font in the document will have an instance of this class
 * where the characters used will be represented.

 * @author  Paulo Soares
 */
internal class FontDetails
/**
 * Each font used in a document has an instance of this class.
 * This class stores the characters used in the document and other
 * specifics unique to the current working document.
 * @param fontName the font name
 * *
 * @param indirectReference the indirect reference to the font
 * *
 * @param baseFont the BaseFont
 */
(
        /**
         * The font name that appears in the document body stream
         */
        /**
         * Gets the font name as it appears in the document body.
         * @return the font name
         */
        var fontName: PdfName,
        /**
         * The indirect reference to this font
         */
        /**
         * Gets the indirect reference to this font.
         * @return the indirect reference to this font
         */
        var indirectReference: PdfIndirectReference,
        /**
         * The font
         */
        /**
         * Gets the BaseFont of this font.
         * @return the BaseFont of this font
         */
        var baseFont: BaseFont) {
    /**
     * The font if it's an instance of TrueTypeFontUnicode
     */
    var ttu: TrueTypeFontUnicode
    /**
     * The font if it's an instance of CJKFont
     */
    var cjkFont: CJKFont
    /**
     * The array used with single byte encodings
     */
    var shortTag: ByteArray
    /**
     * The map used with double byte encodings. The key is Integer(glyph) and
     * the value is int[]{glyph, width, Unicode code}
     */
    var longTag: HashMap<Int, IntArray>
    /**
     * IntHashtable with CIDs of CJK glyphs that are used in the text.
     */
    var cjkTag: IntHashtable
    /**
     * The font type
     */
    var fontType: Int = 0
    /**
     * true if the font is symbolic
     */
    var symbolic: Boolean = false
    /**
     * Indicates if only a subset of the glyphs and widths for that particular
     * encoding should be included in the document.
     */
    /**
     * Indicates if all the glyphs and widths for that particular
     * encoding should be included in the document.
     * @return false to include all the glyphs and widths.
     */
    /**
     * Indicates if all the glyphs and widths for that particular
     * encoding should be included in the document. Set to false
     * to include all.
     * @param subset new value of property subset
     */
    var isSubset = true

    init {
        fontType = baseFont.fontType
        when (fontType) {
            BaseFont.FONT_TYPE_T1, BaseFont.FONT_TYPE_TT -> shortTag = ByteArray(256)
            BaseFont.FONT_TYPE_CJK -> {
                cjkTag = IntHashtable()
                cjkFont = baseFont as CJKFont
            }
            BaseFont.FONT_TYPE_TTUNI -> {
                longTag = HashMap<Int, IntArray>()
                ttu = baseFont as TrueTypeFontUnicode
                symbolic = baseFont.isFontSpecific
            }
        }
    }

    fun convertToBytesGid(gids: String): Array<Any> {
        if (fontType != BaseFont.FONT_TYPE_TTUNI)
            throw IllegalArgumentException("GID require TT Unicode")
        try {
            val sb = StringBuilder()
            var totalWidth = 0
            for (gid in gids.toCharArray()) {
                val width = ttu.getGlyphWidth(gid.toInt())
                totalWidth += width
                val vchar = ttu.GetCharFromGlyphId(gid.toInt())
                if (vchar != 0) {
                    sb.append(Utilities.convertFromUtf32(vchar))
                }
                val gl = Integer.valueOf(gid.toInt())
                if (!longTag.containsKey(gl))
                    longTag.put(gl, intArrayOf(gid.toInt(), width, vchar))
            }
            return arrayOf(gids.toByteArray(charset(CJKFont.CJK_ENCODING)), sb.toString(), Integer.valueOf(totalWidth))
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    /**
     * Converts the text into bytes to be placed in the document.
     * The conversion is done according to the font and the encoding and the characters
     * used are stored.
     * @param text the text to convert
     * *
     * @return the conversion
     */
    fun convertToBytes(text: String): ByteArray {
        var b: ByteArray? = null
        when (fontType) {
            BaseFont.FONT_TYPE_T3 -> return baseFont.convertToBytes(text)
            BaseFont.FONT_TYPE_T1, BaseFont.FONT_TYPE_TT -> {
                b = baseFont.convertToBytes(text)
                val len = b.size
                for (k in 0..len - 1)
                    shortTag[b[k] and 0xff] = 1
            }
            BaseFont.FONT_TYPE_CJK -> {
                val len = text.length
                if (cjkFont.isIdentity) {
                    for (k in 0..len - 1) {
                        cjkTag.put(text[k].toInt(), 0)
                    }
                } else {
                    var k = 0
                    while (k < len) {
                        val `val`: Int
                        if (Utilities.isSurrogatePair(text, k)) {
                            `val` = Utilities.convertToUtf32(text, k)
                            k++
                        } else {
                            `val` = text[k].toInt()
                        }
                        cjkTag.put(cjkFont.getCidCode(`val`), 0)
                        ++k
                    }
                }
                b = cjkFont.convertToBytes(text)
            }
            BaseFont.FONT_TYPE_DOCUMENT -> {
                b = baseFont.convertToBytes(text)
            }
            BaseFont.FONT_TYPE_TTUNI -> {
                try {
                    var len = text.length
                    var metrics: IntArray? = null
                    var glyph = CharArray(len)
                    var i = 0
                    if (symbolic) {
                        b = PdfEncodings.convertToBytes(text, "symboltt")
                        len = b!!.size
                        for (k in 0..len - 1) {
                            metrics = ttu.getMetricsTT(b[k] and 0xff)
                            if (metrics == null)
                                continue
                            longTag.put(Integer.valueOf(metrics[0]), intArrayOf(metrics[0], metrics[1], ttu.getUnicodeDifferences(b[k] and 0xff).toInt()))
                            glyph[i++] = metrics[0].toChar()
                        }
                    } else if (canApplyGlyphSubstitution()) {
                        return convertToBytesAfterGlyphSubstitution(text)
                    } else {
                        var k = 0
                        while (k < len) {
                            val `val`: Int
                            if (Utilities.isSurrogatePair(text, k)) {
                                `val` = Utilities.convertToUtf32(text, k)
                                k++
                            } else {
                                `val` = text[k].toInt()
                            }
                            metrics = ttu.getMetricsTT(`val`)
                            if (metrics == null) {
                                ++k
                                continue
                            }
                            val m0 = metrics[0]
                            val gl = Integer.valueOf(m0)
                            if (!longTag.containsKey(gl))
                                longTag.put(gl, intArrayOf(m0, metrics[1], `val`))
                            glyph[i++] = m0.toChar()
                            ++k
                        }
                    }
                    glyph = Utilities.copyOfRange(glyph, 0, i)
                    b = StringUtils.convertCharsToBytes(glyph)
                } catch (e: UnsupportedEncodingException) {
                    throw ExceptionConverter(e)
                }

            }
        }
        return b
    }

    private fun canApplyGlyphSubstitution(): Boolean {
        return fontType == BaseFont.FONT_TYPE_TTUNI && ttu.glyphSubstitutionMap != null
    }

    @Throws(UnsupportedEncodingException::class)
    private fun convertToBytesAfterGlyphSubstitution(text: String): ByteArray {

        if (!canApplyGlyphSubstitution()) {
            throw IllegalArgumentException("Make sure the font type if TTF Unicode and a valid GlyphSubstitutionTable exists!")
        }

        val glyphSubstitutionMap = ttu.glyphSubstitutionMap

        // generate a regex from the characters to be substituted

        // for Indic languages: push back the CompositeCharacters with smaller length
        val compositeCharacters = TreeSet(IndicCompositeCharacterComparator())
        compositeCharacters.addAll(glyphSubstitutionMap.keys)

        // convert the text to a list of Glyph, also take care of the substitution
        val tokenizer = ArrayBasedStringTokenizer(compositeCharacters.toArray<String>(arrayOfNulls<String>(0)))
        val tokens = tokenizer.tokenize(text)

        val glyphList = ArrayList<Glyph>(50)

        for (token in tokens) {

            // first check whether this is in the substitution map
            val subsGlyph = glyphSubstitutionMap[token]

            if (subsGlyph != null) {
                glyphList.add(subsGlyph)
            } else {
                // break up the string into individual characters
                for (c in token.toCharArray()) {
                    val metrics = ttu.getMetricsTT(c.toInt())
                    val glyphCode = metrics[0]
                    val glyphWidth = metrics[1]
                    glyphList.add(Glyph(glyphCode, glyphWidth, c.toString()))
                }
            }

        }

        val glyphRepositioner = glyphRepositioner

        glyphRepositioner?.repositionGlyphs(glyphList)

        val charEncodedGlyphCodes = CharArray(glyphList.size)

        // process each Glyph thus obtained
        for (i in glyphList.indices) {
            val glyph = glyphList[i]
            charEncodedGlyphCodes[i] = glyph.code.toChar()
            val glyphCode = Integer.valueOf(glyph.code)

            if (!longTag.containsKey(glyphCode)) {
                // FIXME: this is buggy as the 3rd arg. should be a String as a Glyph can represent more than 1 char
                longTag.put(glyphCode, intArrayOf(glyph.code, glyph.width, glyph.chars!![0].toInt()))
            }
        }

        return String(charEncodedGlyphCodes).toByteArray(charset(CJKFont.CJK_ENCODING))
    }

    private val glyphRepositioner: GlyphRepositioner?
        get() {
            val language = ttu.supportedLanguage ?: throw IllegalArgumentException("The supported language field cannot be null in " + ttu.javaClass.name)

            when (language) {
                Language.BENGALI -> return BanglaGlyphRepositioner(Collections.unmodifiableMap(ttu.cmap31), ttu.glyphSubstitutionMap)
                else -> return null
            }
        }

    /**
     * Writes the font definition to the document.
     * @param writer the PdfWriter of this document
     */
    fun writeFont(writer: PdfWriter) {
        try {
            when (fontType) {
                BaseFont.FONT_TYPE_T3 -> baseFont.writeFont(writer, indirectReference, null)
                BaseFont.FONT_TYPE_T1, BaseFont.FONT_TYPE_TT -> {
                    var firstChar: Int
                    var lastChar: Int
                    firstChar = 0
                    while (firstChar < 256) {
                        if (shortTag[firstChar].toInt() != 0)
                            break
                        ++firstChar
                    }
                    lastChar = 255
                    while (lastChar >= firstChar) {
                        if (shortTag[lastChar].toInt() != 0)
                            break
                        --lastChar
                    }
                    if (firstChar > 255) {
                        firstChar = 255
                        lastChar = 255
                    }
                    baseFont.writeFont(writer, indirectReference, arrayOf<Any>(Integer.valueOf(firstChar), Integer.valueOf(lastChar), shortTag, java.lang.Boolean.valueOf(isSubset)))
                }
                BaseFont.FONT_TYPE_CJK -> baseFont.writeFont(writer, indirectReference, arrayOf<Any>(cjkTag))
                BaseFont.FONT_TYPE_TTUNI -> baseFont.writeFont(writer, indirectReference, arrayOf<Any>(longTag, java.lang.Boolean.valueOf(isSubset)))
            }
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

}
