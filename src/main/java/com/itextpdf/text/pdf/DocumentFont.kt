/*
 * $Id: 196cb501f96b7281ba2836ee639a42fb85217e8b $
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
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Utilities
import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.pdf.fonts.cmaps.CMapParserEx
import com.itextpdf.text.pdf.fonts.cmaps.CMapToUnicode
import com.itextpdf.text.pdf.fonts.cmaps.CidLocationFromByte

import java.io.IOException
import java.util.HashMap

/**

 * @author  psoares
 */
open class DocumentFont : BaseFont {
    // code, [glyph, width]
    private val metrics = HashMap<Int, IntArray>()
    /** Gets the postscript font name.
     * @return the postscript font name
     */
    /**
     * Sets the font name that will appear in the pdf font dictionary.
     * It does nothing in this case as the font is already in the document.
     * @param name the new font name
     */
    override var postscriptFontName: String? = null
        set(name) {
        }
    private var refFont: PRIndirectReference? = null
    var fontDictionary: PdfDictionary? = null
        private set
    /**
     * Exposes the unicode - > CID map that is constructed from the font's encoding
     * @return the unicode to CID map
     * *
     * @since 2.1.7
     */
    internal val uni2Byte = IntHashtable()
    /**
     * Exposes the CID - > unicode map that is constructed from the font's encoding
     * @return the CID to unicode map
     * *
     * @since 5.4.0
     */
    internal val byte2Uni = IntHashtable()
    /**
     * Gets the difference map
     * @return the difference map
     * *
     * @since 5.0.5
     */
    internal var diffmap: IntHashtable? = null
        private set
    private var ascender = 800f
    private var capHeight = 700f
    private var descender = -200f
    private var italicAngle = 0f
    private var fontWeight = 0f
    private var llx = -50f
    private var lly = -200f
    private var urx = 100f
    private var ury = 900f
    protected var isType0 = false
    protected var defaultWidth = 1000
    private var hMetrics: IntHashtable? = null
    protected var cjkEncoding: String
    protected var uniMap: String

    private var cjkMirror: BaseFont? = null

    /** Creates a new instance of DocumentFont  */
    internal constructor(font: PdfDictionary) {
        this.refFont = null
        this.fontDictionary = font
        init()
    }

    /** Creates a new instance of DocumentFont  */
    internal constructor(refFont: PRIndirectReference) {
        this.refFont = refFont
        fontDictionary = PdfReader.getPdfObject(refFont) as PdfDictionary?
        init()
    }

    /** Creates a new instance of DocumentFont  */
    internal constructor(refFont: PRIndirectReference, drEncoding: PdfDictionary?) {
        this.refFont = refFont
        fontDictionary = PdfReader.getPdfObject(refFont) as PdfDictionary?
        if (fontDictionary!!.get(PdfName.ENCODING) == null && drEncoding != null) {
            for (key in drEncoding.keys) {
                fontDictionary!!.put(PdfName.ENCODING, drEncoding.get(key))
            }
        }
        init()
    }

