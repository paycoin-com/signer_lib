/*
 * $Id: e6f61948e5f6fcbda1ac91aaaac5d611b10ef23a $
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

import com.itextpdf.text.DocumentException
import com.itextpdf.text.error_messages.MessageLocalization

import java.util.HashMap

/**
 * A class to support Type3 fonts.
 */
class Type3Font
/**
 * Creates a Type3 font. This implementation assumes that the /FontMatrix is
 * [0.001 0 0 0.001 0 0] or a 1000-unit glyph coordinate system.
 *
 *
 * An example:
 *
 *
 *
 * Document document = new Document(PageSize.A4);
 * PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("type3.pdf"));
 * document.open();
 * Type3Font t3 = new Type3Font(writer, false);
 * PdfContentByte g = t3.defineGlyph('a', 1000, 0, 0, 750, 750);
 * g.rectangle(0, 0, 750, 750);
 * g.fill();
 * g = t3.defineGlyph('b', 1000, 0, 0, 750, 750);
 * g.moveTo(0, 0);
 * g.lineTo(375, 750);
 * g.lineTo(750, 0);
 * g.fill();
 * Font f = new Font(t3, 12);
 * document.add(new Paragraph("ababab", f));
 * document.close();
 *
 * @param writer the writer
 * *
 * @param colorized if true the font may specify color, if false no color commands are allowed
 * * and only images as masks can be used
 */
