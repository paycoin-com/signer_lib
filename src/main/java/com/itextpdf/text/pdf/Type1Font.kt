/*
 * $Id: e6ac8771b3ceb5b55cee858da8c8a74ca0e6c97d $
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

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.StreamUtil
import com.itextpdf.text.pdf.fonts.FontsResourceAnchor

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.HashMap
import java.util.StringTokenizer

/** Reads a Type1 font

 * @author Paulo Soares
 */
internal class Type1Font
/** Creates a new Type1 font.
 * @param ttfAfm the AFM file if the input is made with a byte array
 * *
 * @param pfb the PFB file if the input is made with a byte array
 * *
 * @param afmFile the name of one of the 14 built-in fonts or the location of an AFM file. The file must end in '.afm'
 * *
 * @param enc the encoding to be applied to this font
 * *
 * @param emb true if the font is to be embedded in the PDF
 * *
 * @throws DocumentException the AFM file is invalid
 * *
 * @throws IOException the AFM file could not be read
 * *
 * @since    2.1.5
 */
@Throws(DocumentException::class, IOException::class)
constructor(
        /** The file in use.
         */
        private val fileName: String, enc: String, emb: Boolean, ttfAfm: ByteArray?, pfb: ByteArray?, forceRead: Boolean) : BaseFont() {

    /** The PFB file if the input was made with a byte array.
     */
    protected var pfb: ByteArray? = null
    /** The Postscript font name.
     */
    /** Gets the postscript font name.
     * @return the postscript font name
     */
    /**
     * Sets the font name that will appear in the pdf font dictionary.
     * Use with care as it can easily make a font unreadable if not embedded.
     * @param name the new font name
     */
    override var postscriptFontName: String? = null
    /** The full name of the font.
     */
    private var FullName: String? = null
    /** The family name of the font.
     */
    private var FamilyName: String? = null
    /** The weight of the font: normal, bold, etc.
     */
    private var Weight = ""
    /** The italic angle of the font, usually 0.0 or negative.
     */
    private var ItalicAngle = 0.0f
    /** true if all the characters have the same
     * width.
     */
    private var IsFixedPitch = false
    /** The character set of the font.
     */
    private var CharacterSet: String? = null
    /** The llx of the FontBox.
     */
    private var llx = -50
    /** The lly of the FontBox.
     */
    private var lly = -200
    /** The lurx of the FontBox.
     */
    private var urx = 1000
    /** The ury of the FontBox.
     */
    private var ury = 900
    /** The underline position.
     */
    private var UnderlinePosition = -100
    /** The underline thickness.
     */
    private var UnderlineThickness = 50
    /** The font's encoding name. This encoding is 'StandardEncoding' or
     * 'AdobeStandardEncoding' for a font that can be totally encoded
     * according to the characters names. For all other names the
     * font is treated as symbolic.
     */
    private var EncodingScheme = "FontSpecific"
    /** A variable.
     */
    private var CapHeight = 700
    /** A variable.
     */
    private var XHeight = 480
    /** A variable.
     */
    private var Ascender = 800
    /** A variable.
     */
    private var Descender = -200
    /** A variable.
     */
    private var StdHW: Int = 0
    /** A variable.
     */
    private var StdVW = 80

    /** Represents the section CharMetrics in the AFM file. Each
     * value of this array contains a Object[4] with an
     * Integer, Integer, String and int[]. This is the code, width, name and char bbox.
     * The key is the name of the char and also an Integer with the char number.
     */
    private val CharMetrics = HashMap<Any, Array<Any>>()
    /** Represents the section KernPairs in the AFM file. The key is
     * the name of the first character and the value is a Object[]
     * with 2 elements for each kern pair. Position 0 is the name of
     * the second character and position 1 is the kerning distance. This is
     * repeated for all the pairs.
     */
    private val KernPairs = HashMap<String, Array<Any>>()
    /** true if this font is one of the 14 built in fonts.
     */
    private var builtinFont = false

    init {
        if (emb && ttfAfm != null && pfb == null)
            throw DocumentException(MessageLocalization.getComposedMessage("two.byte.arrays.are.needed.if.the.type1.font.is.embedded"))
        if (emb && ttfAfm != null)
            this.pfb = pfb
        encoding = enc
        embedded = emb
        fontType = BaseFont.FONT_TYPE_T1
        var rf: RandomAccessFileOrArray? = null
        var `is`: InputStream? = null
        if (BaseFont.BuiltinFonts14.containsKey(fileName)) {
            embedded = false
            builtinFont = true
            var buf = ByteArray(1024)
            try {
                if (resourceAnchor == null)
                    resourceAnchor = FontsResourceAnchor()
                `is` = StreamUtil.getResourceStream(BaseFont.RESOURCE_PATH + fileName + ".afm", resourceAnchor!!.javaClass.classLoader)
                if (`is` == null) {
                    val msg = MessageLocalization.getComposedMessage("1.not.found.as.resource", fileName)
                    System.err.println(msg)
                    //throw new DocumentException(msg);
                }
                val out = ByteArrayOutputStream()
                while (true) {
                    val size = `is`.read(buf)
                    if (size < 0)
                        break
                    out.write(buf, 0, size)
                }
                buf = out.toByteArray()
            } finally {
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: Exception) {
                        // empty on purpose
                    }

                }
            }
            try {
                rf = RandomAccessFileOrArray(buf)
                process(rf)
            } finally {
                if (rf != null) {
                    try {
                        rf.close()
                    } catch (e: Exception) {
                        // empty on purpose
                    }

                }
            }
        } else if (fileName.toLowerCase().endsWith(".afm")) {
            try {
                if (ttfAfm == null)
                    rf = RandomAccessFileOrArray(fileName, forceRead, Document.plainRandomAccess)
                else
                    rf = RandomAccessFileOrArray(ttfAfm)
                process(rf)
            } finally {
                if (rf != null) {
                    try {
                        rf.close()
                    } catch (e: Exception) {
                        // empty on purpose
                    }

                }
            }
        } else if (fileName.toLowerCase().endsWith(".pfm")) {
            try {
                val ba = ByteArrayOutputStream()
                if (ttfAfm == null)
                    rf = RandomAccessFileOrArray(fileName, forceRead, Document.plainRandomAccess)
                else
                    rf = RandomAccessFileOrArray(ttfAfm)
                Pfm2afm.convert(rf, ba)
                rf.close()
                rf = RandomAccessFileOrArray(ba.toByteArray())
                process(rf)
            } finally {
                if (rf != null) {
                    try {
                        rf.close()
                    } catch (e: Exception) {
                        // empty on purpose
                    }

                }
            }
        } else
            throw DocumentException(MessageLocalization.getComposedMessage("1.is.not.an.afm.or.pfm.font.file", fileName))

        EncodingScheme = EncodingScheme.trim { it <= ' ' }
        if (EncodingScheme == "AdobeStandardEncoding" || EncodingScheme == "StandardEncoding") {
            fontSpecific = false
        }
        if (!encoding.startsWith("#"))
            PdfEncodings.convertToBytes(" ", enc) // check if the encoding exists
        createEncoding()
    }

    /** Gets the width from the font according to the name or,
     * if the name is null, meaning it is a symbolic font,
     * the char c.
     * @param c the char if the font is symbolic
     * *
     * @param name the glyph name
     * *
     * @return the width of the char
     */
    fun getRawWidth(c: Int, name: String?): Int {
        val metrics: Array<Any>?
        if (name == null) {
            // font specific
            metrics = CharMetrics[Integer.valueOf(c)]
        } else {
            if (name == ".notdef")
                return 0
            metrics = CharMetrics[name]
        }
        if (metrics != null)
            return (metrics[1] as Int).toInt()
        return 0
    }

    /** Gets the kerning between two Unicode characters. The characters
     * are converted to names and this names are used to find the kerning
     * pairs in the HashMap KernPairs.
     * @param char1 the first char
     * *
     * @param char2 the second char
     * *
     * @return the kerning to be applied
     */
    override fun getKerning(char1: Int, char2: Int): Int {
        val first = GlyphList.unicodeToName(char1) ?: return 0
        val second = GlyphList.unicodeToName(char2) ?: return 0
        val obj = KernPairs[first] ?: return 0
        var k = 0
        while (k < obj.size) {
            if (second == obj[k])
                return (obj[k + 1] as Int).toInt()
            k += 2
        }
        return 0
    }


    /** Reads the font metrics
     * @param rf the AFM file
     * *
     * @throws DocumentException the AFM file is invalid
     * *
     * @throws IOException the AFM file could not be read
     */
    @Throws(DocumentException::class, IOException::class)
    fun process(rf: RandomAccessFileOrArray) {
        var line: String
        var isMetrics = false
        while ((line = rf.readLine()) != null) {
            val tok = StringTokenizer(line, " ,\n\r\t\f")
            if (!tok.hasMoreTokens())
                continue
            val ident = tok.nextToken()
            if (ident == "FontName")
                postscriptFontName = tok.nextToken("\u00ff").substring(1)
            else if (ident == "FullName")
                FullName = tok.nextToken("\u00ff").substring(1)
            else if (ident == "FamilyName")
                FamilyName = tok.nextToken("\u00ff").substring(1)
            else if (ident == "Weight")
                Weight = tok.nextToken("\u00ff").substring(1)
            else if (ident == "ItalicAngle")
                ItalicAngle = java.lang.Float.parseFloat(tok.nextToken())
            else if (ident == "IsFixedPitch")
                IsFixedPitch = tok.nextToken() == "true"
            else if (ident == "CharacterSet")
                CharacterSet = tok.nextToken("\u00ff").substring(1)
            else if (ident == "FontBBox") {
                llx = java.lang.Float.parseFloat(tok.nextToken()).toInt()
                lly = java.lang.Float.parseFloat(tok.nextToken()).toInt()
                urx = java.lang.Float.parseFloat(tok.nextToken()).toInt()
                ury = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            } else if (ident == "UnderlinePosition")
                UnderlinePosition = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            else if (ident == "UnderlineThickness")
                UnderlineThickness = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            else if (ident == "EncodingScheme")
                EncodingScheme = tok.nextToken("\u00ff").substring(1)
            else if (ident == "CapHeight")
                CapHeight = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            else if (ident == "XHeight")
                XHeight = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            else if (ident == "Ascender")
                Ascender = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            else if (ident == "Descender")
                Descender = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            else if (ident == "StdHW")
                StdHW = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            else if (ident == "StdVW")
                StdVW = java.lang.Float.parseFloat(tok.nextToken()).toInt()
            else if (ident == "StartCharMetrics") {
                isMetrics = true
                break
            }
        }
        if (!isMetrics)
            throw DocumentException(MessageLocalization.getComposedMessage("missing.startcharmetrics.in.1", fileName))
        while ((line = rf.readLine()) != null) {
            var tok = StringTokenizer(line)
            if (!tok.hasMoreTokens())
                continue
            var ident = tok.nextToken()
            if (ident == "EndCharMetrics") {
                isMetrics = false
                break
            }
            var C = Integer.valueOf(-1)
            var WX = Integer.valueOf(250)
            var N = ""
            var B: IntArray? = null

            tok = StringTokenizer(line, ";")
            while (tok.hasMoreTokens()) {
                val tokc = StringTokenizer(tok.nextToken())
                if (!tokc.hasMoreTokens())
                    continue
                ident = tokc.nextToken()
                if (ident == "C")
                    C = Integer.valueOf(tokc.nextToken())
                else if (ident == "WX")
                    WX = Integer.valueOf(java.lang.Float.parseFloat(tokc.nextToken()).toInt())
                else if (ident == "N")
                    N = tokc.nextToken()
                else if (ident == "B") {
                    B = intArrayOf(Integer.parseInt(tokc.nextToken()), Integer.parseInt(tokc.nextToken()), Integer.parseInt(tokc.nextToken()), Integer.parseInt(tokc.nextToken()))
                }
            }
            val metrics = arrayOf<Any>(C, WX, N, B)
            if (C!!.toInt() >= 0)
                CharMetrics.put(C, metrics)
            CharMetrics.put(N, metrics)
        }
        if (isMetrics)
            throw DocumentException(MessageLocalization.getComposedMessage("missing.endcharmetrics.in.1", fileName))
        if (!CharMetrics.containsKey("nonbreakingspace")) {
            val space = CharMetrics["space"]
            if (space != null)
                CharMetrics.put("nonbreakingspace", space)
        }
        while ((line = rf.readLine()) != null) {
            val tok = StringTokenizer(line)
            if (!tok.hasMoreTokens())
                continue
            val ident = tok.nextToken()
            if (ident == "EndFontMetrics")
                return
            if (ident == "StartKernPairs") {
                isMetrics = true
                break
            }
        }
        if (!isMetrics)
            throw DocumentException(MessageLocalization.getComposedMessage("missing.endfontmetrics.in.1", fileName))
        while ((line = rf.readLine()) != null) {
            val tok = StringTokenizer(line)
            if (!tok.hasMoreTokens())
                continue
            val ident = tok.nextToken()
            if (ident == "KPX") {
                val first = tok.nextToken()
                val second = tok.nextToken()
                val width = Integer.valueOf(java.lang.Float.parseFloat(tok.nextToken()).toInt())
                val relates = KernPairs[first]
                if (relates == null)
                    KernPairs.put(first, arrayOf(second, width))
                else {
                    val n = relates.size
                    val relates2 = arrayOfNulls<Any>(n + 2)
                    System.arraycopy(relates, 0, relates2, 0, n)
                    relates2[n] = second
                    relates2[n + 1] = width
                    KernPairs.put(first, relates2)
                }
            } else if (ident == "EndKernPairs") {
                isMetrics = false
                break
            }
        }
        if (isMetrics)
            throw DocumentException(MessageLocalization.getComposedMessage("missing.endkernpairs.in.1", fileName))
        rf.close()
    }

    /** If the embedded flag is false or if the font is
     * one of the 14 built in types, it returns null,
     * otherwise the font is read and output in a PdfStream object.
     * @return the PdfStream containing the font or null
     * *
     * @throws DocumentException if there is an error reading the font
     * *
     * @since 2.1.3
     */
    // empty on purpose
    val fullFontStream: PdfStream?
        @Throws(DocumentException::class)
        get() {
            if (builtinFont || !embedded)
                return null
            var rf: RandomAccessFileOrArray? = null
            try {
                val filePfb = fileName.substring(0, fileName.length - 3) + "pfb"
                if (pfb == null)
                    rf = RandomAccessFileOrArray(filePfb, true, Document.plainRandomAccess)
                else
                    rf = RandomAccessFileOrArray(pfb)
                val fileLength = rf.length().toInt()
                val st = ByteArray(fileLength - 18)
                val lengths = IntArray(3)
                var bytePtr = 0
                for (k in 0..2) {
                    if (rf.read() != 0x80)
                        throw DocumentException(MessageLocalization.getComposedMessage("start.marker.missing.in.1", filePfb))
                    if (rf.read() != PFB_TYPES[k])
                        throw DocumentException(MessageLocalization.getComposedMessage("incorrect.segment.type.in.1", filePfb))
                    var size = rf.read()
                    size += rf.read() shl 8
                    size += rf.read() shl 16
                    size += rf.read() shl 24
                    lengths[k] = size
                    while (size != 0) {
                        val got = rf.read(st, bytePtr, size)
                        if (got < 0)
                            throw DocumentException(MessageLocalization.getComposedMessage("premature.end.in.1", filePfb))
                        bytePtr += got
                        size -= got
                    }
                }
                return BaseFont.StreamFont(st, lengths, compressionLevel)
            } catch (e: Exception) {
                throw DocumentException(e)
            } finally {
                if (rf != null) {
                    try {
                        rf.close()
                    } catch (e: Exception) {
                    }

                }
            }
        }

    /** Generates the font descriptor for this font or null if it is
     * one of the 14 built in fonts.
     * @param fontStream the indirect reference to a PdfStream containing the font or null
     * *
     * @return the PdfDictionary containing the font descriptor or null
     */
    private fun getFontDescriptor(fontStream: PdfIndirectReference?): PdfDictionary? {
        if (builtinFont)
            return null
        val dic = PdfDictionary(PdfName.FONTDESCRIPTOR)
        dic.put(PdfName.ASCENT, PdfNumber(Ascender))
        dic.put(PdfName.CAPHEIGHT, PdfNumber(CapHeight))
        dic.put(PdfName.DESCENT, PdfNumber(Descender))
        dic.put(PdfName.FONTBBOX, PdfRectangle(llx.toFloat(), lly.toFloat(), urx.toFloat(), ury.toFloat()))
        dic.put(PdfName.FONTNAME, PdfName(postscriptFontName))
        dic.put(PdfName.ITALICANGLE, PdfNumber(ItalicAngle))
        dic.put(PdfName.STEMV, PdfNumber(StdVW))
        if (fontStream != null)
            dic.put(PdfName.FONTFILE, fontStream)
        var flags = 0
        if (IsFixedPitch)
            flags = flags or 1
        flags = flags or if (fontSpecific) 4 else 32
        if (ItalicAngle < 0)
            flags = flags or 64
        if (postscriptFontName!!.indexOf("Caps") >= 0 || postscriptFontName!!.endsWith("SC"))
            flags = flags or 131072
        if (Weight == "Bold")
            flags = flags or 262144
        dic.put(PdfName.FLAGS, PdfNumber(flags))

        return dic
    }

    /** Generates the font dictionary for this font.
     * @return the PdfDictionary containing the font dictionary
     * *
     * @param firstChar the first valid character
     * *
     * @param lastChar the last valid character
     * *
     * @param shortTag a 256 bytes long byte array where each unused byte is represented by 0
     * *
     * @param fontDescriptor the indirect reference to a PdfDictionary containing the font descriptor or null
     */
    private fun getFontBaseType(fontDescriptor: PdfIndirectReference?, firstChar: Int, lastChar: Int, shortTag: ByteArray): PdfDictionary {
        var firstChar = firstChar
        val dic = PdfDictionary(PdfName.FONT)
        dic.put(PdfName.SUBTYPE, PdfName.TYPE1)
        dic.put(PdfName.BASEFONT, PdfName(postscriptFontName))
        val stdEncoding = encoding == "Cp1252" || encoding == "MacRoman"
        if (!fontSpecific || specialMap != null) {
            for (k in firstChar..lastChar) {
                if (differences[k] != BaseFont.notdef) {
                    firstChar = k
                    break
                }
            }
            if (stdEncoding)
                dic.put(PdfName.ENCODING, if (encoding == "Cp1252") PdfName.WIN_ANSI_ENCODING else PdfName.MAC_ROMAN_ENCODING)
            else {
                val enc = PdfDictionary(PdfName.ENCODING)
                val dif = PdfArray()
                var gap = true
                for (k in firstChar..lastChar) {
                    if (shortTag[k].toInt() != 0) {
                        if (gap) {
                            dif.add(PdfNumber(k))
                            gap = false
                        }
                        dif.add(PdfName(differences[k]))
                    } else
                        gap = true
                }
                enc.put(PdfName.DIFFERENCES, dif)
                dic.put(PdfName.ENCODING, enc)
            }
        }
        if (specialMap != null || forceWidthsOutput || !(builtinFont && (fontSpecific || stdEncoding))) {
            dic.put(PdfName.FIRSTCHAR, PdfNumber(firstChar))
            dic.put(PdfName.LASTCHAR, PdfNumber(lastChar))
            val wd = PdfArray()
            for (k in firstChar..lastChar) {
                if (shortTag[k].toInt() == 0)
                    wd.add(PdfNumber(0))
                else
                    wd.add(PdfNumber(widths[k]))
            }
            dic.put(PdfName.WIDTHS, wd)
        }
        if (!builtinFont && fontDescriptor != null)
            dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor)
        return dic
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
    fun writeFont(writer: PdfWriter, ref: PdfIndirectReference, params: Array<Any>) {
        var firstChar = (params[0] as Int).toInt()
        var lastChar = (params[1] as Int).toInt()
        val shortTag = params[2] as ByteArray
        val subsetp = (params[3] as Boolean).booleanValue() && subset
        if (!(subsetp && embedded)) {
            firstChar = 0
            lastChar = shortTag.size - 1
            for (k in shortTag.indices)
                shortTag[k] = 1
        }
        var ind_font: PdfIndirectReference? = null
        var pobj: PdfObject? = null
        var obj: PdfIndirectObject? = null
        pobj = fullFontStream
        if (pobj != null) {
            obj = writer.addToBody(pobj)
            ind_font = obj!!.indirectReference
        }
        pobj = getFontDescriptor(ind_font)
        if (pobj != null) {
            obj = writer.addToBody(pobj)
            ind_font = obj!!.indirectReference
        }
        pobj = getFontBaseType(ind_font, firstChar, lastChar, shortTag)
        writer.addToBody(pobj, ref)
    }

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
        when (key) {
            BaseFont.AWT_ASCENT, BaseFont.ASCENT -> return Ascender * fontSize / 1000
            BaseFont.CAPHEIGHT -> return CapHeight * fontSize / 1000
            BaseFont.AWT_DESCENT, BaseFont.DESCENT -> return Descender * fontSize / 1000
            BaseFont.ITALICANGLE -> return ItalicAngle
            BaseFont.BBOXLLX -> return llx * fontSize / 1000
            BaseFont.BBOXLLY -> return lly * fontSize / 1000
            BaseFont.BBOXURX -> return urx * fontSize / 1000
            BaseFont.BBOXURY -> return ury * fontSize / 1000
            BaseFont.AWT_LEADING -> return 0f
            BaseFont.AWT_MAXADVANCE -> return (urx - llx) * fontSize / 1000
            BaseFont.UNDERLINE_POSITION -> return UnderlinePosition * fontSize / 1000
            BaseFont.UNDERLINE_THICKNESS -> return UnderlineThickness * fontSize / 1000
        }
        return 0f
    }

    /** Sets the font parameter identified by key. Valid values
     * for key are ASCENT, AWT_ASCENT, CAPHEIGHT,
     * DESCENT, AWT_DESCENT,
     * ITALICANGLE, BBOXLLX, BBOXLLY, BBOXURX
     * and BBOXURY.
     * @param key the parameter to be updated
     * *
     * @param value the parameter value
     */
    override fun setFontDescriptor(key: Int, value: Float) {
        when (key) {
            BaseFont.AWT_ASCENT, BaseFont.ASCENT -> Ascender = value.toInt()
            BaseFont.AWT_DESCENT, BaseFont.DESCENT -> Descender = value.toInt()
            else -> {
            }
        }
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
        get() = arrayOf(arrayOf<String>("", "", "", FullName))

    /** Gets all the entries of the names-table. If it is a True Type font
     * each array element will have {Name ID, Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"4", "", "", "",
     * font name}.
     * @return the full name of the font
     */
    override val allNameEntries: Array<Array<String>>
        get() = arrayOf(arrayOf<String>("4", "", "", "", FullName))

    /** Gets the family name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the family name of the font
     */
    override val familyFontName: Array<Array<String>>
        get() = arrayOf(arrayOf<String>("", "", "", FamilyName))

    /** Checks if the font has any kerning pairs.
     * @return true if the font has any kerning pairs
     */
    override fun hasKernPairs(): Boolean {
        return !KernPairs.isEmpty()
    }

    /**
     * Sets the kerning between two Unicode chars.
     * @param char1 the first char
     * *
     * @param char2 the second char
     * *
     * @param kern the kerning to apply in normalized 1000 units
     * *
     * @return `true` if the kerning was applied, `false` otherwise
     */
    override fun setKerning(char1: Int, char2: Int, kern: Int): Boolean {
        val first = GlyphList.unicodeToName(char1) ?: return false
        val second = GlyphList.unicodeToName(char2) ?: return false
        var obj: Array<Any>? = KernPairs[first]
        if (obj == null) {
            obj = arrayOf(second, Integer.valueOf(kern))
            KernPairs.put(first, obj)
            return true
        }
        var k = 0
        while (k < obj.size) {
            if (second == obj[k]) {
                obj[k + 1] = Integer.valueOf(kern)
                return true
            }
            k += 2
        }
        val size = obj.size
        val obj2 = arrayOfNulls<Any>(size + 2)
        System.arraycopy(obj, 0, obj2, 0, size)
        obj2[size] = second
        obj2[size + 1] = Integer.valueOf(kern)
        KernPairs.put(first, obj2)
        return true
    }

    override fun getRawCharBBox(c: Int, name: String?): IntArray {
        val metrics: Array<Any>?
        if (name == null) {
            // font specific
            metrics = CharMetrics[Integer.valueOf(c)]
        } else {
            if (name == ".notdef")
                return null
            metrics = CharMetrics[name]
        }
        if (metrics != null)
            return metrics[3] as IntArray
        return null
    }

    companion object {
        private var resourceAnchor: FontsResourceAnchor? = null
        /** Types of records in a PFB file. ASCII is 1 and BINARY is 2.
         * They have to appear in the PFB file in this sequence.
         */
        private val PFB_TYPES = intArrayOf(1, 2, 1)
    }

}
