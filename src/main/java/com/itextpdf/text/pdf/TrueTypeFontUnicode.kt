/*
 * $Id: 17eac58934895d5bb3e31d5cbed047255c055147 $
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
import com.itextpdf.text.Utilities
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.fonts.otf.GlyphSubstitutionTableReader
import com.itextpdf.text.pdf.fonts.otf.Language
import com.itextpdf.text.pdf.languages.ArabicLigaturizer

import java.io.IOException
import java.util.Arrays
import java.util.Comparator
import java.util.HashMap

/** Represents a True Type font with Unicode encoding. All the character
 * in the font can be used directly by using the encoding Identity-H or
 * Identity-V. This is the only way to represent some character sets such
 * as Thai.
 * @author  Paulo Soares
 */
internal class TrueTypeFontUnicode
/**
 * Creates a new TrueType font addressed by Unicode characters. The font
 * will always be embedded.
 * @param ttFile the location of the font on file. The file must end in '.ttf'.
 * * The modifiers after the name are ignored.
 * *
 * @param enc the encoding to be applied to this font
 * *
 * @param emb true if the font is to be embedded in the PDF
 * *
 * @param ttfAfm the font as a byte array
 * *
 * @throws DocumentException the font is invalid
 * *
 * @throws IOException the font file could not be read
 */