(private val writer: PdfWriter, private val colorized: Boolean) : BaseFont() {

    private val usedSlot: BooleanArray
    private val widths3 = IntHashtable()
    private val char2glyph = HashMap<Int, Type3Glyph>()
    private var llx = java.lang.Float.NaN
    private var lly: Float = 0.toFloat()
    private var urx: Float = 0.toFloat()
    private var ury: Float = 0.toFloat()
    private val pageResources = PageResources()

    /**
     * Creates a Type3 font.
     * @param writer the writer
     * *
     * @param chars an array of chars corresponding to the glyphs used (not used, present for compatibility only)
     * *
     * @param colorized if true the font may specify color, if false no color commands are allowed
     * * and only images as masks can be used
     */
    constructor(writer: PdfWriter, chars: CharArray, colorized: Boolean) : this(writer, colorized) {
    }

    init {
        fontType = BaseFont.FONT_TYPE_T3
        usedSlot = BooleanArray(256)
    }

    /**
     * Defines a glyph. If the character was already defined it will return the same content
     * @param c the character to match this glyph.
     * *
     * @param wx the advance this character will have
     * *
     * @param llx the X lower left corner of the glyph bounding box. If the colorize option is
     * * true the value is ignored
     * *
     * @param lly the Y lower left corner of the glyph bounding box. If the colorize option is
     * * true the value is ignored
     * *
     * @param urx the X upper right corner of the glyph bounding box. If the colorize option is
     * * true the value is ignored
     * *
     * @param ury the Y upper right corner of the glyph bounding box. If the colorize option is
     * * true the value is ignored
     * *
     * @return a content where the glyph can be defined
     */
    fun defineGlyph(c: Char, wx: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfContentByte {
        if (c.toInt() == 0 || c.toInt() > 255)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.char.1.doesn.t.belong.in.this.type3.font", c.toInt()))
        usedSlot[c] = true
        val ck = Integer.valueOf(c.toInt())
        var glyph: Type3Glyph? = char2glyph[ck]
        if (glyph != null)
            return glyph
        widths3.put(c.toInt(), wx.toInt())
        if (!colorized) {
            if (java.lang.Float.isNaN(this.llx)) {
                this.llx = llx
                this.lly = lly
                this.urx = urx
                this.ury = ury
            } else {
                this.llx = Math.min(this.llx, llx)
                this.lly = Math.min(this.lly, lly)
                this.urx = Math.max(this.urx, urx)
                this.ury = Math.max(this.ury, ury)
            }
        }
        glyph = Type3Glyph(writer, pageResources, wx, llx, lly, urx, ury, colorized)
        char2glyph.put(ck, glyph)
        return glyph
    }

    override val familyFontName: Array<Array<String>>
        get() = fullFontName

    override fun getFontDescriptor(key: Int, fontSize: Float): Float {
        return 0f
    }

    override val fullFontName: Array<Array<String>>
        get() = arrayOf(arrayOf("", "", "", ""))

    /**
     * @since 2.0.8
     */
    override val allNameEntries: Array<Array<String>>
        get() = arrayOf(arrayOf("4", "", "", "", ""))

    override fun getKerning(char1: Int, char2: Int): Int {
        return 0
    }

    override var postscriptFontName: String
        get() = ""
        set(name) {
        }

    override fun getRawCharBBox(c: Int, name: String?): IntArray {
        return null
    }

    internal fun getRawWidth(c: Int, name: String): Int {
        return 0
    }

    override fun hasKernPairs(): Boolean {
        return false
    }

    override fun setKerning(char1: Int, char2: Int, kern: Int): Boolean {
        return false
    }

    @Throws(com.itextpdf.text.DocumentException::class, java.io.IOException::class)
    internal fun writeFont(writer: PdfWriter, ref: PdfIndirectReference, params: Array<Any>) {
        if (this.writer !== writer)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("type3.font.used.with.the.wrong.pdfwriter"))

        // Get first & lastchar ...
        var firstChar = 0
        while (firstChar < usedSlot.size && !usedSlot[firstChar]) firstChar++

        if (firstChar == usedSlot.size) {
            throw DocumentException(MessageLocalization.getComposedMessage("no.glyphs.defined.for.type3.font"))
        }
        var lastChar = usedSlot.size - 1
        while (lastChar >= firstChar && !usedSlot[lastChar]) lastChar--

        val widths = IntArray(lastChar - firstChar + 1)
        val invOrd = IntArray(lastChar - firstChar + 1)

        var invOrdIndx = 0
        var w = 0
        var u = firstChar
        while (u <= lastChar) {
            if (usedSlot[u]) {
                invOrd[invOrdIndx++] = u
                widths[w] = widths3.get(u)
            }
            u++
            w++
        }
        val diffs = PdfArray()
        val charprocs = PdfDictionary()
        var last = -1
        for (k in 0..invOrdIndx - 1) {
            val c = invOrd[k]
            if (c > last) {
                last = c
                diffs.add(PdfNumber(last))
            }
            ++last
            val c2 = invOrd[k]
            var s: String? = GlyphList.unicodeToName(c2)
            if (s == null)
                s = "a" + c2
            val n = PdfName(s)
            diffs.add(n)
            val glyph = char2glyph[Integer.valueOf(c2)]
            val stream = PdfStream(glyph.toPdf(null))
            stream.flateCompress(compressionLevel.toInt())
            val refp = writer.addToBody(stream).indirectReference
            charprocs.put(n, refp)
        }
        val font = PdfDictionary(PdfName.FONT)
        font.put(PdfName.SUBTYPE, PdfName.TYPE3)
        if (colorized)
            font.put(PdfName.FONTBBOX, PdfRectangle(0f, 0f, 0f, 0f))
        else
            font.put(PdfName.FONTBBOX, PdfRectangle(llx, lly, urx, ury))
        font.put(PdfName.FONTMATRIX, PdfArray(floatArrayOf(0.001f, 0f, 0f, 0.001f, 0f, 0f)))
        font.put(PdfName.CHARPROCS, writer.addToBody(charprocs).indirectReference)
        val encoding = PdfDictionary()
        encoding.put(PdfName.DIFFERENCES, diffs)
        font.put(PdfName.ENCODING, writer.addToBody(encoding).indirectReference)
        font.put(PdfName.FIRSTCHAR, PdfNumber(firstChar))
        font.put(PdfName.LASTCHAR, PdfNumber(lastChar))
        font.put(PdfName.WIDTHS, writer.addToBody(PdfArray(widths)).indirectReference)
        if (pageResources.hasResources())
            font.put(PdfName.RESOURCES, writer.addToBody(pageResources.resources).indirectReference)
        writer.addToBody(font, ref)
    }

    /**
     * Always returns null, because you can't get the FontStream of a Type3 font.
     * @return    null
     * *
     * @since    2.1.3
     */
    val fullFontStream: PdfStream?
        get() = null


    override fun convertToBytes(text: String): ByteArray {
        val cc = text.toCharArray()
        val b = ByteArray(cc.size)
        var p = 0
        for (k in cc.indices) {
            val c = cc[k]
            if (charExists(c.toInt()))
                b[p++] = c.toByte()
        }
        if (b.size == p)
            return b
        val b2 = ByteArray(p)
        System.arraycopy(b, 0, b2, 0, p)
        return b2
    }

    internal fun convertToBytes(char1: Int): ByteArray {
        if (charExists(char1))
            return byteArrayOf(char1.toByte())
        else
            return ByteArray(0)
    }

    override fun getWidth(char1: Int): Int {
        if (!widths3.containsKey(char1))
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.char.1.is.not.defined.in.a.type3.font", char1))
        return widths3.get(char1)
    }

    override fun getWidth(text: String): Int {
        val c = text.toCharArray()
        var total = 0
        for (k in c.indices)
            total += getWidth(c[k].toInt())
        return total
    }

    override fun getCharBBox(c: Int): IntArray? {
        return null
    }

    override fun charExists(c: Int): Boolean {
        if (c > 0 && c < 256) {
            return usedSlot[c]
        } else {
            return false
        }
    }

    override fun setCharAdvance(c: Int, advance: Int): Boolean {
        return false
    }

}