    private fun init() {
        encoding = ""
        fontSpecific = false
        fontType = BaseFont.FONT_TYPE_DOCUMENT
        val baseFont = fontDictionary!!.getAsName(PdfName.BASEFONT)
        postscriptFontName = if (baseFont != null) PdfName.decodeName(baseFont.toString()) else "Unspecified Font Name"
        val subType = fontDictionary!!.getAsName(PdfName.SUBTYPE)
        if (PdfName.TYPE1 == subType || PdfName.TRUETYPE == subType)
            doType1TT()
        else if (PdfName.TYPE3 == subType) {
            // In case of a Type3 font, we just show the characters as is.
            // Note that this doesn't always make sense:
            // Type 3 fonts are user defined fonts where arbitrary characters are mapped to custom glyphs
            // For instance: the character a could be mapped to an image of a dog, the character b to an image of a cat
            // When parsing a document that shows a cat and a dog, you shouldn't expect seeing a cat and a dog. Instead you'll get b and a.
            fillEncoding(null)
            fillDiffMap(fontDictionary!!.getAsDict(PdfName.ENCODING), null)
            fillWidths()
        } else {
            val encodingName = fontDictionary!!.getAsName(PdfName.ENCODING)
            if (encodingName != null) {
                val enc = PdfName.decodeName(encodingName.toString())
                val ffontname = CJKFont.GetCompatibleFont(enc)
                if (ffontname != null) {
                    try {
                        cjkMirror = BaseFont.createFont(ffontname, enc, false)
                    } catch (e: Exception) {
                        throw ExceptionConverter(e)
                    }

                    cjkEncoding = enc
                    uniMap = (cjkMirror as CJKFont).uniMap
                }
                if (PdfName.TYPE0 == subType) {
                    isType0 = true
                    if (enc != "Identity-H" && cjkMirror != null) {
                        val df = PdfReader.getPdfObjectRelease(fontDictionary!!.get(PdfName.DESCENDANTFONTS)) as PdfArray?
                        val cidft = PdfReader.getPdfObjectRelease(df.getPdfObject(0)) as PdfDictionary?
                        val dwo = PdfReader.getPdfObjectRelease(cidft.get(PdfName.DW)) as PdfNumber?
                        if (dwo != null)
                            defaultWidth = dwo.intValue()
                        hMetrics = readWidths(PdfReader.getPdfObjectRelease(cidft.get(PdfName.W)) as PdfArray?)

                        val fontDesc = PdfReader.getPdfObjectRelease(cidft.get(PdfName.FONTDESCRIPTOR)) as PdfDictionary?
                        fillFontDesc(fontDesc)
                    } else {
                        processType0(fontDictionary)
                    }
                }
            }
        }
    }

