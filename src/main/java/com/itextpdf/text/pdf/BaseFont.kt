/*
 * $Id: dadaf63f95c7ab7645b58e465fa0158a25588d27 $
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
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.exceptions.InvalidPdfException

import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Base class for the several font types supported

 * @author Paulo Soares
 */

abstract class BaseFont
/**
 * Creates new BaseFont
 */
protected constructor() {

    protected var subsetRanges: ArrayList<IntArray>? = null
    /** The font type.
     */
    /** Gets the font type. The font types can be: FONT_TYPE_T1,
     * FONT_TYPE_TT, FONT_TYPE_CJK and FONT_TYPE_TTUNI.
     * @return the font type
     */
    var fontType: Int = 0
        internal set

    /** table of characters widths for this encoding  */
    /** Gets the font width array.
     * @return the font width array
     */
    var widths = IntArray(256)
        protected set

    /** encoding names  */
    /** Gets the array with the names of the characters.
     * @return the array with the names of the characters
     */
    var differences = arrayOfNulls<String>(256)
        protected set
    /** same as differences but with the unicode codes  */
    /** Gets the array with the unicode characters.
     * @return the array with the unicode characters
     */
    var unicodeDifferences = CharArray(256)
        protected set

    protected var charBBoxes = arrayOfNulls<IntArray>(256)
    /** encoding used with this font  */
    /** Gets the encoding used to convert String into byte[].
     * @return the encoding name
     */
    var encoding: String
        protected set

    /** true if the font is to be embedded in the PDF  */
    /** Gets the embedded flag.
     * @return true if the font is embedded.
     */
    var isEmbedded: Boolean = false
        protected set

    /**
     * The compression level for the font stream.
     * @since    2.1.3
     */
    /**
     * Returns the compression level used for the font streams.
     * @return the compression level (0 = best speed, 9 = best compression, -1 is default)
     * *
     * @since 2.1.3
     */
    /**
     * Sets the compression level to be used for the font streams.
     * @param compressionLevel a value between 0 (best speed) and 9 (best compression)
     * *
     * @since 2.1.3
     */
    var compressionLevel = PdfStream.DEFAULT_COMPRESSION
        set(compressionLevel) = if (compressionLevel < PdfStream.NO_COMPRESSION || compressionLevel > PdfStream.BEST_COMPRESSION)
            this.compressionLevel = PdfStream.DEFAULT_COMPRESSION
        else
            this.compressionLevel = compressionLevel

    /**
     * true if the font must use its built in encoding. In that case the
     * encoding is only used to map a char to the position inside
     * the font, not to the expected char name.
     */
    /** Gets the symbolic flag of the font.
     * @return true if the font is symbolic
     */
    var isFontSpecific = true
        protected set

    /** Forces the output of the width array. Only matters for the 14
     * built-in fonts.
     */
    /** Gets the state of the property.
     * @return value of property forceWidthsOutput
     */
    /** Set to true to force the generation of the
     * widths array.
     * @param forceWidthsOutput true to force the generation of the
     * * widths array
     */
    var isForceWidthsOutput = false

    /** Converts char directly to byte
     * by casting.
     */
    /** Gets the direct conversion of char to byte.
     * @return value of property directTextToByte.
     * *
     * @see .setDirectTextToByte
     */
    /** Sets the conversion of char directly to byte
     * by casting. This is a low level feature to put the bytes directly in
     * the content stream without passing through String.getBytes().
     * @param directTextToByte New value of property directTextToByte.
     */
    var isDirectTextToByte = false

    /** Indicates if all the glyphs and widths for that particular
     * encoding should be included in the document.
     */
    /** Indicates if all the glyphs and widths for that particular
     * encoding should be included in the document.
     * @return false to include all the glyphs and widths.
     */
    /** Indicates if all the glyphs and widths for that particular
     * encoding should be included in the document. When set to true
     * only the glyphs used will be included in the font. When set to false
     * and [.addSubsetRange] was not called the full font will be included
     * otherwise just the characters ranges will be included.
     * @param subset new value of property subset
     */
    var isSubset = true

    protected var fastWinansi = false

    /**
     * Custom encodings use this map to key the Unicode character
     * to the single byte code.
     */
    protected var specialMap: IntHashtable? = null

    /**
     * Indicates whether the font is used for verticl writing or not.
     * @return `true` if the writing mode is vertical for the given font, `false` otherwise.
     */
    var isVertical = false
        protected set

    /** Generates the PDF stream with the Type1 and Truetype fonts returning
     * a PdfStream.
     */
    internal class StreamFont : PdfStream {

        /** Generates the PDF stream with the Type1 and Truetype fonts returning
         * a PdfStream.
         * @param contents the content of the stream
         * *
         * @param lengths an array of int that describes the several lengths of each part of the font
         * *
         * @param compressionLevel    the compression level of the Stream
         * *
         * @throws DocumentException error in the stream compression
         * *
         * @since    2.1.3 (replaces the constructor without param compressionLevel)
         */
        @Throws(DocumentException::class)
        constructor(contents: ByteArray, lengths: IntArray, compressionLevel: Int) {
            try {
                bytes = contents
                put(PdfName.LENGTH, PdfNumber(bytes.size))
                for (k in lengths.indices) {
                    put(PdfName("Length" + (k + 1)), PdfNumber(lengths[k]))
                }
                flateCompress(compressionLevel)
            } catch (e: Exception) {
                throw DocumentException(e)
            }

        }

        /**
         * Generates the PDF stream for a font.
         * @param contents the content of a stream
         * *
         * @param subType the subtype of the font.
         * *
         * @param compressionLevel    the compression level of the Stream
         * *
         * @throws DocumentException error in the stream compression
         * *
         * @since    2.1.3 (replaces the constructor without param compressionLevel)
         */
        @Throws(DocumentException::class)
        constructor(contents: ByteArray, subType: String?, compressionLevel: Int) {
            try {
                bytes = contents
                put(PdfName.LENGTH, PdfNumber(bytes.size))
                if (subType != null)
                    put(PdfName.SUBTYPE, PdfName(subType))
                flateCompress(compressionLevel)
            } catch (e: Exception) {
                throw DocumentException(e)
            }

        }
    }

    /**
     * Creates the widths and the differences arrays
     */
    protected fun createEncoding() {
        if (encoding.startsWith("#")) {
            specialMap = IntHashtable()
            val tok = StringTokenizer(encoding.substring(1), " ,\t\n\r\f")
            if (tok.nextToken() == "full") {
                while (tok.hasMoreTokens()) {
                    val order = tok.nextToken()
                    val name = tok.nextToken()
                    val uni = Integer.parseInt(tok.nextToken(), 16).toChar()
                    var orderK: Int
                    if (order.startsWith("'"))
                        orderK = order[1].toInt()
                    else
                        orderK = Integer.parseInt(order)
                    orderK %= 256
                    specialMap!!.put(uni.toInt(), orderK)
                    differences[orderK] = name
                    unicodeDifferences[orderK] = uni
                    widths[orderK] = getRawWidth(uni.toInt(), name)
                    charBBoxes[orderK] = getRawCharBBox(uni.toInt(), name)
                }
            } else {
                var k = 0
                if (tok.hasMoreTokens())
                    k = Integer.parseInt(tok.nextToken())
                while (tok.hasMoreTokens() && k < 256) {
                    val hex = tok.nextToken()
                    val uni = Integer.parseInt(hex, 16) % 0x10000
                    val name = GlyphList.unicodeToName(uni)
                    if (name != null) {
                        specialMap!!.put(uni, k)
                        differences[k] = name
                        unicodeDifferences[k] = uni.toChar()
                        widths[k] = getRawWidth(uni, name)
                        charBBoxes[k] = getRawCharBBox(uni, name)
                        ++k
                    }
                }
            }
            for (k in 0..255) {
                if (differences[k] == null) {
                    differences[k] = notdef
                }
            }
        } else if (isFontSpecific) {
            for (k in 0..255) {
                widths[k] = getRawWidth(k, null)
                charBBoxes[k] = getRawCharBBox(k, null)
            }
        } else {
            var s: String
            var name: String?
            var c: Char
            val b = ByteArray(1)
            for (k in 0..255) {
                b[0] = k.toByte()
                s = PdfEncodings.convertToString(b, encoding)
                if (s.length > 0) {
                    c = s[0]
                } else {
                    c = '?'
                }
                name = GlyphList.unicodeToName(c.toInt())
                if (name == null)
                    name = notdef
                differences[k] = name
                unicodeDifferences[k] = c
                widths[k] = getRawWidth(c.toInt(), name)
                charBBoxes[k] = getRawCharBBox(c.toInt(), name)
            }
        }
    }

    /**
     * Gets the width from the font according to the Unicode char c
     * or the name. If the name is null it's a symbolic font.
     * @param c the unicode char
     * *
     * @param name the glyph name
     * *
     * @return the width of the char
     */
    internal abstract fun getRawWidth(c: Int, name: String?): Int

    /**
     * Gets the kerning between two Unicode chars.
     * @param char1 the first char
     * *
     * @param char2 the second char
     * *
     * @return the kerning to be applied in normalized 1000 units
     */
    abstract fun getKerning(char1: Int, char2: Int): Int

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
    abstract fun setKerning(char1: Int, char2: Int, kern: Int): Boolean

    /**
     * Gets the width of a char in normalized 1000 units.
     * @param char1 the unicode char to get the width of
     * *
     * @return the width in normalized 1000 units
     */
    open fun getWidth(char1: Int): Int {
        if (fastWinansi) {
            if (char1 < 128 || char1 >= 160 && char1 <= 255)
                return widths[char1]
            else
                return widths[PdfEncodings.winansi.get(char1)]
        } else {
            var total = 0
            val mbytes = convertToBytes(/*(char)*/char1)
            for (k in mbytes.indices)
                total += widths[0xff and mbytes[k]]
            return total
        }
    }

    /**
     * Gets the width of a String in normalized 1000 units.
     * @param text the String to get the width of
     * *
     * @return the width in normalized 1000 units
     */
    open fun getWidth(text: String): Int {
        var total = 0
        if (fastWinansi) {
            val len = text.length
            for (k in 0..len - 1) {
                val char1 = text[k]
                if (char1.toInt() < 128 || char1.toInt() >= 160 && char1.toInt() <= 255)
                    total += widths[char1]
                else
                    total += widths[PdfEncodings.winansi.get(char1.toInt())]
            }
            return total
        } else {
            val mbytes = convertToBytes(text)
            for (k in mbytes.indices)
                total += widths[0xff and mbytes[k]]
        }
        return total
    }

    /**
     * Gets the descent of a String in normalized 1000 units. The descent will always be
     * less than or equal to zero even if all the characters have an higher descent.
     * @param text the String to get the descent of
     * *
     * @return the descent in normalized 1000 units
     */
    fun getDescent(text: String): Int {
        var min = 0
        val chars = text.toCharArray()
        for (k in chars.indices) {
            val bbox = getCharBBox(chars[k].toInt())
            if (bbox != null && bbox[1] < min)
                min = bbox[1]
        }
        return min
    }

    /**
     * Gets the ascent of a String in normalized 1000 units. The ascent will always be
     * greater than or equal to zero even if all the characters have a lower ascent.
     * @param text the String to get the ascent of
     * *
     * @return the ascent in normalized 1000 units
     */
    fun getAscent(text: String): Int {
        var max = 0
        val chars = text.toCharArray()
        for (k in chars.indices) {
            val bbox = getCharBBox(chars[k].toInt())
            if (bbox != null && bbox[3] > max)
                max = bbox[3]
        }
        return max
    }

    /**
     * Gets the descent of a String in points. The descent will always be
     * less than or equal to zero even if all the characters have an higher descent.
     * @param text the String to get the descent of
     * *
     * @param fontSize the size of the font
     * *
     * @return the descent in points
     */
    fun getDescentPoint(text: String, fontSize: Float): Float {
        return getDescent(text).toFloat() * 0.001f * fontSize
    }

    /**
     * Gets the ascent of a String in points. The ascent will always be
     * greater than or equal to zero even if all the characters have a lower ascent.
     * @param text the String to get the ascent of
     * *
     * @param fontSize the size of the font
     * *
     * @return the ascent in points
     */
    fun getAscentPoint(text: String, fontSize: Float): Float {
        return getAscent(text).toFloat() * 0.001f * fontSize
    }
    // ia>

    /**
     * Gets the width of a String in points taking kerning
     * into account.
     * @param text the String to get the width of
     * *
     * @param fontSize the font size
     * *
     * @return the width in points
     */
    fun getWidthPointKerned(text: String, fontSize: Float): Float {
        val size = getWidth(text).toFloat() * 0.001f * fontSize
        if (!hasKernPairs())
            return size
        val len = text.length - 1
        var kern = 0
        val c = text.toCharArray()
        for (k in 0..len - 1) {
            kern += getKerning(c[k].toInt(), c[k + 1].toInt())
        }
        return size + kern.toFloat() * 0.001f * fontSize
    }

    /**
     * Gets the width of a String in points.
     * @param text the String to get the width of
     * *
     * @param fontSize the font size
     * *
     * @return the width in points
     */
    fun getWidthPoint(text: String, fontSize: Float): Float {
        return getWidth(text).toFloat() * 0.001f * fontSize
    }

    /**
     * Gets the width of a char in points.
     * @param char1 the char to get the width of
     * *
     * @param fontSize the font size
     * *
     * @return the width in points
     */
    fun getWidthPoint(char1: Int, fontSize: Float): Float {
        return getWidth(char1).toFloat() * 0.001f * fontSize
    }

    /**
     * Converts a String to a byte array according
     * to the font's encoding.
     * @param text the String to be converted
     * *
     * @return an array of byte representing the conversion according to the font's encoding
     */
    open fun convertToBytes(text: String): ByteArray {
        if (isDirectTextToByte)
            return PdfEncodings.convertToBytes(text, null)
        if (specialMap != null) {
            val b = ByteArray(text.length)
            var ptr = 0
            val length = text.length
            for (k in 0..length - 1) {
                val c = text[k]
                if (specialMap!!.containsKey(c.toInt()))
                    b[ptr++] = specialMap!!.get(c.toInt()).toByte()
            }
            if (ptr < length) {
                val b2 = ByteArray(ptr)
                System.arraycopy(b, 0, b2, 0, ptr)
                return b2
            } else
                return b
        }
        return PdfEncodings.convertToBytes(text, encoding)
    }

    /**
     * Converts a char to a byte array according
     * to the font's encoding.
     * @param char1 the char to be converted
     * *
     * @return an array of byte representing the conversion according to the font's encoding
     */
    internal open fun convertToBytes(char1: Int): ByteArray {
        if (isDirectTextToByte)
            return PdfEncodings.convertToBytes(char1.toChar(), null)
        if (specialMap != null) {
            if (specialMap!!.containsKey(char1))
                return byteArrayOf(specialMap!!.get(char1).toByte())
            else
                return ByteArray(0)
        }
        return PdfEncodings.convertToBytes(char1.toChar(), encoding)
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
    internal abstract fun writeFont(writer: PdfWriter, ref: PdfIndirectReference, params: Array<Any>)

    /**
     * Returns a PdfStream object with the full font program (if possible).
     * This method will return null for some types of fonts (CJKFont, Type3Font)
     * or if there is no font program available (standard Type 1 fonts).
     * @return    a PdfStream with the font program
     * *
     * @since    2.1.3
     */
    internal abstract val fullFontStream: PdfStream

    /** Gets the font parameter identified by key. Valid values
     * for key are ASCENT, AWT_ASCENT, CAPHEIGHT,
     * DESCENT, AWT_DESCENT,
     * ITALICANGLE, BBOXLLX, BBOXLLY, BBOXURX
     * and BBOXURY.
     * @param key the parameter to be extracted
     * *
     * @param fontSize the font size in points
     * *
     * @return the parameter in points
     */
    abstract fun getFontDescriptor(key: Int, fontSize: Float): Float

    /** Sets the font parameter identified by key. Valid values
     * for key are ASCENT, AWT_ASCENT, CAPHEIGHT,
     * DESCENT, AWT_DESCENT,
     * ITALICANGLE, BBOXLLX, BBOXLLY, BBOXURX
     * and BBOXURY.
     * @param key the parameter to be updated
     * *
     * @param value the parameter value
     */
    open fun setFontDescriptor(key: Int, value: Float) {
    }

    /** Gets the Unicode character corresponding to the byte output to the pdf stream.
     * @param index the byte index
     * *
     * @return the Unicode character
     */
    internal fun getUnicodeDifferences(index: Int): Char {
        return unicodeDifferences[index]
    }

    /** Gets the postscript font name.
     * @return the postscript font name
     */
    /**
     * Sets the font name that will appear in the pdf font dictionary.
     * Use with care as it can easily make a font unreadable if not embedded.
     * @param name the new font name
     */
    abstract var postscriptFontName: String

    val subfamily: String
        get() = ""

    /** Gets the full name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the full name of the font
     */
    abstract val fullFontName: Array<Array<String>>

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
    abstract val allNameEntries: Array<Array<String>>

    /** Gets the family name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the family name of the font
     */
    abstract val familyFontName: Array<Array<String>>

    /** Gets the code pages supported by the font. This has only meaning
     * with True Type fonts.
     * @return the code pages supported by the font
     */
    val codePagesSupported: Array<String>
        get() = arrayOfNulls(0)

    /** Gets the Unicode equivalent to a CID.
     * The (inexistent) CID  is translated as '\n'.
     * It has only meaning with CJK fonts with Identity encoding.
     * @param c the CID code
     * *
     * @return the Unicode equivalent
     */
    open fun getUnicodeEquivalent(c: Int): Int {
        return c
    }

    /** Gets the CID code given an Unicode.
     * It has only meaning with CJK fonts.
     * @param c the Unicode
     * *
     * @return the CID equivalent
     */
    open fun getCidCode(c: Int): Int {
        return c
    }

    /** Checks if the font has any kerning pairs.
     * @return true if the font has any kerning pairs
     */
    abstract fun hasKernPairs(): Boolean

    /**
     * Checks if a character exists in this font.
     * @param c the character to check
     * *
     * @return true if the character has a glyph,
     * * false otherwise
     */
    open fun charExists(c: Int): Boolean {
        val b = convertToBytes(c)
        return b.size > 0
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
    open fun setCharAdvance(c: Int, advance: Int): Boolean {
        val b = convertToBytes(c)
        if (b.size == 0)
            return false
        widths[0xff and b[0]] = advance
        return true
    }

    /**
     * Gets the smallest box enclosing the character contours. It will return
     * null if the font has not the information or the character has no
     * contours, as in the case of the space, for example. Characters with no contours may
     * also return [0,0,0,0].
     * @param c the character to get the contour bounding box from
     * *
     * @return an array of four floats with the bounding box in the format [llx,lly,urx,ury] or
     * * `null`
     */
    open fun getCharBBox(c: Int): IntArray? {
        val b = convertToBytes(c)
        if (b.size == 0)
            return null
        else
            return charBBoxes[b[0] and 0xff]
    }

    /**
     * get default array of six numbers specifying the font matrix, mapping glyph space to text space
     * @return an array of six values
     * * `null`
     */
    val fontMatrix: DoubleArray
        get() = DEFAULT_FONT_MATRIX

    protected abstract fun getRawCharBBox(c: Int, name: String?): IntArray

    /**
     * iText expects Arabic Diactrics (tashkeel) to have zero advance but some fonts,
     * most notably those that come with Windows, like times.ttf, have non-zero
     * advance for those characters. This method makes those character to have zero
     * width advance and work correctly in the iText Arabic shaping and reordering
     * context.
     */
    fun correctArabicAdvance() {
        run {
            var c = '\u064b'
            while (c <= '\u0658') {
                setCharAdvance(c.toInt(), 0)
                ++c
            }
        }
        setCharAdvance('\u0670', 0)
        run {
            var c = '\u06d6'
            while (c <= '\u06dc') {
                setCharAdvance(c.toInt(), 0)
                ++c
            }
        }
        run {
            var c = '\u06df'
            while (c <= '\u06e4') {
                setCharAdvance(c.toInt(), 0)
                ++c
            }
        }
        run {
            var c = '\u06e7'
            while (c <= '\u06e8') {
                setCharAdvance(c.toInt(), 0)
                ++c
            }
        }
        var c = '\u06ea'
        while (c <= '\u06ed') {
            setCharAdvance(c.toInt(), 0)
            ++c
        }
    }

    /**
     * Adds a character range when subsetting. The range is an int array
     * where the first element is the start range inclusive and the second element is the
     * end range inclusive. Several ranges are allowed in the same array.
     * @param range the character range
     */
    fun addSubsetRange(range: IntArray) {
        if (subsetRanges == null)
            subsetRanges = ArrayList<IntArray>()
        subsetRanges!!.add(range)
    }

    companion object {

        /** This is a possible value of a base 14 type 1 font  */
        val COURIER = "Courier"

        /** This is a possible value of a base 14 type 1 font  */
        val COURIER_BOLD = "Courier-Bold"

        /** This is a possible value of a base 14 type 1 font  */
        val COURIER_OBLIQUE = "Courier-Oblique"

        /** This is a possible value of a base 14 type 1 font  */
        val COURIER_BOLDOBLIQUE = "Courier-BoldOblique"

        /** This is a possible value of a base 14 type 1 font  */
        val HELVETICA = "Helvetica"

        /** This is a possible value of a base 14 type 1 font  */
        val HELVETICA_BOLD = "Helvetica-Bold"

        /** This is a possible value of a base 14 type 1 font  */
        val HELVETICA_OBLIQUE = "Helvetica-Oblique"

        /** This is a possible value of a base 14 type 1 font  */
        val HELVETICA_BOLDOBLIQUE = "Helvetica-BoldOblique"

        /** This is a possible value of a base 14 type 1 font  */
        val SYMBOL = "Symbol"

        /** This is a possible value of a base 14 type 1 font  */
        val TIMES_ROMAN = "Times-Roman"

        /** This is a possible value of a base 14 type 1 font  */
        val TIMES_BOLD = "Times-Bold"

        /** This is a possible value of a base 14 type 1 font  */
        val TIMES_ITALIC = "Times-Italic"

        /** This is a possible value of a base 14 type 1 font  */
        val TIMES_BOLDITALIC = "Times-BoldItalic"

        /** This is a possible value of a base 14 type 1 font  */
        val ZAPFDINGBATS = "ZapfDingbats"

        /** The maximum height above the baseline reached by glyphs in this
         * font, excluding the height of glyphs for accented characters.
         */
        val ASCENT = 1
        /** The y coordinate of the top of flat capital letters, measured from
         * the baseline.
         */
        val CAPHEIGHT = 2
        /** The maximum depth below the baseline reached by glyphs in this
         * font. The value is a negative number.
         */
        val DESCENT = 3
        /** The angle, expressed in degrees counterclockwise from the vertical,
         * of the dominant vertical strokes of the font. The value is
         * negative for fonts that slope to the right, as almost all italic fonts do.
         */
        val ITALICANGLE = 4
        /** The lower left x glyph coordinate.
         */
        val BBOXLLX = 5
        /** The lower left y glyph coordinate.
         */
        val BBOXLLY = 6
        /** The upper right x glyph coordinate.
         */
        val BBOXURX = 7
        /** The upper right y glyph coordinate.
         */
        val BBOXURY = 8

        /** AWT Font property  */
        val AWT_ASCENT = 9
        /** AWT Font property  */
        val AWT_DESCENT = 10
        /** AWT Font property  */
        val AWT_LEADING = 11
        /** AWT Font property  */
        val AWT_MAXADVANCE = 12
        /**
         * The underline position. Usually a negative value.
         */
        val UNDERLINE_POSITION = 13
        /**
         * The underline thickness.
         */
        val UNDERLINE_THICKNESS = 14
        /**
         * The strikethrough position.
         */
        val STRIKETHROUGH_POSITION = 15
        /**
         * The strikethrough thickness.
         */
        val STRIKETHROUGH_THICKNESS = 16
        /**
         * The recommended vertical size for subscripts for this font.
         */
        val SUBSCRIPT_SIZE = 17
        /**
         * The recommended vertical offset from the baseline for subscripts for this font. Usually a negative value.
         */
        val SUBSCRIPT_OFFSET = 18
        /**
         * The recommended vertical size for superscripts for this font.
         */
        val SUPERSCRIPT_SIZE = 19
        /**
         * The recommended vertical offset from the baseline for superscripts for this font.
         */
        val SUPERSCRIPT_OFFSET = 20
        /**
         * The weight class of the font, as defined by the font author
         * @since 5.0.2
         */
        val WEIGHT_CLASS = 21
        /**
         * The width class of the font, as defined by the font author
         * @since 5.0.2
         */
        val WIDTH_CLASS = 22
        /**
         * The entry of PDF FontDescriptor dictionary.
         * (Optional; PDF 1.5; strongly recommended for Type 3 fonts in Tagged PDF documents)
         * The weight (thickness) component of the fully-qualified font name or font specifier.
         * A value larger than 500 indicates bold font-weight.
         */
        val FONT_WEIGHT = 23
        /** The font is Type 1.
         */
        val FONT_TYPE_T1 = 0
        /** The font is True Type with a standard encoding.
         */
        val FONT_TYPE_TT = 1
        /** The font is CJK.
         */
        val FONT_TYPE_CJK = 2
        /** The font is True Type with a Unicode encoding.
         */
        val FONT_TYPE_TTUNI = 3
        /** A font already inside the document.
         */
        val FONT_TYPE_DOCUMENT = 4
        /** A Type3 font.
         */
        val FONT_TYPE_T3 = 5
        /** The Unicode encoding with horizontal writing.
         */
        val IDENTITY_H = "Identity-H"
        /** The Unicode encoding with vertical writing.
         */
        val IDENTITY_V = "Identity-V"

        /** A possible encoding.  */
        val CP1250 = "Cp1250"

        /** A possible encoding.  */
        val CP1252 = "Cp1252"

        /** A possible encoding.  */
        val CP1257 = "Cp1257"

        /** A possible encoding.  */
        val WINANSI = "Cp1252"

        /** A possible encoding.  */
        val MACROMAN = "MacRoman"

        val CHAR_RANGE_LATIN = intArrayOf(0, 0x17f, 0x2000, 0x206f, 0x20a0, 0x20cf, 0xfb00, 0xfb06)
        val CHAR_RANGE_ARABIC = intArrayOf(0, 0x7f, 0x0600, 0x067f, 0x20a0, 0x20cf, 0xfb50, 0xfbff, 0xfe70, 0xfeff)
        val CHAR_RANGE_HEBREW = intArrayOf(0, 0x7f, 0x0590, 0x05ff, 0x20a0, 0x20cf, 0xfb1d, 0xfb4f)
        val CHAR_RANGE_CYRILLIC = intArrayOf(0, 0x7f, 0x0400, 0x052f, 0x2000, 0x206f, 0x20a0, 0x20cf)

        /** default array of six numbers specifying the font matrix, mapping glyph space to text space  */
        val DEFAULT_FONT_MATRIX = doubleArrayOf(0.001, 0.0, 0.0, 0.001, 0.0, 0.0)

        /** if the font has to be embedded  */
        val EMBEDDED = true

        /** if the font doesn't have to be embedded  */
        val NOT_EMBEDDED = false
        /** if the font has to be cached  */
        val CACHED = true
        /** if the font doesn't have to be cached  */
        val NOT_CACHED = false

        /** The path to the font resources.  */
        val RESOURCE_PATH = "com/itextpdf/text/pdf/fonts/"
        /** The fake CID code that represents a newline.  */
        val CID_NEWLINE = '\u7fff'

        /**
         * Unicode Character 'PARAGRAPH SEPARATOR' (U+2029)
         * Treated as a line feed character in XFA rich and plain text.
         * @since 5.4.3
         */
        val PARAGRAPH_SEPARATOR = '\u2029'
        /** a not defined character in a custom PDF encoding  */
        val notdef = ".notdef"

        /** cache for the fonts already used.  */
        protected var fontCache = ConcurrentHashMap<String, BaseFont>()

        /** list of the 14 built in fonts.  */
        protected val BuiltinFonts14 = HashMap<String, PdfName>()

        init {
            BuiltinFonts14.put(COURIER, PdfName.COURIER)
            BuiltinFonts14.put(COURIER_BOLD, PdfName.COURIER_BOLD)
            BuiltinFonts14.put(COURIER_BOLDOBLIQUE, PdfName.COURIER_BOLDOBLIQUE)
            BuiltinFonts14.put(COURIER_OBLIQUE, PdfName.COURIER_OBLIQUE)
            BuiltinFonts14.put(HELVETICA, PdfName.HELVETICA)
            BuiltinFonts14.put(HELVETICA_BOLD, PdfName.HELVETICA_BOLD)
            BuiltinFonts14.put(HELVETICA_BOLDOBLIQUE, PdfName.HELVETICA_BOLDOBLIQUE)
            BuiltinFonts14.put(HELVETICA_OBLIQUE, PdfName.HELVETICA_OBLIQUE)
            BuiltinFonts14.put(SYMBOL, PdfName.SYMBOL)
            BuiltinFonts14.put(TIMES_ROMAN, PdfName.TIMES_ROMAN)
            BuiltinFonts14.put(TIMES_BOLD, PdfName.TIMES_BOLD)
            BuiltinFonts14.put(TIMES_BOLDITALIC, PdfName.TIMES_BOLDITALIC)
            BuiltinFonts14.put(TIMES_ITALIC, PdfName.TIMES_ITALIC)
            BuiltinFonts14.put(ZAPFDINGBATS, PdfName.ZAPFDINGBATS)
        }

        /**
         * Creates a new font. This font can be one of the 14 built in types,
         * a Type1 font referred to by an AFM or PFM file, a TrueType font (simple or collection) or a CJK font from the
         * Adobe Asian Font Pack. TrueType fonts and CJK fonts can have an optional style modifier
         * appended to the name. These modifiers are: Bold, Italic and BoldItalic. An
         * example would be "STSong-Light,Bold". Note that this modifiers do not work if
         * the font is embedded. Fonts in TrueType collections are addressed by index such as "msgothic.ttc,1".
         * This would get the second font (indexes start at 0), in this case "MS PGothic".
         *
         * The fonts are cached and if they already exist they are extracted from the cache,
         * not parsed again.
         *
         * Besides the common encodings described by name, custom encodings
         * can also be made. These encodings will only work for the single byte fonts
         * Type1 and TrueType. The encoding string starts with a '#'
         * followed by "simple" or "full". If "simple" there is a decimal for the first character position and then a list
         * of hex values representing the Unicode codes that compose that encoding.
         * The "simple" encoding is recommended for TrueType fonts
         * as the "full" encoding risks not matching the character with the right glyph
         * if not done with care.
         * The "full" encoding is specially aimed at Type1 fonts where the glyphs have to be
         * described by non standard names like the Tex math fonts. Each group of three elements
         * compose a code position: the one byte code order in decimal or as 'x' (x cannot be the space), the name and the Unicode character
         * used to access the glyph. The space must be assigned to character position 32 otherwise
         * text justification will not work.
         *
         * Example for a "simple" encoding that includes the Unicode
         * character space, A, B and ecyrillic:
         *
         * "# simple 32 0020 0041 0042 0454"
         *
         *
         * Example for a "full" encoding for a Type1 Tex font:
         *
         * "# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020"
         *
         *
         * This method calls:
         *
         * createFont(name, encoding, embedded, true, null, null);
         *
         * @param name the name of the font or its location on file
         * *
         * @param encoding the encoding to be applied to this font
         * *
         * @param embedded true if the font is to be embedded in the PDF
         * *
         * @param    forceRead    in some cases (TrueTypeFont, Type1Font), the full font file will be read and kept in memory if forceRead is true
         * *
         * @return returns a new font. This font may come from the cache
         * *
         * @throws DocumentException the font is invalid
         * *
         * @throws IOException the font file could not be read
         * *
         * @since    2.1.5
         */
        @Throws(DocumentException::class, IOException::class)
        fun createFont(name: String, encoding: String, embedded: Boolean, forceRead: Boolean): BaseFont {
            return createFont(name, encoding, embedded, true, null, null, forceRead)
        }

        /** Creates a new font. This font can be one of the 14 built in types,
         * a Type1 font referred to by an AFM or PFM file, a TrueType font (simple or collection) or a CJK font from the
         * Adobe Asian Font Pack. TrueType fonts and CJK fonts can have an optional style modifier
         * appended to the name. These modifiers are: Bold, Italic and BoldItalic. An
         * example would be "STSong-Light,Bold". Note that this modifiers do not work if
         * the font is embedded. Fonts in TrueType collections are addressed by index such as "msgothic.ttc,1".
         * This would get the second font (indexes start at 0), in this case "MS PGothic".
         *
         * The fonts may or may not be cached depending on the flag cached.
         * If the byte arrays are present the font will be
         * read from them instead of the name. A name is still required to identify
         * the font type.
         *
         * Besides the common encodings described by name, custom encodings
         * can also be made. These encodings will only work for the single byte fonts
         * Type1 and TrueType. The encoding string starts with a '#'
         * followed by "simple" or "full". If "simple" there is a decimal for the first character position and then a list
         * of hex values representing the Unicode codes that compose that encoding.
         * The "simple" encoding is recommended for TrueType fonts
         * as the "full" encoding risks not matching the character with the right glyph
         * if not done with care.
         * The "full" encoding is specially aimed at Type1 fonts where the glyphs have to be
         * described by non standard names like the Tex math fonts. Each group of three elements
         * compose a code position: the one byte code order in decimal or as 'x' (x cannot be the space), the name and the Unicode character
         * used to access the glyph. The space must be assigned to character position 32 otherwise
         * text justification will not work.
         *
         * Example for a "simple" encoding that includes the Unicode
         * character space, A, B and ecyrillic:
         *
         * "# simple 32 0020 0041 0042 0454"
         *
         *
         * Example for a "full" encoding for a Type1 Tex font:
         *
         * "# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020"
         *
         * @param name the name of the font or its location on file
         * *
         * @param encoding the encoding to be applied to this font
         * *
         * @param embedded true if the font is to be embedded in the PDF
         * *
         * @param cached true if the font comes from the cache or is added to
         * * the cache if new, false if the font is always created new
         * *
         * @param ttfAfm the true type font or the afm in a byte array
         * *
         * @param pfb the pfb in a byte array
         * *
         * @param noThrow if true will not throw an exception if the font is not recognized and will return null, if false will throw
         * * an exception if the font is not recognized. Note that even if true an exception may be thrown in some circumstances.
         * * This parameter is useful for FontFactory that may have to check many invalid font names before finding the right one
         * *
         * @param    forceRead    in some cases (TrueTypeFont, Type1Font), the full font file will be read and kept in memory if forceRead is true
         * *
         * @return returns a new font. This font may come from the cache but only if cached
         * * is true, otherwise it will always be created new
         * *
         * @throws DocumentException the font is invalid
         * *
         * @throws IOException the font file could not be read
         * *
         * @since    2.1.5
         */
        @Throws(DocumentException::class, IOException::class)
        @JvmOverloads fun createFont(name: String = BaseFont.HELVETICA, encoding: String = BaseFont.WINANSI, embedded: Boolean = BaseFont.NOT_EMBEDDED, cached: Boolean = true, ttfAfm: ByteArray = null, pfb: ByteArray = null, noThrow: Boolean = false, forceRead: Boolean = false): BaseFont? {
            var encoding = encoding
            var embedded = embedded
            val nameBase = getBaseName(name)
            encoding = normalizeEncoding(encoding)
            val isBuiltinFonts14 = BuiltinFonts14.containsKey(name)
            val isCJKFont = if (isBuiltinFonts14) false else CJKFont.isCJKFont(nameBase, encoding)
            if (isBuiltinFonts14 || isCJKFont)
                embedded = false
            else if (encoding == IDENTITY_H || encoding == IDENTITY_V)
                embedded = true
            var fontFound: BaseFont? = null
            var fontBuilt: BaseFont? = null
            val key = name + "\n" + encoding + "\n" + embedded
            if (cached) {
                fontFound = fontCache[key]
                if (fontFound != null)
                    return fontFound
            }
            if (isBuiltinFonts14 || name.toLowerCase().endsWith(".afm") || name.toLowerCase().endsWith(".pfm")) {
                fontBuilt = Type1Font(name, encoding, embedded, ttfAfm, pfb, forceRead)
                fontBuilt.fastWinansi = encoding == CP1252
            } else if (nameBase.toLowerCase().endsWith(".ttf") || nameBase.toLowerCase().endsWith(".otf") || nameBase.toLowerCase().indexOf(".ttc,") > 0) {
                if (encoding == IDENTITY_H || encoding == IDENTITY_V)
                    fontBuilt = TrueTypeFontUnicode(name, encoding, embedded, ttfAfm, forceRead)
                else {
                    fontBuilt = TrueTypeFont(name, encoding, embedded, ttfAfm, false, forceRead)
                    fontBuilt.fastWinansi = encoding == CP1252
                }
            } else if (isCJKFont)
                fontBuilt = CJKFont(name, encoding, embedded)
            else if (noThrow)
                return null
            else
                throw DocumentException(MessageLocalization.getComposedMessage("font.1.with.2.is.not.recognized", name, encoding))
            if (cached) {
                fontFound = fontCache[key]
                if (fontFound != null)
                    return fontFound
                fontCache.putIfAbsent(key, fontBuilt)
            }
            return fontBuilt
        }

        /**
         * Creates a font based on an existing document font. The created font font may not
         * behave as expected, depending on the encoding or subset.
         * @param fontRef the reference to the document font
         * *
         * @return the font
         */
        fun createFont(fontRef: PRIndirectReference): BaseFont {
            return DocumentFont(fontRef)
        }

        /**
         * Gets the name without the modifiers Bold, Italic or BoldItalic.
         * @param name the full name of the font
         * *
         * @return the name without the modifiers Bold, Italic or BoldItalic
         */
        protected fun getBaseName(name: String): String {
            if (name.endsWith(",Bold"))
                return name.substring(0, name.length - 5)
            else if (name.endsWith(",Italic"))
                return name.substring(0, name.length - 7)
            else if (name.endsWith(",BoldItalic"))
                return name.substring(0, name.length - 11)
            else
                return name
        }

        /**
         * Normalize the encoding names. "winansi" is changed to "Cp1252" and
         * "macroman" is changed to "MacRoman".
         * @param enc the encoding to be normalized
         * *
         * @return the normalized encoding
         */
        protected fun normalizeEncoding(enc: String): String {
            if (enc == "winansi" || enc == "")
                return CP1252
            else if (enc == "macroman")
                return MACROMAN
            else
                return enc
        }

        /** Creates a unique subset prefix to be added to the font name when the font is embedded and subset.
         * @return the subset prefix
         */
        fun createSubsetPrefix(): String {
            val s = StringBuilder("")
            for (k in 0..5)
                s.append(Math.random() * 26 + 'A'.0).toChar())
            return s + "+"
        }

        /** Gets the full name of the font. If it is a True Type font
         * each array element will have {Platform ID, Platform Encoding ID,
         * Language ID, font name}. The interpretation of this values can be
         * found in the Open Type specification, chapter 2, in the 'name' table.
         * For the other fonts the array has a single element with {"", "", "",
         * font name}.
         * @param name the name of the font
         * *
         * @param encoding the encoding of the font
         * *
         * @param ttfAfm the true type font or the afm in a byte array
         * *
         * @throws DocumentException on error
         * *
         * @throws IOException on error
         * *
         * @return the full name of the font
         */
        @Throws(DocumentException::class, IOException::class)
        fun getFullFontName(name: String, encoding: String, ttfAfm: ByteArray): Array<Array<String>> {
            val nameBase = getBaseName(name)
            var fontBuilt: BaseFont? = null
            if (nameBase.toLowerCase().endsWith(".ttf") || nameBase.toLowerCase().endsWith(".otf") || nameBase.toLowerCase().indexOf(".ttc,") > 0)
                fontBuilt = TrueTypeFont(name, CP1252, false, ttfAfm, true, false)
            else
                fontBuilt = createFont(name, encoding, false, false, ttfAfm, null)
            return fontBuilt!!.fullFontName
        }

        /** Gets all the names from the font. Only the required tables are read.
         * @param name the name of the font
         * *
         * @param encoding the encoding of the font
         * *
         * @param ttfAfm the true type font or the afm in a byte array
         * *
         * @throws DocumentException on error
         * *
         * @throws IOException on error
         * *
         * @return an array of Object[] built with {getPostscriptFontName(), getFamilyFontName(), getFullFontName()}
         */
        @Throws(DocumentException::class, IOException::class)
        fun getAllFontNames(name: String, encoding: String, ttfAfm: ByteArray): Array<Any> {
            val nameBase = getBaseName(name)
            var fontBuilt: BaseFont? = null
            if (nameBase.toLowerCase().endsWith(".ttf") || nameBase.toLowerCase().endsWith(".otf") || nameBase.toLowerCase().indexOf(".ttc,") > 0)
                fontBuilt = TrueTypeFont(name, CP1252, false, ttfAfm, true, false)
            else
                fontBuilt = createFont(name, encoding, false, false, ttfAfm, null)
            return arrayOf(fontBuilt!!.postscriptFontName, fontBuilt.familyFontName, fontBuilt.fullFontName)
        }

        /** Gets all the entries of the namestable from the font. Only the required tables are read.
         * @param name the name of the font
         * *
         * @param encoding the encoding of the font
         * *
         * @param ttfAfm the true type font or the afm in a byte array
         * *
         * @throws DocumentException on error
         * *
         * @throws IOException on error
         * *
         * @return an array of Object[] built with {getPostscriptFontName(), getFamilyFontName(), getFullFontName()}
         * *
         * @since 2.0.8
         */
        @Throws(DocumentException::class, IOException::class)
        fun getAllNameEntries(name: String, encoding: String, ttfAfm: ByteArray): Array<Array<String>> {
            val nameBase = getBaseName(name)
            var fontBuilt: BaseFont? = null
            if (nameBase.toLowerCase().endsWith(".ttf") || nameBase.toLowerCase().endsWith(".otf") || nameBase.toLowerCase().indexOf(".ttc,") > 0)
                fontBuilt = TrueTypeFont(name, CP1252, false, ttfAfm, true, false)
            else
                fontBuilt = createFont(name, encoding, false, false, ttfAfm, null)
            return fontBuilt!!.allNameEntries
        }

        /** Enumerates the postscript font names present inside a
         * True Type Collection.
         * @param ttcFile the file name of the font
         * *
         * @throws DocumentException on error
         * *
         * @throws IOException on error
         * *
         * @return the postscript font names
         */
        @Throws(DocumentException::class, IOException::class)
        fun enumerateTTCNames(ttcFile: String): Array<String> {
            return EnumerateTTC(ttcFile).getNames()
        }

        /** Enumerates the postscript font names present inside a
         * True Type Collection.
         * @param ttcArray the font as a byte array
         * *
         * @throws DocumentException on error
         * *
         * @throws IOException on error
         * *
         * @return the postscript font names
         */
        @Throws(DocumentException::class, IOException::class)
        fun enumerateTTCNames(ttcArray: ByteArray): Array<String> {
            return EnumerateTTC(ttcArray).getNames()
        }

        private fun addFont(fontRef: PRIndirectReference, hits: IntHashtable, fonts: ArrayList<Array<Any>>) {
            val obj = PdfReader.getPdfObject(fontRef)
            if (obj == null || !obj.isDictionary)
                return
            val font = obj as PdfDictionary?
            val subtype = font.getAsName(PdfName.SUBTYPE)
            if (PdfName.TYPE1 != subtype && PdfName.TRUETYPE != subtype && PdfName.TYPE0 != subtype)
                return
            val name = font.getAsName(PdfName.BASEFONT)
            fonts.add(arrayOf<Any>(PdfName.decodeName(name.toString()), fontRef))
            hits.put(fontRef.getNumber(), 1)
        }

        private fun recourseFonts(page: PdfDictionary?, hits: IntHashtable, fonts: ArrayList<Array<Any>>, level: Int, visitedResources: HashSet<PdfDictionary>) {
            var level = level
            ++level
            if (level > 50)
            // in case we have an endless loop
                return
            if (page == null)
                return
            val resources = page.getAsDict(PdfName.RESOURCES) ?: return
            val font = resources.getAsDict(PdfName.FONT)
            if (font != null) {
                for (key in font.keys) {
                    val ft = font.get(key)
                    if (ft == null || !ft.isIndirect)
                        continue
                    val hit = (ft as PRIndirectReference).getNumber()
                    if (hits.containsKey(hit))
                        continue
                    addFont(ft as PRIndirectReference?, hits, fonts)
                }
            }
            val xobj = resources.getAsDict(PdfName.XOBJECT)
            if (xobj != null) {
                if (visitedResources.add(xobj)) {
                    for (key in xobj.keys) {
                        val po = xobj.getDirectObject(key)
                        if (po is PdfDictionary)
                            recourseFonts(po, hits, fonts, level, visitedResources)
                    }
                    visitedResources.remove(xobj)
                } else
                    throw ExceptionConverter(InvalidPdfException(MessageLocalization.getComposedMessage("illegal.resources.tree")))
            }
        }

        /**
         * Gets a list of all document fonts. Each element of the ArrayList
         * contains a Object[]{String,PRIndirectReference} with the font name
         * and the indirect reference to it.
         * @param reader the document where the fonts are to be listed from
         * *
         * @return the list of fonts and references
         */
        fun getDocumentFonts(reader: PdfReader): ArrayList<Array<Any>> {
            val hits = IntHashtable()
            val fonts = ArrayList<Array<Any>>()
            val npages = reader.numberOfPages
            for (k in 1..npages)
                recourseFonts(reader.getPageN(k), hits, fonts, 1, HashSet<PdfDictionary>())
            return fonts
        }

        /**
         * Gets a list of the document fonts in a particular page. Each element of the ArrayList
         * contains a Object[]{String,PRIndirectReference} with the font name
         * and the indirect reference to it.
         * @param reader the document where the fonts are to be listed from
         * *
         * @param page the page to list the fonts from
         * *
         * @return the list of fonts and references
         */
        fun getDocumentFonts(reader: PdfReader, page: Int): ArrayList<Array<Any>> {
            val hits = IntHashtable()
            val fonts = ArrayList<Array<Any>>()
            recourseFonts(reader.getPageN(page), hits, fonts, 1, HashSet<PdfDictionary>())
            return fonts
        }
    }


}
/**
 * Creates a new font. This will always be the default Helvetica font (not embedded).
 * This method is introduced because Helvetica is used in many examples.
 * @return    a BaseFont object (Helvetica, Winansi, not embedded)
 * *
 * @throws    IOException            This shouldn't occur ever
 * *
 * @throws    DocumentException    This shouldn't occur ever
 * *
 * @since    2.1.1
 */
/**
 * Creates a new font. This font can be one of the 14 built in types,
 * a Type1 font referred to by an AFM or PFM file, a TrueType font (simple or collection) or a CJK font from the
 * Adobe Asian Font Pack. TrueType fonts and CJK fonts can have an optional style modifier
 * appended to the name. These modifiers are: Bold, Italic and BoldItalic. An
 * example would be "STSong-Light,Bold". Note that this modifiers do not work if
 * the font is embedded. Fonts in TrueType collections are addressed by index such as "msgothic.ttc,1".
 * This would get the second font (indexes start at 0), in this case "MS PGothic".
 *
 * The fonts are cached and if they already exist they are extracted from the cache,
 * not parsed again.
 *
 * Besides the common encodings described by name, custom encodings
 * can also be made. These encodings will only work for the single byte fonts
 * Type1 and TrueType. The encoding string starts with a '#'
 * followed by "simple" or "full". If "simple" there is a decimal for the first character position and then a list
 * of hex values representing the Unicode codes that compose that encoding.
 * The "simple" encoding is recommended for TrueType fonts
 * as the "full" encoding risks not matching the character with the right glyph
 * if not done with care.
 * The "full" encoding is specially aimed at Type1 fonts where the glyphs have to be
 * described by non standard names like the Tex math fonts. Each group of three elements
 * compose a code position: the one byte code order in decimal or as 'x' (x cannot be the space), the name and the Unicode character
 * used to access the glyph. The space must be assigned to character position 32 otherwise
 * text justification will not work.
 *
 * Example for a "simple" encoding that includes the Unicode
 * character space, A, B and ecyrillic:
 *
 * "# simple 32 0020 0041 0042 0454"
 *
 *
 * Example for a "full" encoding for a Type1 Tex font:
 *
 * "# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020"
 *
 *
 * This method calls:
 *
 * createFont(name, encoding, embedded, true, null, null);
 *
 * @param name the name of the font or its location on file
 * *
 * @param encoding the encoding to be applied to this font
 * *
 * @param embedded true if the font is to be embedded in the PDF
 * *
 * @return returns a new font. This font may come from the cache
 * *
 * @throws DocumentException the font is invalid
 * *
 * @throws IOException the font file could not be read
 */
/** Creates a new font. This font can be one of the 14 built in types,
 * a Type1 font referred to by an AFM or PFM file, a TrueType font (simple or collection) or a CJK font from the
 * Adobe Asian Font Pack. TrueType fonts and CJK fonts can have an optional style modifier
 * appended to the name. These modifiers are: Bold, Italic and BoldItalic. An
 * example would be "STSong-Light,Bold". Note that this modifiers do not work if
 * the font is embedded. Fonts in TrueType collections are addressed by index such as "msgothic.ttc,1".
 * This would get the second font (indexes start at 0), in this case "MS PGothic".
 *
 * The fonts may or may not be cached depending on the flag cached.
 * If the byte arrays are present the font will be
 * read from them instead of the name. A name is still required to identify
 * the font type.
 *
 * Besides the common encodings described by name, custom encodings
 * can also be made. These encodings will only work for the single byte fonts
 * Type1 and TrueType. The encoding string starts with a '#'
 * followed by "simple" or "full". If "simple" there is a decimal for the first character position and then a list
 * of hex values representing the Unicode codes that compose that encoding.
 * The "simple" encoding is recommended for TrueType fonts
 * as the "full" encoding risks not matching the character with the right glyph
 * if not done with care.
 * The "full" encoding is specially aimed at Type1 fonts where the glyphs have to be
 * described by non standard names like the Tex math fonts. Each group of three elements
 * compose a code position: the one byte code order in decimal or as 'x' (x cannot be the space), the name and the Unicode character
 * used to access the glyph. The space must be assigned to character position 32 otherwise
 * text justification will not work.
 *
 * Example for a "simple" encoding that includes the Unicode
 * character space, A, B and ecyrillic:
 *
 * "# simple 32 0020 0041 0042 0454"
 *
 *
 * Example for a "full" encoding for a Type1 Tex font:
 *
 * "# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020"
 *
 * @param name the name of the font or its location on file
 * *
 * @param encoding the encoding to be applied to this font
 * *
 * @param embedded true if the font is to be embedded in the PDF
 * *
 * @param cached true if the font comes from the cache or is added to
 * * the cache if new, false if the font is always created new
 * *
 * @param ttfAfm the true type font or the afm in a byte array
 * *
 * @param pfb the pfb in a byte array
 * *
 * @return returns a new font. This font may come from the cache but only if cached
 * * is true, otherwise it will always be created new
 * *
 * @throws DocumentException the font is invalid
 * *
 * @throws IOException the font file could not be read
 * *
 * @since    iText 0.80
 */
/** Creates a new font. This font can be one of the 14 built in types,
 * a Type1 font referred to by an AFM or PFM file, a TrueType font (simple or collection) or a CJK font from the
 * Adobe Asian Font Pack. TrueType fonts and CJK fonts can have an optional style modifier
 * appended to the name. These modifiers are: Bold, Italic and BoldItalic. An
 * example would be "STSong-Light,Bold". Note that this modifiers do not work if
 * the font is embedded. Fonts in TrueType collections are addressed by index such as "msgothic.ttc,1".
 * This would get the second font (indexes start at 0), in this case "MS PGothic".
 *
 * The fonts may or may not be cached depending on the flag cached.
 * If the byte arrays are present the font will be
 * read from them instead of the name. A name is still required to identify
 * the font type.
 *
 * Besides the common encodings described by name, custom encodings
 * can also be made. These encodings will only work for the single byte fonts
 * Type1 and TrueType. The encoding string starts with a '#'
 * followed by "simple" or "full". If "simple" there is a decimal for the first character position and then a list
 * of hex values representing the Unicode codes that compose that encoding.
 * The "simple" encoding is recommended for TrueType fonts
 * as the "full" encoding risks not matching the character with the right glyph
 * if not done with care.
 * The "full" encoding is specially aimed at Type1 fonts where the glyphs have to be
 * described by non standard names like the Tex math fonts. Each group of three elements
 * compose a code position: the one byte code order in decimal or as 'x' (x cannot be the space), the name and the Unicode character
 * used to access the glyph. The space must be assigned to character position 32 otherwise
 * text justification will not work.
 *
 * Example for a "simple" encoding that includes the Unicode
 * character space, A, B and ecyrillic:
 *
 * "# simple 32 0020 0041 0042 0454"
 *
 *
 * Example for a "full" encoding for a Type1 Tex font:
 *
 * "# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020"
 *
 * @param name the name of the font or its location on file
 * *
 * @param encoding the encoding to be applied to this font
 * *
 * @param embedded true if the font is to be embedded in the PDF
 * *
 * @param cached true if the font comes from the cache or is added to
 * * the cache if new, false if the font is always created new
 * *
 * @param ttfAfm the true type font or the afm in a byte array
 * *
 * @param pfb the pfb in a byte array
 * *
 * @param noThrow if true will not throw an exception if the font is not recognized and will return null, if false will throw
 * * an exception if the font is not recognized. Note that even if true an exception may be thrown in some circumstances.
 * * This parameter is useful for FontFactory that may have to check many invalid font names before finding the right one
 * *
 * @return returns a new font. This font may come from the cache but only if cached
 * * is true, otherwise it will always be created new
 * *
 * @throws DocumentException the font is invalid
 * *
 * @throws IOException the font file could not be read
 * *
 * @since    2.0.3
 */