@Throws(DocumentException::class, IOException::class)
constructor(ttFile: String, enc: String, emb: Boolean, ttfAfm: ByteArray, forceRead: Boolean) : TrueTypeFont(), Comparator<IntArray> {

    protected var glyphSubstitutionMap: Map<String, Glyph>? = null
        private set
    var supportedLanguage:

            Language? = null
        private set

    init {
        val nameBase = getBaseName(ttFile)
        val ttcName = TrueTypeFont.getTTCName(nameBase)
        if (nameBase.length < ttFile.length) {
            style = ttFile.substring(nameBase.length)
        }
        encoding = enc
        embedded = emb
        fileName = ttcName
        ttcIndex = ""
        if (ttcName.length < nameBase.length)
            ttcIndex = nameBase.substring(ttcName.length + 1)
        fontType = BaseFont.FONT_TYPE_TTUNI
        if ((fileName.toLowerCase().endsWith(".ttf") || fileName.toLowerCase().endsWith(".otf") || fileName.toLowerCase().endsWith(".ttc")) && (enc == BaseFont.IDENTITY_H || enc == BaseFont.IDENTITY_V) && emb) {
            process(ttfAfm, forceRead)
            if (os_2.fsType.toInt() == 2)
                throw DocumentException(MessageLocalization.getComposedMessage("1.cannot.be.embedded.due.to.licensing.restrictions", fileName + style))
            // Sivan
            if (cmap31 == null && !fontSpecific || cmap10 == null && fontSpecific)
                directTextToByte = true
            //throw new DocumentException(MessageLocalization.getComposedMessage("1.2.does.not.contain.an.usable.cmap", fileName, style));
            if (fontSpecific) {
                fontSpecific = false
                val tempEncoding = encoding
                encoding = ""
                createEncoding()
                encoding = tempEncoding
                fontSpecific = true
            }
        } else
            throw DocumentException(MessageLocalization.getComposedMessage("1.2.is.not.a.ttf.font.file", fileName, style))
        vertical = enc.endsWith("V")
    }

    @Throws(DocumentException::class, IOException::class)
    internal override fun process(ttfAfm: ByteArray?, preload: Boolean) {
        super.process(ttfAfm, preload)
        //readGsubTable();
    }

    /**
     * Gets the width of a char in normalized 1000 units.
     * @param char1 the unicode char to get the width of
     * *
     * @return the width in normalized 1000 units
     */
    override fun getWidth(char1: Int): Int {
        if (vertical)
            return 1000
        if (fontSpecific) {
            if (char1 and 0xff00 == 0 || char1 and 0xff00 == 0xf000)
                return getRawWidth(char1 and 0xff, null)
            else
                return 0
        } else {
            return getRawWidth(char1, encoding)
        }
    }

    /**
     * Gets the width of a String in normalized 1000 units.
     * @param text the String to get the width of
     * *
     * @return the width in normalized 1000 units
     */
    override fun getWidth(text: String): Int {
        if (vertical)
            return text.length * 1000
        var total = 0
        if (fontSpecific) {
            val cc = text.toCharArray()
            val len = cc.size
            for (k in 0..len - 1) {
                val c = cc[k]
                if (c.toInt() and 0xff00 == 0 || c.toInt() and 0xff00 == 0xf000)
                    total += getRawWidth(c.toInt() and 0xff, null)
            }
        } else {
            val len = text.length
            var k = 0
            while (k < len) {
                if (Utilities.isSurrogatePair(text, k)) {
                    total += getRawWidth(Utilities.convertToUtf32(text, k), encoding)
                    ++k
                } else
                    total += getRawWidth(text[k].toInt(), encoding)
                ++k
            }
        }
        return total
    }

    /** Creates a ToUnicode CMap to allow copy and paste from Acrobat.
     * @param metrics metrics[0] contains the glyph index and metrics[2]
     * * contains the Unicode code
     * *
     * @return the stream representing this CMap or null
     */
    fun getToUnicode(metrics: Array<Any>): PdfStream? {
        if (metrics.size == 0)
            return null
        val buf = StringBuffer(
                "/CIDInit /ProcSet findresource begin\n" +
                        "12 dict begin\n" +
                        "begincmap\n" +
                        "/CIDSystemInfo\n" +
                        "<< /Registry (TTX+0)\n" +
                        "/Ordering (T42UV)\n" +
                        "/Supplement 0\n" +
                        ">> def\n" +
                        "/CMapName /TTX+0 def\n" +
                        "/CMapType 2 def\n" +
                        "1 begincodespacerange\n" +
                        "<0000><FFFF>\n" +
                        "endcodespacerange\n")
        var size = 0
        for (k in metrics.indices) {
            if (size == 0) {
                if (k != 0) {
                    buf.append("endbfrange\n")
                }
                size = Math.min(100, metrics.size - k)
                buf.append(size).append(" beginbfrange\n")
            }
            --size
            val metric = metrics[k] as IntArray
            val fromTo = toHex(metric[0])
            buf.append(fromTo).append(fromTo).append(toHex(metric[2])).append('\n')
        }
        buf.append(
                "endbfrange\n" +
                        "endcmap\n" +
                        "CMapName currentdict /CMap defineresource pop\n" +
                        "end end\n")
        val s = buf.toString()
        val stream = PdfStream(PdfEncodings.convertToBytes(s, null))
        stream.flateCompress(compressionLevel.toInt())
        return stream
    }

    /** Generates the CIDFontTyte2 dictionary.
     * @param fontDescriptor the indirect reference to the font descriptor
     * *
     * @param subsetPrefix the subset prefix
     * *
     * @param metrics the horizontal width metrics
     * *
     * @return a stream
     */
    fun getCIDFontType2(fontDescriptor: PdfIndirectReference, subsetPrefix: String, metrics: Array<Any>): PdfDictionary {
        val dic = PdfDictionary(PdfName.FONT)
        // sivan; cff
        if (cff) {
            dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE0)
            dic.put(PdfName.BASEFONT, PdfName(subsetPrefix + postscriptFontName + "-" + encoding))
        } else {
            dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE2)
            dic.put(PdfName.BASEFONT, PdfName(subsetPrefix + postscriptFontName))
        }
        dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor)
        if (!cff)
            dic.put(PdfName.CIDTOGIDMAP, PdfName.IDENTITY)
        val cdic = PdfDictionary()
        cdic.put(PdfName.REGISTRY, PdfString("Adobe"))
        cdic.put(PdfName.ORDERING, PdfString("Identity"))
        cdic.put(PdfName.SUPPLEMENT, PdfNumber(0))
        dic.put(PdfName.CIDSYSTEMINFO, cdic)
        if (!vertical) {
            dic.put(PdfName.DW, PdfNumber(1000))
            val buf = StringBuffer("[")
            var lastNumber = -10
            var firstTime = true
            for (k in metrics.indices) {
                val metric = metrics[k] as IntArray
                if (metric[1] == 1000)
                    continue
                val m = metric[0]
                if (m == lastNumber + 1) {
                    buf.append(' ').append(metric[1])
                } else {
                    if (!firstTime) {
                        buf.append(']')
                    }
                    firstTime = false
                    buf.append(m).append('[').append(metric[1])
                }
                lastNumber = m
            }
            if (buf.length > 1) {
                buf.append("]]")
                dic.put(PdfName.W, PdfLiteral(buf.toString()))
            }
        }
        return dic
    }

    /** Generates the font dictionary.
     * @param descendant the descendant dictionary
     * *
     * @param subsetPrefix the subset prefix
     * *
     * @param toUnicode the ToUnicode stream
     * *
     * @return the stream
     */
    fun getFontBaseType(descendant: PdfIndirectReference, subsetPrefix: String, toUnicode: PdfIndirectReference?): PdfDictionary {
        val dic = PdfDictionary(PdfName.FONT)

        dic.put(PdfName.SUBTYPE, PdfName.TYPE0)
        // The PDF Reference manual advises to add -encoding to CID font names
        if (cff)
            dic.put(PdfName.BASEFONT, PdfName(subsetPrefix + postscriptFontName + "-" + encoding))
        else
            dic.put(PdfName.BASEFONT, PdfName(subsetPrefix + postscriptFontName))//dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix+fontName));
        //dic.put(PdfName.BASEFONT, new PdfName(fontName));
        dic.put(PdfName.ENCODING, PdfName(encoding))
        dic.put(PdfName.DESCENDANTFONTS, PdfArray(descendant))
        if (toUnicode != null)
            dic.put(PdfName.TOUNICODE, toUnicode)
        return dic
    }

    fun GetCharFromGlyphId(gid: Int): Int {
        if (glyphIdToChar == null) {
            val g2 = IntArray(maxGlyphId)
            var map: HashMap<Int, IntArray>? = null
            if (cmapExt != null) {
                map = cmapExt
            } else if (cmap31 != null) {
                map = cmap31
            }
            if (map != null) {
                for (entry in map.entries) {
                    g2[entry.value[0]] = entry.key.toInt()
                }
            }
            glyphIdToChar = g2
        }
        return glyphIdToChar[gid]
    }

    /** The method used to sort the metrics array.
     * @param o1 the first element
     * *
     * @param o2 the second element
     * *
     * @return the comparison
     */
    override fun compare(o1: IntArray, o2: IntArray): Int {
        val m1 = o1[0]
        val m2 = o2[0]
        if (m1 < m2)
            return -1
        if (m1 == m2)
            return 0
        return 1
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
    internal override fun writeFont(writer: PdfWriter, ref: PdfIndirectReference, params: Array<Any>) {
        writer.getTtfUnicodeWriter().writeFont(this, ref, params, rotbits)
    }

    /**
     * Returns a PdfStream object with the full font program.
     * @return    a PdfStream with the font program
     * *
     * @since    2.1.3
     */
    override val fullFontStream: PdfStream
        @Throws(IOException::class, DocumentException::class)
        get() {
            if (cff) {
                return BaseFont.StreamFont(readCffFont(), "CIDFontType0C", compressionLevel)
            }
            return super.fullFontStream
        }

    /** A forbidden operation. Will throw a null pointer exception.
     * @param text the text
     * *
     * @return always null
     */
    override fun convertToBytes(text: String): ByteArray {
        return null
    }

    fun convertToBytes(char1: Int): ByteArray? {
        return null
    }

    /** Gets the glyph index and metrics for a character.
     * @param c the character
     * *
     * @return an int array with {glyph index, width}
     */
    override fun getMetricsTT(c: Int): IntArray? {
        if (cmapExt != null)
            return cmapExt!![Integer.valueOf(c)]
        var map: HashMap<Int, IntArray>? = null
        if (fontSpecific)
            map = cmap10
        else
            map = cmap31
        if (map == null)
            return null
        if (fontSpecific) {
            if (c and 0xffffff00.toInt() == 0 || c and 0xffffff00.toInt() == 0xf000)
                return map[Integer.valueOf(c and 0xff)]
            else
                return null
        } else {
            var result: IntArray? = map[Integer.valueOf(c)]
            if (result == null) {
                val ch = ArabicLigaturizer.getReverseMapping(c.toChar())
                if (ch != null)
                    result = map[Integer.valueOf(ch.toInt())]
            }
            return result
        }
    }

    /**
     * Checks if a character exists in this font.
     * @param c the character to check
     * *
     * @return true if the character has a glyph,
     * * false otherwise
     */
    override fun charExists(c: Int): Boolean {
        return getMetricsTT(c) != null
    }

    /**
     * Sets the character advance.
     * @param c the character
     * *
     * @param advance the character advance normalized to 1000 units
     * *
     * @return true if the advance was set,
     * * false otherwise
     */
    override fun setCharAdvance(c: Int, advance: Int): Boolean {
        val m = getMetricsTT(c) ?: return false
        m[1] = advance
        return true
    }

    override fun getCharBBox(c: Int): IntArray? {
        if (bboxes == null)
            return null
        val m = getMetricsTT(c) ?: return null
        return bboxes!![m[0]]
    }

    @Throws(IOException::class)
    private fun readGsubTable() {
        if (tables["GSUB"] != null) {

            val glyphToCharacterMap = HashMap<Int, Char>(cmap31!!.size)

            for (charCode in cmap31!!.keys) {
                val c = charCode!!.toInt().toChar()
                val glyphCode = cmap31!![charCode][0]
                glyphToCharacterMap.put(glyphCode, c)
            }

            val gsubReader = GlyphSubstitutionTableReader(
                    rf, tables["GSUB"][0], glyphToCharacterMap, glyphWidthsByIndex)

            try {
                gsubReader.read()
                supportedLanguage = gsubReader.supportedLanguage

                if (SUPPORTED_LANGUAGES_FOR_OTF.contains(supportedLanguage)) {
                    glyphSubstitutionMap = gsubReader.glyphSubstitutionMap
                    /*if (false) {
                    	StringBuilder  sb = new StringBuilder(50);
                        
                        for (int glyphCode : glyphToCharacterMap.keySet()) {
                        	sb.append(glyphCode).append("=>").append(glyphToCharacterMap.get(glyphCode)).append("\n");
                        }
                        System.out.println("GlyphToCharacterMap:\n" + sb.toString());
                    }
                    if (false) {
                        StringBuilder sb = new StringBuilder(50);
                        int count = 1;
                        
                        for (String chars : glyphSubstitutionMap.keySet()) {
                            int glyphId = glyphSubstitutionMap.get(chars).code;
                            sb.append(count++).append(".>");
                            sb.append(chars).append(" => ").append(glyphId).append("\n");
                        }
                        System.out.println("GlyphSubstitutionMap:\n" + sb.toString());
                    }*/
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    companion object {

        private val SUPPORTED_LANGUAGES_FOR_OTF = Arrays.asList(Language.BENGALI)

        private fun toHex4(n: Int): String {
            val s = "0000" + Integer.toHexString(n)
            return s.substring(s.length - 4)
        }

        /** Gets an hex string in the format "&lt;HHHH&gt;".
         * @param n the number
         * *
         * @return the hex string
         */
        fun toHex(n: Int): String {
            var n = n
            if (n < 0x10000)
                return "<" + toHex4(n) + ">"
            n -= 0x10000
            val high = n / 0x400 + 0xd800
            val low = n % 0x400 + 0xdc00
            return "[<" + toHex4(high) + toHex4(low) + ">]"
        }

        private val rotbits = byteArrayOf(0x80.toByte(), 0x40.toByte(), 0x20.toByte(), 0x10.toByte(), 0x08.toByte(), 0x04.toByte(), 0x02.toByte(), 0x01.toByte())
    }
}