    private fun processType0(font: PdfDictionary) {
        try {
            val toUniObject = PdfReader.getPdfObjectRelease(font.get(PdfName.TOUNICODE))
            val df = PdfReader.getPdfObjectRelease(font.get(PdfName.DESCENDANTFONTS)) as PdfArray?
            val cidft = PdfReader.getPdfObjectRelease(df.getPdfObject(0)) as PdfDictionary?
            val dwo = PdfReader.getPdfObjectRelease(cidft.get(PdfName.DW)) as PdfNumber?
            var dw = 1000
            if (dwo != null)
                dw = dwo.intValue()
            val widths = readWidths(PdfReader.getPdfObjectRelease(cidft.get(PdfName.W)) as PdfArray?)
            val fontDesc = PdfReader.getPdfObjectRelease(cidft.get(PdfName.FONTDESCRIPTOR)) as PdfDictionary?
            fillFontDesc(fontDesc)
            if (toUniObject is PRStream) {
                fillMetrics(PdfReader.getStreamBytes(toUniObject), widths, dw)
            } else if (PdfName("Identity-H") == toUniObject) {
                fillMetricsIdentity(widths, dw)
            }
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    private fun readWidths(ws: PdfArray?): IntHashtable {
        val hh = IntHashtable()
        if (ws == null)
            return hh
        var k = 0
        while (k < ws.size()) {
            var c1 = (PdfReader.getPdfObjectRelease(ws.getPdfObject(k)) as PdfNumber).intValue()
            val obj = PdfReader.getPdfObjectRelease(ws.getPdfObject(++k))
            if (obj.isArray) {
                val a2 = obj as PdfArray
                for (j in 0..a2.size() - 1) {
                    val c2 = (PdfReader.getPdfObjectRelease(a2.getPdfObject(j)) as PdfNumber).intValue()
                    hh.put(c1++, c2)
                }
            } else {
                val c2 = (obj as PdfNumber).intValue()
                val w = (PdfReader.getPdfObjectRelease(ws.getPdfObject(++k)) as PdfNumber).intValue()
                while (c1 <= c2) {
                    hh.put(c1, w)
                    ++c1
                }
            }
            ++k
        }
        return hh
    }

    private fun decodeString(ps: PdfString): String {
        if (ps.isHexWriting)
            return PdfEncodings.convertToString(ps.bytes, "UnicodeBigUnmarked")
        else
            return ps.toUnicodeString()
    }

    private fun fillMetricsIdentity(widths: IntHashtable, dw: Int) {
        for (i in 0..65535) {
            var w = dw
            if (widths.containsKey(i))
                w = widths.get(i)
            metrics.put(i, intArrayOf(i, w))
        }
    }

    private fun fillMetrics(touni: ByteArray, widths: IntHashtable, dw: Int) {
        try {
            val ps = PdfContentParser(PRTokeniser(RandomAccessFileOrArray(RandomAccessSourceFactory().createSource(touni))))
            var ob: PdfObject? = null
            var notFound = true
            var nestLevel = 0
            var maxExc = 50
            while (notFound || nestLevel > 0) {
                try {
                    ob = ps.readPRObject()
                } catch (ex: Exception) {
                    if (--maxExc < 0)
                        break
                    continue
                }

                if (ob == null)
                    break
                if (ob.type() == PdfContentParser.COMMAND_TYPE) {
                    if (ob.toString() == "begin") {
                        notFound = false
                        nestLevel++
                    } else if (ob.toString() == "end") {
                        nestLevel--
                    } else if (ob.toString() == "beginbfchar") {
                        while (true) {
                            val nx = ps.readPRObject()
                            if (nx.toString() == "endbfchar")
                                break
                            val cid = decodeString(nx as PdfString)
                            val uni = decodeString(ps.readPRObject() as PdfString?)
                            if (uni.length == 1) {
                                val cidc = cid[0].toInt()
                                val unic = uni[uni.length - 1].toInt()
                                var w = dw
                                if (widths.containsKey(cidc))
                                    w = widths.get(cidc)
                                metrics.put(Integer.valueOf(unic), intArrayOf(cidc, w))
                            }
                        }
                    } else if (ob.toString() == "beginbfrange") {
                        while (true) {
                            val nx = ps.readPRObject()
                            if (nx.toString() == "endbfrange")
                                break
                            val cid1 = decodeString(nx as PdfString)
                            val cid2 = decodeString(ps.readPRObject() as PdfString?)
                            var cid1c = cid1[0].toInt()
                            val cid2c = cid2[0].toInt()
                            val ob2 = ps.readPRObject()
                            if (ob2.isString) {
                                val uni = decodeString(ob2 as PdfString)
                                if (uni.length == 1) {
                                    var unic = uni[uni.length - 1].toInt()
                                    while (cid1c <= cid2c) {
                                        var w = dw
                                        if (widths.containsKey(cid1c))
                                            w = widths.get(cid1c)
                                        metrics.put(Integer.valueOf(unic), intArrayOf(cid1c, w))
                                        cid1c++
                                        unic++
                                    }
                                }
                            } else {
                                val a = ob2 as PdfArray
                                var j = 0
                                while (j < a.size()) {
                                    val uni = decodeString(a.getAsString(j))
                                    if (uni.length == 1) {
                                        val unic = uni[uni.length - 1].toInt()
                                        var w = dw
                                        if (widths.containsKey(cid1c))
                                            w = widths.get(cid1c)
                                        metrics.put(Integer.valueOf(unic), intArrayOf(cid1c, w))
                                    }
                                    ++j
                                    ++cid1c
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    private fun doType1TT() {
        var toUnicode: CMapToUnicode? = null
        var enc = PdfReader.getPdfObject(fontDictionary!!.get(PdfName.ENCODING))
        if (enc == null) {
            val baseFont = fontDictionary!!.getAsName(PdfName.BASEFONT)
            if (BaseFont.BuiltinFonts14.containsKey(postscriptFontName) && (PdfName.SYMBOL == baseFont || PdfName.ZAPFDINGBATS == baseFont)) {
                fillEncoding(baseFont)
            } else
                fillEncoding(null)
            try {
                toUnicode = processToUnicode()
                if (toUnicode != null) {
                    val rm = toUnicode.createReverseMapping()
                    for (kv in rm.entries) {
                        uni2Byte.put(kv.key.toInt(), kv.value.toInt())
                        byte2Uni.put(kv.value.toInt(), kv.key.toInt())
                    }
                }
            } catch (ex: Exception) {
                throw ExceptionConverter(ex)
            }

        } else {
            if (enc.isName)
                fillEncoding(enc as PdfName?)
            else if (enc.isDictionary) {
                val encDic = enc as PdfDictionary?
                enc = PdfReader.getPdfObject(encDic.get(PdfName.BASEENCODING))
                if (enc == null)
                    fillEncoding(null)
                else
                    fillEncoding(enc as PdfName?)
                fillDiffMap(encDic, toUnicode)
            }
        }

        if (BaseFont.BuiltinFonts14.containsKey(postscriptFontName)) {
            val bf: BaseFont
            try {
                bf = BaseFont.createFont(postscriptFontName, BaseFont.WINANSI, false)
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

            var e = uni2Byte.toOrderedKeys()
            for (k in e.indices) {
                val n = uni2Byte.get(e[k])
                widths[n] = bf.getRawWidth(n, GlyphList.unicodeToName(e[k]))
            }
            if (diffmap != null) {
                //widths for diffmap must override existing ones
                e = diffmap!!.toOrderedKeys()
                for (k in e.indices) {
                    val n = diffmap!!.get(e[k])
                    widths[n] = bf.getRawWidth(n, GlyphList.unicodeToName(e[k]))
                }
                diffmap = null
            }
            ascender = bf.getFontDescriptor(BaseFont.ASCENT, 1000f)
            capHeight = bf.getFontDescriptor(BaseFont.CAPHEIGHT, 1000f)
            descender = bf.getFontDescriptor(BaseFont.DESCENT, 1000f)
            italicAngle = bf.getFontDescriptor(BaseFont.ITALICANGLE, 1000f)
            fontWeight = bf.getFontDescriptor(BaseFont.FONT_WEIGHT, 1000f)
            llx = bf.getFontDescriptor(BaseFont.BBOXLLX, 1000f)
            lly = bf.getFontDescriptor(BaseFont.BBOXLLY, 1000f)
            urx = bf.getFontDescriptor(BaseFont.BBOXURX, 1000f)
            ury = bf.getFontDescriptor(BaseFont.BBOXURY, 1000f)
        }
        fillWidths()
        fillFontDesc(fontDictionary!!.getAsDict(PdfName.FONTDESCRIPTOR))
    }

    private fun fillWidths() {
        val newWidths = fontDictionary!!.getAsArray(PdfName.WIDTHS)
        val first = fontDictionary!!.getAsNumber(PdfName.FIRSTCHAR)
        val last = fontDictionary!!.getAsNumber(PdfName.LASTCHAR)
        if (first != null && last != null && newWidths != null) {
            val f = first.intValue()
            val nSize = f + newWidths.size()
            if (widths.size < nSize) {
                val tmp = IntArray(nSize)
                System.arraycopy(widths, 0, tmp, 0, f)
                widths = tmp
            }
            for (k in 0..newWidths.size() - 1) {
                widths[f + k] = newWidths.getAsNumber(k).intValue()
            }
        }
    }

    private fun fillDiffMap(encDic: PdfDictionary, toUnicode: CMapToUnicode?) {
        var toUnicode = toUnicode
        val diffs = encDic.getAsArray(PdfName.DIFFERENCES)
        if (diffs != null) {
            diffmap = IntHashtable()
            var currentNumber = 0
            for (k in 0..diffs.size() - 1) {
                val obj = diffs.getPdfObject(k)
                if (obj.isNumber)
                    currentNumber = (obj as PdfNumber).intValue()
                else {
                    val c = GlyphList.nameToUnicode(PdfName.decodeName((obj as PdfName).toString()))
                    if (c != null && c.size > 0) {
                        uni2Byte.put(c[0], currentNumber)
                        byte2Uni.put(currentNumber, c[0])
                        diffmap!!.put(c[0], currentNumber)
                    } else {
                        if (toUnicode == null) {
                            toUnicode = processToUnicode()
                            if (toUnicode == null) {
                                toUnicode = CMapToUnicode()
                            }
                        }
                        val unicode = toUnicode.lookup(byteArrayOf(currentNumber.toByte()), 0, 1)
                        if (unicode != null && unicode.length == 1) {
                            this.uni2Byte.put(unicode[0].toInt(), currentNumber)
                            this.byte2Uni.put(currentNumber, unicode[0].toInt())
                            this.diffmap!!.put(unicode[0].toInt(), currentNumber)
                        }
                    }
                    ++currentNumber
                }
            }
        }
    }

    private fun processToUnicode(): CMapToUnicode {
        var cmapRet: CMapToUnicode? = null
        val toUni = PdfReader.getPdfObjectRelease(this.fontDictionary!!.get(PdfName.TOUNICODE))
        if (toUni is PRStream) {
            try {
                val touni = PdfReader.getStreamBytes(toUni)
                val lb = CidLocationFromByte(touni)
                cmapRet = CMapToUnicode()
                CMapParserEx.parseCid("", cmapRet, lb)
            } catch (e: Exception) {
                cmapRet = null
            }

        }
        return cmapRet
    }

    private fun fillFontDesc(fontDesc: PdfDictionary?) {
        if (fontDesc == null)
            return
        var v: PdfNumber? = fontDesc.getAsNumber(PdfName.ASCENT)
        if (v != null)
            ascender = v.floatValue()
        v = fontDesc.getAsNumber(PdfName.CAPHEIGHT)
        if (v != null)
            capHeight = v.floatValue()
        v = fontDesc.getAsNumber(PdfName.DESCENT)
        if (v != null)
            descender = v.floatValue()
        v = fontDesc.getAsNumber(PdfName.ITALICANGLE)
        if (v != null)
            italicAngle = v.floatValue()
        v = fontDesc.getAsNumber(PdfName.FONTWEIGHT)
        if (v != null) {
            fontWeight = v.floatValue()
        }
        val bbox = fontDesc.getAsArray(PdfName.FONTBBOX)
        if (bbox != null) {
            llx = bbox.getAsNumber(0).floatValue()
            lly = bbox.getAsNumber(1).floatValue()
            urx = bbox.getAsNumber(2).floatValue()
            ury = bbox.getAsNumber(3).floatValue()
            if (llx > urx) {
                val t = llx
                llx = urx
                urx = t
            }
            if (lly > ury) {
                val t = lly
                lly = ury
                ury = t
            }
        }
        val maxAscent = Math.max(ury, ascender)
        val minDescent = Math.min(lly, descender)
        ascender = maxAscent * 1000 / (maxAscent - minDescent)
        descender = minDescent * 1000 / (maxAscent - minDescent)
    }

    private fun fillEncoding(encoding: PdfName?) {
        if (encoding == null && isSymbolic) {
            for (k in 0..255) {
                uni2Byte.put(k, k)
                byte2Uni.put(k, k)
            }
        } else if (PdfName.MAC_ROMAN_ENCODING == encoding || PdfName.WIN_ANSI_ENCODING == encoding
                || PdfName.SYMBOL == encoding || PdfName.ZAPFDINGBATS == encoding) {
            val b = ByteArray(256)
            for (k in 0..255)
                b[k] = k.toByte()
            var enc = BaseFont.WINANSI
            if (PdfName.MAC_ROMAN_ENCODING == encoding)
                enc = BaseFont.MACROMAN
            else if (PdfName.SYMBOL == encoding)
                enc = BaseFont.SYMBOL
            else if (PdfName.ZAPFDINGBATS == encoding)
                enc = BaseFont.ZAPFDINGBATS
            val cv = PdfEncodings.convertToString(b, enc)
            val arr = cv.toCharArray()
            for (k in 0..255) {
                uni2Byte.put(arr[k].toInt(), k)
                byte2Uni.put(k, arr[k].toInt())
            }
            this.encoding = enc
        } else {
            for (k in 0..255) {
                uni2Byte.put(stdEnc[k], k)
                byte2Uni.put(k, stdEnc[k])
            }
        }
    }

    /** Gets the family name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the family name of the font
     */
    override val familyFontName: Array<Array<String>>
        get() = fullFontName

    /** Gets the font parameter identified by key. Valid values
     * for key are ASCENT, CAPHEIGHT, DESCENT,
     * ITALICANGLE, BBOXLLX, BBOXLLY, BBOXURX
     * and BBOXURY.
     * @param key the parameter to be extracted
     * *
     * @param fontSize the font size in points
     * *
     * @return the parameter in points
     */
    override fun getFontDescriptor(key: Int, fontSize: Float): Float {
        if (cjkMirror != null)
            return cjkMirror!!.getFontDescriptor(key, fontSize)
        when (key) {
            BaseFont.AWT_ASCENT, BaseFont.ASCENT -> return ascender * fontSize / 1000
            BaseFont.CAPHEIGHT -> return capHeight * fontSize / 1000
            BaseFont.AWT_DESCENT, BaseFont.DESCENT -> return descender * fontSize / 1000
            BaseFont.ITALICANGLE -> return italicAngle
            BaseFont.BBOXLLX -> return llx * fontSize / 1000
            BaseFont.BBOXLLY -> return lly * fontSize / 1000
            BaseFont.BBOXURX -> return urx * fontSize / 1000
            BaseFont.BBOXURY -> return ury * fontSize / 1000
            BaseFont.AWT_LEADING -> return 0f
            BaseFont.AWT_MAXADVANCE -> return (urx - llx) * fontSize / 1000
            BaseFont.FONT_WEIGHT -> return fontWeight * fontSize / 1000
        }
        return 0f
    }

    /** Gets the full name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the full name of the font
     */
    override val fullFontName: Array<Array<String>>
        get() = arrayOf(arrayOf<String>("", "", "", postscriptFontName))

    /** Gets all the entries of the names-table. If it is a True Type font
     * each array element will have {Name ID, Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"4", "", "", "",
     * font name}.
     * @return the full name of the font
     * *
     * @since 2.0.8
     */
    override val allNameEntries: Array<Array<String>>
        get() = arrayOf(arrayOf<String>("4", "", "", "", postscriptFontName))

    /** Gets the kerning between two Unicode chars.
     * @param char1 the first char
     * *
     * @param char2 the second char
     * *
     * @return the kerning to be applied
     */
    override fun getKerning(char1: Int, char2: Int): Int {
        return 0
    }

    /** Gets the width from the font according to the Unicode char c
     * or the name. If the name is null it's a symbolic font.
     * @param c the unicode char
     * *
     * @param name the glyph name
     * *
     * @return the width of the char
     */
    internal fun getRawWidth(c: Int, name: String): Int {
        return 0
    }

    /** Checks if the font has any kerning pairs.
     * @return true if the font has any kerning pairs
     */
    override fun hasKernPairs(): Boolean {
        return false
    }

    /** Outputs to the writer the font dictionaries and streams.
     * @param writer the writer for this document
     * *
     * @param ref the font indirect reference
     * *
     * @param params several parameters that depend on the font type
     * *
     * @throws IOException on error
     * *
     * @throws DocumentException error in generating the object
     */
    @Throws(DocumentException::class, IOException::class)
    internal fun writeFont(writer: PdfWriter, ref: PdfIndirectReference, params: Array<Any>) {
    }

    /**
     * Always returns null.
     * @return    null
     * *
     * @since    2.1.3
     */
    val fullFontStream: PdfStream?
        get() = null

    /**
     * Gets the width of a char in normalized 1000 units.
     * @param char1 the unicode char to get the width of
     * *
     * @return the width in normalized 1000 units
     */
    override fun getWidth(char1: Int): Int {
        if (isType0) {
            if (hMetrics != null && cjkMirror != null && !cjkMirror!!.isVertical) {
                val c = cjkMirror!!.getCidCode(char1)
                val v = hMetrics!!.get(c)
                if (v > 0)
                    return v
                else
                    return defaultWidth
            } else {
                val ws = metrics[Integer.valueOf(char1)]
                if (ws != null)
                    return ws[1]
                else
                    return 0
            }
        }
        if (cjkMirror != null)
            return cjkMirror!!.getWidth(char1)
        return super.getWidth(char1)
    }

    override fun getWidth(text: String): Int {
        if (isType0) {
            var total = 0
            if (hMetrics != null && cjkMirror != null && !cjkMirror!!.isVertical) {
                if ((cjkMirror as CJKFont).isIdentity) {
                    for (k in 0..text.length - 1) {
                        total += getWidth(text[k].toInt())
                    }
                } else {
                    var k = 0
                    while (k < text.length) {
                        val `val`: Int
                        if (Utilities.isSurrogatePair(text, k)) {
                            `val` = Utilities.convertToUtf32(text, k)
                            k++
                        } else {
                            `val` = text[k].toInt()
                        }
                        total += getWidth(`val`)
                        ++k
                    }
                }
            } else {
                val chars = text.toCharArray()
                val len = chars.size
                for (k in 0..len - 1) {
                    val ws = metrics[Integer.valueOf(chars[k].toInt())]
                    if (ws != null)
                        total += ws[1]
                }
            }
            return total
        }
        if (cjkMirror != null)
            return cjkMirror!!.getWidth(text)
        return super.getWidth(text)
    }

    override fun convertToBytes(text: String): ByteArray {
        if (cjkMirror != null)
            return cjkMirror!!.convertToBytes(text)
        else if (isType0) {
            val chars = text.toCharArray()
            val len = chars.size
            val b = ByteArray(len * 2)
            var bptr = 0
            for (k in 0..len - 1) {
                val ws = metrics[Integer.valueOf(chars[k].toInt())]
                if (ws != null) {
                    val g = ws[0]
                    b[bptr++] = (g / 256).toByte()
                    b[bptr++] = g.toByte()
                }
            }
            if (bptr == b.size)
                return b
            else {
                val nb = ByteArray(bptr)
                System.arraycopy(b, 0, nb, 0, bptr)
                return nb
            }
        } else {
            val cc = text.toCharArray()
            val b = ByteArray(cc.size)
            var ptr = 0
            for (k in cc.indices) {
                if (uni2Byte.containsKey(cc[k].toInt()))
                    b[ptr++] = uni2Byte.get(cc[k].toInt()).toByte()
            }
            if (ptr == b.size)
                return b
            else {
                val b2 = ByteArray(ptr)
                System.arraycopy(b, 0, b2, 0, ptr)
                return b2
            }
        }
    }

    internal fun convertToBytes(char1: Int): ByteArray {
        if (cjkMirror != null)
            return cjkMirror!!.convertToBytes(char1)
        else if (isType0) {
            val ws = metrics[Integer.valueOf(char1)]
            if (ws != null) {
                val g = ws[0]
                return byteArrayOf((g / 256).toByte(), g.toByte())
            } else
                return ByteArray(0)
        } else {
            if (uni2Byte.containsKey(char1))
                return byteArrayOf(uni2Byte.get(char1).toByte())
            else
                return ByteArray(0)
        }
    }

    internal val indirectReference: PdfIndirectReference
        get() {
            if (refFont == null)
                throw IllegalArgumentException("Font reuse not allowed with direct font objects.")
            return refFont
        }

    override fun charExists(c: Int): Boolean {
        if (cjkMirror != null)
            return cjkMirror!!.charExists(c)
        else if (isType0) {
            return metrics.containsKey(Integer.valueOf(c))
        } else
            return super.charExists(c)
    }

    override val fontMatrix: DoubleArray
        get() {
            if (fontDictionary!!.getAsArray(PdfName.FONTMATRIX) != null)
                return fontDictionary!!.getAsArray(PdfName.FONTMATRIX).asDoubleArray()
            else
                return BaseFont.DEFAULT_FONT_MATRIX

        }

    override fun setKerning(char1: Int, char2: Int, kern: Int): Boolean {
        return false
    }

    override fun getCharBBox(c: Int): IntArray? {
        return null
    }

    override fun getRawCharBBox(c: Int, name: String?): IntArray {
        return null
    }

    override var isVertical: Boolean
        get() {
            if (cjkMirror != null)
                return cjkMirror!!.isVertical
            else
                return super.isVertical
        }
        set(value: Boolean) {
            super.isVertical = value
        }

    internal val isSymbolic: Boolean
        get() {
            val fontDescriptor = fontDictionary!!.getAsDict(PdfName.FONTDESCRIPTOR) ?: return false
            val flags = fontDescriptor.getAsNumber(PdfName.FLAGS) ?: return false
            return flags.intValue() and 0x04 != 0
        }

    companion object {

        private val stdEnc = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 33, 34, 35, 36, 37, 38, 8217, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 8216, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 161, 162, 163, 8260, 165, 402, 167, 164, 39, 8220, 171, 8249, 8250, 64257, 64258, 0, 8211, 8224, 8225, 183, 0, 182, 8226, 8218, 8222, 8221, 187, 8230, 8240, 0, 191, 0, 96, 180, 710, 732, 175, 728, 729, 168, 0, 730, 184, 0, 733, 731, 711, 8212, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 198, 0, 170, 0, 0, 0, 0, 321, 216, 338, 186, 0, 0, 0, 0, 0, 230, 0, 0, 0, 305, 0, 0, 322, 248, 339, 223, 0, 0, 0, 0)
    }
}
