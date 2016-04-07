/*
 * $Id: 1caf4a4b156c638a902612102197a99bd613b105 $
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

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

/**
 * Reads a Truetype font

 * @author Paulo Soares
 */
internal open class TrueTypeFont : BaseFont {

    protected var justNames = false
    /**
     * Contains the location of the several tables. The key is the name of
     * the table and the value is an int[2] where position 0
     * is the offset from the start of the file and position 1 is the length
     * of the table.
     */
    protected var tables: HashMap<String, IntArray>
    /**
     * The file in use.
     */
    protected var rf: RandomAccessFileOrArray? = null
    /**
     * The file name.
     */
    protected var fileName: String

    protected var cff = false

    protected var cffOffset: Int = 0

    protected var cffLength: Int = 0

    /**
     * The offset from the start of the file to the table directory.
     * It is 0 for TTF and may vary for TTC depending on the chosen font.
     */
    protected var directoryOffset: Int = 0
    /**
     * The index for the TTC font. It is an empty String for a
     * TTF file.
     */
    protected var ttcIndex: String
    /**
     * The style modifier
     */
    protected var style = ""
    /**
     * The content of table 'head'.
     */
    protected var head = FontHeader()
    /**
     * The content of table 'hhea'.
     */
    protected var hhea = HorizontalHeader()
    /**
     * The content of table 'OS/2'.
     */
    protected var os_2 = WindowsMetrics()
    /**
     * The width of the glyphs. This is essentially the content of table
     * 'hmtx' normalized to 1000 units.
     */
    protected var glyphWidthsByIndex: IntArray

    protected var bboxes: Array<IntArray>? = null
    /**
     * The map containing the code information for the table 'cmap', encoding 1.0.
     * The key is the code and the value is an int[2] where position 0
     * is the glyph number and position 1 is the glyph width normalized to 1000
     * units.
     */
    protected var cmap10: HashMap<Int, IntArray>? = null
    /**
     * The map containing the code information for the table 'cmap', encoding 3.1
     * in Unicode.
     *
     *
     * The key is the code and the value is an int[2] where position 0
     * is the glyph number and position 1 is the glyph width normalized to 1000
     * units.
     */
    protected var cmap31: HashMap<Int, IntArray>? = null

    protected var cmapExt: HashMap<Int, IntArray>? = null

    protected var glyphIdToChar: IntArray

    protected var maxGlyphId: Int = 0

    /**
     * The map containing the kerning information. It represents the content of
     * table 'kern'. The key is an Integer where the top 16 bits
     * are the glyph number for the first character and the lower 16 bits are the
     * glyph number for the second character. The value is the amount of kerning in
     * normalized 1000 units as an Integer. This value is usually negative.
     */
    protected var kerning = IntHashtable()

    /**
     * The font name.
     * This name is usually extracted from the table 'name' with
     * the 'Name ID' 6.
     */
    /**
     * Gets the postscript font name.

     * @return the postscript font name
     */
    /**
     * Sets the font name that will appear in the pdf font dictionary.
     * Use with care as it can easily make a font unreadable if not embedded.

     * @param name the new font name
     */
    override var postscriptFontName: String

    /**
     * The font subfamily
     * This subFamily name is usually extracted from the table 'name' with
     * the 'Name ID' 2 or 'Name ID' 17.
     */
    protected var subFamily: Array<Array<String>>? = null

    /**
     * The full name of the font 'Name ID' 1 or 'Name ID' 16
     */
    /**
     * Gets the full name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.

     * @return the full name of the font
     */
    override var fullFontName: Array<Array<String>>
        protected set(value: Array<Array<String>>) {
            super.fullFontName = value
        }

    /**
     * All the names of the Names-Table
     */
    /**
     * Gets all the entries of the Names-Table. If it is a True Type font
     * each array element will have {Name ID, Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.

     * @return the full name of the font
     */
    override var allNameEntries: Array<Array<String>>
        protected set(value: Array<Array<String>>) {
            super.allNameEntries = value
        }

    /**
     * The family name of the font
     */
    /**
     * Gets the family name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.

     * @return the family name of the font
     */
    override var familyFontName: Array<Array<String>>
        protected set(value: Array<Array<String>>) {
            super.familyFontName = value
        }
    /**
     * The italic angle. It is usually extracted from the 'post' table or in it's
     * absence with the code:
     *
     *
     *
     * -Math.atan2(hhea.caretSlopeRun, hhea.caretSlopeRise) * 180 / Math.PI
     *
     */
    protected var italicAngle: Double = 0.toDouble()
    /**
     * true if all the glyphs have the same width.
     */
    protected var isFixedPitch = false

    protected var underlinePosition: Int = 0

    protected var underlineThickness: Int = 0

    /**
     * The components of table 'head'.
     */
    protected class FontHeader {
        /**
         * A variable.
         */
        internal var flags: Int = 0
        /**
         * A variable.
         */
        internal var unitsPerEm: Int = 0
        /**
         * A variable.
         */
        internal var xMin: Short = 0
        /**
         * A variable.
         */
        internal var yMin: Short = 0
        /**
         * A variable.
         */
        internal var xMax: Short = 0
        /**
         * A variable.
         */
        internal var yMax: Short = 0
        /**
         * A variable.
         */
        internal var macStyle: Int = 0
    }

    /**
     * The components of table 'hhea'.
     */
    protected class HorizontalHeader {
        /**
         * A variable.
         */
        internal var Ascender: Short = 0
        /**
         * A variable.
         */
        internal var Descender: Short = 0
        /**
         * A variable.
         */
        internal var LineGap: Short = 0
        /**
         * A variable.
         */
        internal var advanceWidthMax: Int = 0
        /**
         * A variable.
         */
        internal var minLeftSideBearing: Short = 0
        /**
         * A variable.
         */
        internal var minRightSideBearing: Short = 0
        /**
         * A variable.
         */
        internal var xMaxExtent: Short = 0
        /**
         * A variable.
         */
        internal var caretSlopeRise: Short = 0
        /**
         * A variable.
         */
        internal var caretSlopeRun: Short = 0
        /**
         * A variable.
         */
        internal var numberOfHMetrics: Int = 0
    }

    /**
     * The components of table 'OS/2'.
     */
    protected class WindowsMetrics {
        /**
         * A variable.
         */
        internal var xAvgCharWidth: Short = 0
        /**
         * A variable.
         */
        internal var usWeightClass: Int = 0
        /**
         * A variable.
         */
        internal var usWidthClass: Int = 0
        /**
         * A variable.
         */
        internal var fsType: Short = 0
        /**
         * A variable.
         */
        internal var ySubscriptXSize: Short = 0
        /**
         * A variable.
         */
        internal var ySubscriptYSize: Short = 0
        /**
         * A variable.
         */
        internal var ySubscriptXOffset: Short = 0
        /**
         * A variable.
         */
        internal var ySubscriptYOffset: Short = 0
        /**
         * A variable.
         */
        internal var ySuperscriptXSize: Short = 0
        /**
         * A variable.
         */
        internal var ySuperscriptYSize: Short = 0
        /**
         * A variable.
         */
        internal var ySuperscriptXOffset: Short = 0
        /**
         * A variable.
         */
        internal var ySuperscriptYOffset: Short = 0
        /**
         * A variable.
         */
        internal var yStrikeoutSize: Short = 0
        /**
         * A variable.
         */
        internal var yStrikeoutPosition: Short = 0
        /**
         * A variable.
         */
        internal var sFamilyClass: Short = 0
        /**
         * A variable.
         */
        internal var panose = ByteArray(10)
        /**
         * A variable.
         */
        internal var achVendID = ByteArray(4)
        /**
         * A variable.
         */
        internal var fsSelection: Int = 0
        /**
         * A variable.
         */
        internal var usFirstCharIndex: Int = 0
        /**
         * A variable.
         */
        internal var usLastCharIndex: Int = 0
        /**
         * A variable.
         */
        internal var sTypoAscender: Short = 0
        /**
         * A variable.
         */
        internal var sTypoDescender: Short = 0
        /**
         * A variable.
         */
        internal var sTypoLineGap: Short = 0
        /**
         * A variable.
         */
        internal var usWinAscent: Int = 0
        /**
         * A variable.
         */
        internal var usWinDescent: Int = 0
        /**
         * A variable.
         */
        internal var ulCodePageRange1: Int = 0
        /**
         * A variable.
         */
        internal var ulCodePageRange2: Int = 0
        /**
         * A variable.
         */
        internal var sCapHeight: Int = 0
    }

    /**
     * This constructor is present to allow extending the class.
     */
    protected constructor() {
    }

    /**
     * Creates a new TrueType font.

     * @param ttFile the location of the font on file. The file must end in '.ttf' or
     * *               '.ttc' but can have modifiers after the name
     * *
     * @param enc    the encoding to be applied to this font
     * *
     * @param emb    true if the font is to be embedded in the PDF
     * *
     * @param ttfAfm the font as a byte array
     * *
     * @throws DocumentException the font is invalid
     * *
     * @throws IOException       the font file could not be read
     * *
     * @since 2.1.5
     */
    @Throws(DocumentException::class, IOException::class)
    constructor(ttFile: String, enc: String, emb: Boolean, ttfAfm: ByteArray, justNames: Boolean, forceRead: Boolean) {
        this.justNames = justNames
        val nameBase = getBaseName(ttFile)
        val ttcName = getTTCName(nameBase)
        if (nameBase.length < ttFile.length) {
            style = ttFile.substring(nameBase.length)
        }
        encoding = enc
        embedded = emb
        fileName = ttcName
        fontType = BaseFont.FONT_TYPE_TT
        ttcIndex = ""
        if (ttcName.length < nameBase.length)
            ttcIndex = nameBase.substring(ttcName.length + 1)
        if (fileName.toLowerCase().endsWith(".ttf") || fileName.toLowerCase().endsWith(".otf") || fileName.toLowerCase().endsWith(".ttc")) {
            process(ttfAfm, forceRead)
            if (!justNames && embedded && os_2.fsType.toInt() == 2)
                throw DocumentException(MessageLocalization.getComposedMessage("1.cannot.be.embedded.due.to.licensing.restrictions", fileName + style))
        } else
            throw DocumentException(MessageLocalization.getComposedMessage("1.is.not.a.ttf.otf.or.ttc.font.file", fileName + style))
        if (!encoding.startsWith("#"))
            PdfEncodings.convertToBytes(" ", enc) // check if the encoding exists
        createEncoding()
    }


    /**
     * Reads the tables 'head', 'hhea', 'OS/2', 'post' and 'maxp' filling several variables.

     * @throws DocumentException the font is invalid
     * *
     * @throws IOException       the font file could not be read
     */
    @Throws(DocumentException::class, IOException::class)
    fun fillTables() {
        var table_location: IntArray?
        table_location = tables["head"]
        if (table_location == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "head", fileName + style))
        rf!!.seek((table_location[0] + 16).toLong())
        head.flags = rf!!.readUnsignedShort()
        head.unitsPerEm = rf!!.readUnsignedShort()
        rf!!.skipBytes(16)
        head.xMin = rf!!.readShort()
        head.yMin = rf!!.readShort()
        head.xMax = rf!!.readShort()
        head.yMax = rf!!.readShort()
        head.macStyle = rf!!.readUnsignedShort()

        table_location = tables["hhea"]
        if (table_location == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "hhea", fileName + style))
        rf!!.seek((table_location[0] + 4).toLong())
        hhea.Ascender = rf!!.readShort()
        hhea.Descender = rf!!.readShort()
        hhea.LineGap = rf!!.readShort()
        hhea.advanceWidthMax = rf!!.readUnsignedShort()
        hhea.minLeftSideBearing = rf!!.readShort()
        hhea.minRightSideBearing = rf!!.readShort()
        hhea.xMaxExtent = rf!!.readShort()
        hhea.caretSlopeRise = rf!!.readShort()
        hhea.caretSlopeRun = rf!!.readShort()
        rf!!.skipBytes(12)
        hhea.numberOfHMetrics = rf!!.readUnsignedShort()

        table_location = tables["OS/2"]
        if (table_location != null) {
            rf!!.seek(table_location[0].toLong())
            val version = rf!!.readUnsignedShort()
            os_2.xAvgCharWidth = rf!!.readShort()
            os_2.usWeightClass = rf!!.readUnsignedShort()
            os_2.usWidthClass = rf!!.readUnsignedShort()
            os_2.fsType = rf!!.readShort()
            os_2.ySubscriptXSize = rf!!.readShort()
            os_2.ySubscriptYSize = rf!!.readShort()
            os_2.ySubscriptXOffset = rf!!.readShort()
            os_2.ySubscriptYOffset = rf!!.readShort()
            os_2.ySuperscriptXSize = rf!!.readShort()
            os_2.ySuperscriptYSize = rf!!.readShort()
            os_2.ySuperscriptXOffset = rf!!.readShort()
            os_2.ySuperscriptYOffset = rf!!.readShort()
            os_2.yStrikeoutSize = rf!!.readShort()
            os_2.yStrikeoutPosition = rf!!.readShort()
            os_2.sFamilyClass = rf!!.readShort()
            rf!!.readFully(os_2.panose)
            rf!!.skipBytes(16)
            rf!!.readFully(os_2.achVendID)
            os_2.fsSelection = rf!!.readUnsignedShort()
            os_2.usFirstCharIndex = rf!!.readUnsignedShort()
            os_2.usLastCharIndex = rf!!.readUnsignedShort()
            os_2.sTypoAscender = rf!!.readShort()
            os_2.sTypoDescender = rf!!.readShort()
            if (os_2.sTypoDescender > 0)
                os_2.sTypoDescender = (-os_2.sTypoDescender).toShort()
            os_2.sTypoLineGap = rf!!.readShort()
            os_2.usWinAscent = rf!!.readUnsignedShort()
            os_2.usWinDescent = rf!!.readUnsignedShort()
            os_2.ulCodePageRange1 = 0
            os_2.ulCodePageRange2 = 0
            if (version > 0) {
                os_2.ulCodePageRange1 = rf!!.readInt()
                os_2.ulCodePageRange2 = rf!!.readInt()
            }
            if (version > 1) {
                rf!!.skipBytes(2)
                os_2.sCapHeight = rf!!.readShort().toInt()
            } else
                os_2.sCapHeight = (0.7 * head.unitsPerEm).toInt()
        } else if (tables["hhea"] != null && tables["head"] != null) {

            if (head.macStyle == 0) {
                os_2.usWeightClass = 700
                os_2.usWidthClass = 5
            } else if (head.macStyle == 5) {
                os_2.usWeightClass = 400
                os_2.usWidthClass = 3
            } else if (head.macStyle == 6) {
                os_2.usWeightClass = 400
                os_2.usWidthClass = 7
            } else {
                os_2.usWeightClass = 400
                os_2.usWidthClass = 5
            }
            os_2.fsType = 0

            os_2.ySubscriptYSize = 0
            os_2.ySubscriptYOffset = 0
            os_2.ySuperscriptYSize = 0
            os_2.ySuperscriptYOffset = 0
            os_2.yStrikeoutSize = 0
            os_2.yStrikeoutPosition = 0
            os_2.sTypoAscender = (hhea.Ascender - 0.21 * hhea.Ascender).toShort()
            os_2.sTypoDescender = (-(Math.abs(hhea.Descender.toInt()) - Math.abs(hhea.Descender.toInt()) * 0.07)).toShort()
            os_2.sTypoLineGap = (hhea.LineGap * 2).toShort()
            os_2.usWinAscent = hhea.Ascender.toInt()
            os_2.usWinDescent = hhea.Descender.toInt()
            os_2.ulCodePageRange1 = 0
            os_2.ulCodePageRange2 = 0
            os_2.sCapHeight = (0.7 * head.unitsPerEm).toInt()
        }

        table_location = tables["post"]
        if (table_location == null) {
            italicAngle = -Math.atan2(hhea.caretSlopeRun.toDouble(), hhea.caretSlopeRise.toDouble()) * 180 / Math.PI
        } else {
            rf!!.seek((table_location[0] + 4).toLong())
            val mantissa = rf!!.readShort()
            val fraction = rf!!.readUnsignedShort()
            italicAngle = mantissa + fraction / 16384.0
            underlinePosition = rf!!.readShort().toInt()
            underlineThickness = rf!!.readShort().toInt()
            isFixedPitch = rf!!.readInt() != 0
        }

        table_location = tables["maxp"]
        if (table_location == null) {
            maxGlyphId = 65536
        } else {
            rf!!.seek((table_location[0] + 4).toLong())
            maxGlyphId = rf!!.readUnsignedShort()
        }
    }

    /**
     * Gets the Postscript font name.

     * @return the Postscript font name
     * *
     * @throws DocumentException the font is invalid
     * *
     * @throws IOException       the font file could not be read
     */
    val baseFont: String
        @Throws(DocumentException::class, IOException::class)
        get() {
            val table_location: IntArray?
            table_location = tables["name"]
            if (table_location == null)
                throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "name", fileName + style))
            rf!!.seek((table_location[0] + 2).toLong())
            val numRecords = rf!!.readUnsignedShort()
            val startOfStorage = rf!!.readUnsignedShort()
            for (k in 0..numRecords - 1) {
                val platformID = rf!!.readUnsignedShort()
                val platformEncodingID = rf!!.readUnsignedShort()
                val languageID = rf!!.readUnsignedShort()
                val nameID = rf!!.readUnsignedShort()
                val length = rf!!.readUnsignedShort()
                val offset = rf!!.readUnsignedShort()
                if (nameID == 6) {
                    rf!!.seek(table_location[0] + startOfStorage + offset.toLong())
                    if (platformID == 0 || platformID == 3)
                        return readUnicodeString(length)
                    else
                        return readStandardString(length)
                }
            }
            val file = File(fileName)
            return file.name.replace(' ', '-')
        }

    /**
     * Extracts the names of the font in all the languages available.

     * @param id the name id to retrieve
     * *
     * @throws DocumentException on error
     * *
     * @throws IOException       on error
     */
    @Throws(DocumentException::class, IOException::class)
    fun getNames(id: Int): Array<Array<String>> {
        val table_location: IntArray?
        table_location = tables["name"]
        if (table_location == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "name", fileName + style))
        rf!!.seek((table_location[0] + 2).toLong())
        val numRecords = rf!!.readUnsignedShort()
        val startOfStorage = rf!!.readUnsignedShort()
        val names = ArrayList<Array<String>>()
        for (k in 0..numRecords - 1) {
            val platformID = rf!!.readUnsignedShort()
            val platformEncodingID = rf!!.readUnsignedShort()
            val languageID = rf!!.readUnsignedShort()
            val nameID = rf!!.readUnsignedShort()
            val length = rf!!.readUnsignedShort()
            val offset = rf!!.readUnsignedShort()
            if (nameID == id) {
                val pos = rf!!.filePointer.toInt()
                rf!!.seek(table_location[0] + startOfStorage + offset.toLong())
                val name: String
                if (platformID == 0 || platformID == 3 || platformID == 2 && platformEncodingID == 1) {
                    name = readUnicodeString(length)
                } else {
                    name = readStandardString(length)
                }
                names.add(arrayOf(platformID.toString(), platformEncodingID.toString(), languageID.toString(), name))
                rf!!.seek(pos.toLong())
            }
        }
        val thisName = arrayOfNulls<Array<String>>(names.size)
        for (k in names.indices)
            thisName[k] = names[k]
        return thisName
    }

    /**
     * Extracts all the names of the names-Table

     * @throws DocumentException on error
     * *
     * @throws IOException       on error
     */
    val allNames: Array<Array<String>>
        @Throws(DocumentException::class, IOException::class)
        get() {
            val table_location: IntArray?
            table_location = tables["name"]
            if (table_location == null)
                throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "name", fileName + style))
            rf!!.seek((table_location[0] + 2).toLong())
            val numRecords = rf!!.readUnsignedShort()
            val startOfStorage = rf!!.readUnsignedShort()
            val names = ArrayList<Array<String>>()
            for (k in 0..numRecords - 1) {
                val platformID = rf!!.readUnsignedShort()
                val platformEncodingID = rf!!.readUnsignedShort()
                val languageID = rf!!.readUnsignedShort()
                val nameID = rf!!.readUnsignedShort()
                val length = rf!!.readUnsignedShort()
                val offset = rf!!.readUnsignedShort()
                val pos = rf!!.filePointer.toInt()
                rf!!.seek(table_location[0] + startOfStorage + offset.toLong())
                val name: String
                if (platformID == 0 || platformID == 3 || platformID == 2 && platformEncodingID == 1) {
                    name = readUnicodeString(length)
                } else {
                    name = readStandardString(length)
                }
                names.add(arrayOf(nameID.toString(), platformID.toString(), platformEncodingID.toString(), languageID.toString(), name))
                rf!!.seek(pos.toLong())
            }
            val thisName = arrayOfNulls<Array<String>>(names.size)
            for (k in names.indices)
                thisName[k] = names[k]
            return thisName
        }

    fun checkCff() {
        val table_location: IntArray?
        table_location = tables["CFF "]
        if (table_location != null) {
            cff = true
            cffOffset = table_location[0]
            cffLength = table_location[1]
        }
    }

    /**
     * Reads the font data.

     * @param ttfAfm the font as a byte array, possibly null
     * *
     * @throws DocumentException the font is invalid
     * *
     * @throws IOException       the font file could not be read
     * *
     * @since 2.1.5
     */
    @Throws(DocumentException::class, IOException::class)
    internal open fun process(ttfAfm: ByteArray?, preload: Boolean) {
        tables = HashMap<String, IntArray>()

        if (ttfAfm == null)
            rf = RandomAccessFileOrArray(fileName, preload, Document.plainRandomAccess)
        else
            rf = RandomAccessFileOrArray(ttfAfm)

        try {
            if (ttcIndex.length > 0) {
                val dirIdx = Integer.parseInt(ttcIndex)
                if (dirIdx < 0)
                    throw DocumentException(MessageLocalization.getComposedMessage("the.font.index.for.1.must.be.positive", fileName))
                val mainTag = readStandardString(4)
                if (mainTag != "ttcf")
                    throw DocumentException(MessageLocalization.getComposedMessage("1.is.not.a.valid.ttc.file", fileName))
                rf!!.skipBytes(4)
                val dirCount = rf!!.readInt()
                if (dirIdx >= dirCount)
                    throw DocumentException(MessageLocalization.getComposedMessage("the.font.index.for.1.must.be.between.0.and.2.it.was.3", fileName, (dirCount - 1).toString(), dirIdx.toString()))
                rf!!.skipBytes(dirIdx * 4)
                directoryOffset = rf!!.readInt()
            }
            rf!!.seek(directoryOffset.toLong())
            val ttId = rf!!.readInt()
            if (ttId != 0x00010000 && ttId != 0x4F54544F)
                throw DocumentException(MessageLocalization.getComposedMessage("1.is.not.a.valid.ttf.or.otf.file", fileName))
            val num_tables = rf!!.readUnsignedShort()
            rf!!.skipBytes(6)
            for (k in 0..num_tables - 1) {
                val tag = readStandardString(4)
                rf!!.skipBytes(4)
                val table_location = IntArray(2)
                table_location[0] = rf!!.readInt()
                table_location[1] = rf!!.readInt()
                tables.put(tag, table_location)
            }
            checkCff()
            postscriptFontName = baseFont
            fullFontName = getNames(4) //full name
            //author Daniel Lichtenberger, CHEMDOX
            val otfFamilyName = getNames(16)
            if (otfFamilyName.size > 0) {
                familyFontName = otfFamilyName
            } else {
                familyFontName = getNames(1)
            }
            val otfSubFamily = getNames(17)
            if (otfFamilyName.size > 0) {
                subFamily = otfSubFamily
            } else {
                subFamily = getNames(2)
            }
            allNameEntries = allNames
            if (!justNames) {
                fillTables()
                readGlyphWidths()
                readCMaps()
                readKerning()
                readBbox()
            }
        } finally {
            //TODO: For embedded fonts, the underlying data source for the font will be left open until this TrueTypeFont object is collected by the Garbage Collector.  That may not be optimal.
            if (!embedded) {
                rf!!.close()
                rf = null
            }
        }
    }

    /**
     * Reads a String from the font file as bytes using the Cp1252
     * encoding.

     * @param length the length of bytes to read
     * *
     * @return the String read
     * *
     * @throws IOException the font file could not be read
     */
    @Throws(IOException::class)
    protected fun readStandardString(length: Int): String {
        return rf!!.readString(length, BaseFont.WINANSI)
    }

    /**
     * Reads a Unicode String from the font file. Each character is
     * represented by two bytes.

     * @param length the length of bytes to read. The String will have length/2
     * *               characters
     * *
     * @return the String read
     * *
     * @throws IOException the font file could not be read
     */
    @Throws(IOException::class)
    protected fun readUnicodeString(length: Int): String {
        var length = length
        val buf = StringBuffer()
        length /= 2
        for (k in 0..length - 1) {
            buf.append(rf!!.readChar())
        }
        return buf.toString()
    }

    /**
     * Reads the glyphs widths. The widths are extracted from the table 'hmtx'.
     * The glyphs are normalized to 1000 units.

     * @throws DocumentException the font is invalid
     * *
     * @throws IOException       the font file could not be read
     */
    @Throws(DocumentException::class, IOException::class)
    protected fun readGlyphWidths() {
        val table_location: IntArray?
        table_location = tables["hmtx"]
        if (table_location == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "hmtx", fileName + style))
        rf!!.seek(table_location[0].toLong())
        glyphWidthsByIndex = IntArray(hhea.numberOfHMetrics)
        for (k in 0..hhea.numberOfHMetrics - 1) {
            glyphWidthsByIndex[k] = rf!!.readUnsignedShort() * 1000 / head.unitsPerEm
            @SuppressWarnings("unused")
            val leftSideBearing = rf!!.readShort() * 1000 / head.unitsPerEm
        }
    }

    /**
     * Gets a glyph width.

     * @param glyph the glyph to get the width of
     * *
     * @return the width of the glyph in normalized 1000 units
     */
    protected fun getGlyphWidth(glyph: Int): Int {
        var glyph = glyph
        if (glyph >= glyphWidthsByIndex.size)
            glyph = glyphWidthsByIndex.size - 1
        return glyphWidthsByIndex[glyph]
    }

    @Throws(DocumentException::class, IOException::class)
    private fun readBbox() {
        var tableLocation: IntArray?
        tableLocation = tables["head"]
        if (tableLocation == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "head", fileName + style))
        rf!!.seek((tableLocation[0] + TrueTypeFontSubSet.HEAD_LOCA_FORMAT_OFFSET).toLong())
        val locaShortTable = rf!!.readUnsignedShort() == 0
        tableLocation = tables["loca"]
        if (tableLocation == null)
            return
        rf!!.seek(tableLocation[0].toLong())
        val locaTable: IntArray
        if (locaShortTable) {
            val entries = tableLocation[1] / 2
            locaTable = IntArray(entries)
            for (k in 0..entries - 1)
                locaTable[k] = rf!!.readUnsignedShort() * 2
        } else {
            val entries = tableLocation[1] / 4
            locaTable = IntArray(entries)
            for (k in 0..entries - 1)
                locaTable[k] = rf!!.readInt()
        }
        tableLocation = tables["glyf"]
        if (tableLocation == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "glyf", fileName + style))
        val tableGlyphOffset = tableLocation[0]
        bboxes = arrayOfNulls<IntArray>(locaTable.size - 1)
        for (glyph in 0..locaTable.size - 1 - 1) {
            val start = locaTable[glyph]
            if (start != locaTable[glyph + 1]) {
                rf!!.seek(tableGlyphOffset + start + 2.toLong())
                bboxes[glyph] = intArrayOf(rf!!.readShort() * 1000 / head.unitsPerEm, rf!!.readShort() * 1000 / head.unitsPerEm, rf!!.readShort() * 1000 / head.unitsPerEm, rf!!.readShort() * 1000 / head.unitsPerEm)
            }
        }
    }

    /**
     * Reads the several maps from the table 'cmap'. The maps of interest are 1.0 for symbolic
     * fonts and 3.1 for all others. A symbolic font is defined as having the map 3.0.

     * @throws DocumentException the font is invalid
     * *
     * @throws IOException       the font file could not be read
     */
    @Throws(DocumentException::class, IOException::class)
    fun readCMaps() {
        val table_location: IntArray?
        table_location = tables["cmap"]
        if (table_location == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "cmap", fileName + style))
        rf!!.seek(table_location[0].toLong())
        rf!!.skipBytes(2)
        val num_tables = rf!!.readUnsignedShort()
        fontSpecific = false
        var map10 = 0
        var map31 = 0
        var map30 = 0
        var mapExt = 0
        for (k in 0..num_tables - 1) {
            val platId = rf!!.readUnsignedShort()
            val platSpecId = rf!!.readUnsignedShort()
            val offset = rf!!.readInt()
            if (platId == 3 && platSpecId == 0) {
                fontSpecific = true
                map30 = offset
            } else if (platId == 3 && platSpecId == 1) {
                map31 = offset
            } else if (platId == 3 && platSpecId == 10) {
                mapExt = offset
            }
            if (platId == 1 && platSpecId == 0) {
                map10 = offset
            }
        }
        if (map10 > 0) {
            rf!!.seek((table_location[0] + map10).toLong())
            val format = rf!!.readUnsignedShort()
            when (format) {
                0 -> cmap10 = readFormat0()
                4 -> cmap10 = readFormat4()
                6 -> cmap10 = readFormat6()
            }
        }
        if (map31 > 0) {
            rf!!.seek((table_location[0] + map31).toLong())
            val format = rf!!.readUnsignedShort()
            if (format == 4) {
                cmap31 = readFormat4()
            }
        }
        if (map30 > 0) {
            rf!!.seek((table_location[0] + map30).toLong())
            val format = rf!!.readUnsignedShort()
            if (format == 4) {
                cmap10 = readFormat4()
            }
        }
        if (mapExt > 0) {
            rf!!.seek((table_location[0] + mapExt).toLong())
            val format = rf!!.readUnsignedShort()
            when (format) {
                0 -> cmapExt = readFormat0()
                4 -> cmapExt = readFormat4()
                6 -> cmapExt = readFormat6()
                12 -> cmapExt = readFormat12()
            }
        }
    }

    @Throws(IOException::class)
    fun readFormat12(): HashMap<Int, IntArray> {
        val h = HashMap<Int, IntArray>()
        rf!!.skipBytes(2)
        val table_lenght = rf!!.readInt()
        rf!!.skipBytes(4)
        val nGroups = rf!!.readInt()
        for (k in 0..nGroups - 1) {
            val startCharCode = rf!!.readInt()
            val endCharCode = rf!!.readInt()
            var startGlyphID = rf!!.readInt()
            for (i in startCharCode..endCharCode) {
                val r = IntArray(2)
                r[0] = startGlyphID
                r[1] = getGlyphWidth(r[0])
                h.put(Integer.valueOf(i), r)
                startGlyphID++
            }
        }
        return h
    }

    /**
     * The information in the maps of the table 'cmap' is coded in several formats.
     * Format 0 is the Apple standard character to glyph index mapping table.

     * @return a HashMap representing this map
     * *
     * @throws IOException the font file could not be read
     */
    @Throws(IOException::class)
    fun readFormat0(): HashMap<Int, IntArray> {
        val h = HashMap<Int, IntArray>()
        rf!!.skipBytes(4)
        for (k in 0..255) {
            val r = IntArray(2)
            r[0] = rf!!.readUnsignedByte()
            r[1] = getGlyphWidth(r[0])
            h.put(Integer.valueOf(k), r)
        }
        return h
    }

    /**
     * The information in the maps of the table 'cmap' is coded in several formats.
     * Format 4 is the Microsoft standard character to glyph index mapping table.

     * @return a HashMap representing this map
     * *
     * @throws IOException the font file could not be read
     */
    @Throws(IOException::class)
    fun readFormat4(): HashMap<Int, IntArray> {
        val h = HashMap<Int, IntArray>()
        val table_lenght = rf!!.readUnsignedShort()
        rf!!.skipBytes(2)
        val segCount = rf!!.readUnsignedShort() / 2
        rf!!.skipBytes(6)
        val endCount = IntArray(segCount)
        for (k in 0..segCount - 1) {
            endCount[k] = rf!!.readUnsignedShort()
        }
        rf!!.skipBytes(2)
        val startCount = IntArray(segCount)
        for (k in 0..segCount - 1) {
            startCount[k] = rf!!.readUnsignedShort()
        }
        val idDelta = IntArray(segCount)
        for (k in 0..segCount - 1) {
            idDelta[k] = rf!!.readUnsignedShort()
        }
        val idRO = IntArray(segCount)
        for (k in 0..segCount - 1) {
            idRO[k] = rf!!.readUnsignedShort()
        }
        val glyphId = IntArray(table_lenght / 2 - 8 - segCount * 4)
        for (k in glyphId.indices) {
            glyphId[k] = rf!!.readUnsignedShort()
        }
        for (k in 0..segCount - 1) {
            var glyph: Int
            var j = startCount[k]
            while (j <= endCount[k] && j != 0xFFFF) {
                if (idRO[k] == 0) {
                    glyph = j + idDelta[k] and 0xFFFF
                } else {
                    val idx = k + idRO[k] / 2 - segCount + j - startCount[k]
                    if (idx >= glyphId.size) {
                        ++j
                        continue
                    }
                    glyph = glyphId[idx] + idDelta[k] and 0xFFFF
                }
                val r = IntArray(2)
                r[0] = glyph
                r[1] = getGlyphWidth(r[0])
                h.put(Integer.valueOf(if (fontSpecific) if (j and 0xff00 == 0xf000) j and 0xff else j else j), r)
                ++j
            }
        }
        return h
    }

    /**
     * The information in the maps of the table 'cmap' is coded in several formats.
     * Format 6 is a trimmed table mapping. It is similar to format 0 but can have
     * less than 256 entries.

     * @return a HashMap representing this map
     * *
     * @throws IOException the font file could not be read
     */
    @Throws(IOException::class)
    fun readFormat6(): HashMap<Int, IntArray> {
        val h = HashMap<Int, IntArray>()
        rf!!.skipBytes(4)
        val start_code = rf!!.readUnsignedShort()
        val code_count = rf!!.readUnsignedShort()
        for (k in 0..code_count - 1) {
            val r = IntArray(2)
            r[0] = rf!!.readUnsignedShort()
            r[1] = getGlyphWidth(r[0])
            h.put(Integer.valueOf(k + start_code), r)
        }
        return h
    }

    /**
     * Reads the kerning information from the 'kern' table.

     * @throws IOException the font file could not be read
     */
    @Throws(IOException::class)
    fun readKerning() {
        val table_location: IntArray?
        table_location = tables["kern"]
        if (table_location == null)
            return
        rf!!.seek((table_location[0] + 2).toLong())
        val nTables = rf!!.readUnsignedShort()
        var checkpoint = table_location[0] + 4
        var length = 0
        for (k in 0..nTables - 1) {
            checkpoint += length
            rf!!.seek(checkpoint.toLong())
            rf!!.skipBytes(2)
            length = rf!!.readUnsignedShort()
            val coverage = rf!!.readUnsignedShort()
            if (coverage and 0xfff7 == 0x0001) {
                val nPairs = rf!!.readUnsignedShort()
                rf!!.skipBytes(6)
                for (j in 0..nPairs - 1) {
                    val pair = rf!!.readInt()
                    val value = rf!!.readShort() * 1000 / head.unitsPerEm
                    kerning.put(pair, value)
                }
            }
        }
    }

    /**
     * Gets the kerning between two Unicode chars.

     * @param char1 the first char
     * *
     * @param char2 the second char
     * *
     * @return the kerning to be applied
     */
    override fun getKerning(char1: Int, char2: Int): Int {
        var metrics: IntArray? = getMetricsTT(char1) ?: return 0
        val c1 = metrics[0]
        metrics = getMetricsTT(char2)
        if (metrics == null)
            return 0
        val c2 = metrics[0]
        return kerning.get((c1 shl 16) + c2)
    }

    /**
     * Gets the width from the font according to the unicode char c.
     * If the name is null it's a symbolic font.

     * @param c    the unicode char
     * *
     * @param name the glyph name
     * *
     * @return the width of the char
     */
    fun getRawWidth(c: Int, name: String): Int {
        val metric = getMetricsTT(c) ?: return 0
        return metric[1]
    }

    /**
     * Generates the font descriptor for this font.

     * @param subsetPrefix the subset prefix
     * *
     * @param fontStream   the indirect reference to a PdfStream containing the font or null
     * *
     * @return the PdfDictionary containing the font descriptor or null
     */
    protected fun getFontDescriptor(fontStream: PdfIndirectReference?, subsetPrefix: String, cidset: PdfIndirectReference?): PdfDictionary {
        val dic = PdfDictionary(PdfName.FONTDESCRIPTOR)
        dic.put(PdfName.ASCENT, PdfNumber(os_2.sTypoAscender * 1000 / head.unitsPerEm))
        dic.put(PdfName.CAPHEIGHT, PdfNumber(os_2.sCapHeight * 1000 / head.unitsPerEm))
        dic.put(PdfName.DESCENT, PdfNumber(os_2.sTypoDescender * 1000 / head.unitsPerEm))
        dic.put(PdfName.FONTBBOX, PdfRectangle(
                (head.xMin * 1000 / head.unitsPerEm).toFloat(),
                (head.yMin * 1000 / head.unitsPerEm).toFloat(),
                (head.xMax * 1000 / head.unitsPerEm).toFloat(),
                (head.yMax * 1000 / head.unitsPerEm).toFloat()))
        if (cidset != null)
            dic.put(PdfName.CIDSET, cidset)
        if (cff) {
            if (encoding.startsWith("Identity-"))
                dic.put(PdfName.FONTNAME, PdfName(subsetPrefix + postscriptFontName + "-" + encoding))
            else
                dic.put(PdfName.FONTNAME, PdfName(subsetPrefix + postscriptFontName + style))
        } else
            dic.put(PdfName.FONTNAME, PdfName(subsetPrefix + postscriptFontName + style))
        dic.put(PdfName.ITALICANGLE, PdfNumber(italicAngle))
        dic.put(PdfName.STEMV, PdfNumber(80))
        if (fontStream != null) {
            if (cff)
                dic.put(PdfName.FONTFILE3, fontStream)
            else
                dic.put(PdfName.FONTFILE2, fontStream)
        }
        var flags = 0
        if (isFixedPitch)
            flags = flags or 1
        flags = flags or if (fontSpecific) 4 else 32
        if (head.macStyle and 2 != 0)
            flags = flags or 64
        if (head.macStyle and 1 != 0)
            flags = flags or 262144
        dic.put(PdfName.FLAGS, PdfNumber(flags))

        return dic
    }

    /**
     * Generates the font dictionary for this font.

     * @param subsetPrefix   the subset prefix
     * *
     * @param firstChar      the first valid character
     * *
     * @param lastChar       the last valid character
     * *
     * @param shortTag       a 256 bytes long byte array where each unused byte is represented by 0
     * *
     * @param fontDescriptor the indirect reference to a PdfDictionary containing the font descriptor or null
     * *
     * @return the PdfDictionary containing the font dictionary
     */
    protected fun getFontBaseType(fontDescriptor: PdfIndirectReference?, subsetPrefix: String, firstChar: Int, lastChar: Int, shortTag: ByteArray): PdfDictionary {
        var firstChar = firstChar
        val dic = PdfDictionary(PdfName.FONT)
        if (cff) {
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1)
            dic.put(PdfName.BASEFONT, PdfName(postscriptFontName + style))
        } else {
            dic.put(PdfName.SUBTYPE, PdfName.TRUETYPE)
            dic.put(PdfName.BASEFONT, PdfName(subsetPrefix + postscriptFontName + style))
        }
        if (!fontSpecific) {
            for (k in firstChar..lastChar) {
                if (differences[k] != BaseFont.notdef) {
                    firstChar = k
                    break
                }
            }
            if (encoding == "Cp1252" || encoding == "MacRoman")
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
        if (fontDescriptor != null)
            dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor)
        return dic
    }

    protected val fullFont: ByteArray
        @Throws(IOException::class)
        get() {
            var rf2: RandomAccessFileOrArray? = null
            try {
                rf2 = RandomAccessFileOrArray(rf)
                rf2.reOpen()
                val b = ByteArray(rf2.length().toInt())
                rf2.readFully(b)
                return b
            } finally {
                try {
                    if (rf2 != null) {
                        rf2.close()
                    }
                } catch (e: Exception) {
                }

            }
        }

    @Synchronized @Throws(IOException::class, DocumentException::class)
    protected fun getSubSet(glyphs: HashSet<Any>, subsetp: Boolean): ByteArray {
        val sb = TrueTypeFontSubSet(fileName, RandomAccessFileOrArray(rf), glyphs, directoryOffset, true, !subsetp)
        return sb.process()
    }

    protected fun addRangeUni(longTag: HashMap<Int, IntArray>, includeMetrics: Boolean, subsetp: Boolean) {
        if (!subsetp && (subsetRanges != null || directoryOffset > 0)) {
            val rg = if (subsetRanges == null && directoryOffset > 0) intArrayOf(0, 0xffff) else compactRanges(subsetRanges)
            val usemap: HashMap<Int, IntArray>
            if (!fontSpecific && cmap31 != null)
                usemap = cmap31
            else if (fontSpecific && cmap10 != null)
                usemap = cmap10
            else if (cmap31 != null)
                usemap = cmap31
            else
                usemap = cmap10
            for (e in usemap.entries) {
                val v = e.value
                val gi = Integer.valueOf(v[0])
                if (longTag.containsKey(gi))
                    continue
                val c = e.key.toInt()
                var skip = true
                var k = 0
                while (k < rg.size) {
                    if (c >= rg[k] && c <= rg[k + 1]) {
                        skip = false
                        break
                    }
                    k += 2
                }
                if (!skip)
                    longTag.put(gi, if (includeMetrics) intArrayOf(v[0], v[1], c) else null)
            }
        }
    }

    protected fun addRangeUni(longTag: HashSet<Int>, subsetp: Boolean) {
        if (!subsetp && (subsetRanges != null || directoryOffset > 0)) {
            val rg = if (subsetRanges == null && directoryOffset > 0) intArrayOf(0, 0xffff) else compactRanges(subsetRanges)
            val usemap: HashMap<Int, IntArray>
            if (!fontSpecific && cmap31 != null)
                usemap = cmap31
            else if (fontSpecific && cmap10 != null)
                usemap = cmap10
            else if (cmap31 != null)
                usemap = cmap31
            else
                usemap = cmap10
            for (e in usemap.entries) {
                val v = e.value
                val gi = Integer.valueOf(v[0])
                if (longTag.contains(gi))
                    continue
                val c = e.key.toInt()
                var skip = true
                var k = 0
                while (k < rg.size) {
                    if (c >= rg[k] && c <= rg[k + 1]) {
                        skip = false
                        break
                    }
                    k += 2
                }
                if (!skip)
                    longTag.add(gi)
            }
        }
    }

    /**
     * Outputs to the writer the font dictionaries and streams.

     * @param writer the writer for this document
     * *
     * @param ref    the font indirect reference
     * *
     * @param params several parameters that depend on the font type
     * *
     * @throws IOException       on error
     * *
     * @throws DocumentException error in generating the object
     */
    @Throws(DocumentException::class, IOException::class)
    internal open fun writeFont(writer: PdfWriter, ref: PdfIndirectReference, params: Array<Any>) {
        var firstChar = (params[0] as Int).toInt()
        var lastChar = (params[1] as Int).toInt()
        val shortTag = params[2] as ByteArray
        val subsetp = (params[3] as Boolean).booleanValue() && subset

        if (!subsetp) {
            firstChar = 0
            lastChar = shortTag.size - 1
            for (k in shortTag.indices)
                shortTag[k] = 1
        }
        var ind_font: PdfIndirectReference? = null
        var pobj: PdfObject? = null
        var obj: PdfIndirectObject? = null
        var subsetPrefix = ""
        if (embedded) {
            if (cff) {
                pobj = BaseFont.StreamFont(readCffFont(), "Type1C", compressionLevel)
                obj = writer.addToBody(pobj)
                ind_font = obj!!.indirectReference
            } else {
                if (subsetp)
                    subsetPrefix = createSubsetPrefix()
                val glyphs = HashSet<Int>()
                for (k in firstChar..lastChar) {
                    if (shortTag[k].toInt() != 0) {
                        var metrics: IntArray? = null
                        if (specialMap != null) {
                            val cd = GlyphList.nameToUnicode(differences[k])
                            if (cd != null)
                                metrics = getMetricsTT(cd[0])
                        } else {
                            if (fontSpecific)
                                metrics = getMetricsTT(k)
                            else
                                metrics = getMetricsTT(unicodeDifferences[k].toInt())
                        }
                        if (metrics != null)
                            glyphs.add(Integer.valueOf(metrics[0]))
                    }
                }
                addRangeUni(glyphs, subsetp)
                var b: ByteArray? = null
                if (subsetp || directoryOffset != 0 || subsetRanges != null) {
                    b = getSubSet(HashSet(glyphs), subsetp)
                } else {
                    b = fullFont
                }
                val lengths = intArrayOf(b.size)
                pobj = BaseFont.StreamFont(b, lengths, compressionLevel)
                obj = writer.addToBody(pobj)
                ind_font = obj!!.indirectReference
            }
        }
        pobj = getFontDescriptor(ind_font, subsetPrefix, null)
        if (pobj != null) {
            obj = writer.addToBody(pobj)
            ind_font = obj!!.indirectReference
        }
        pobj = getFontBaseType(ind_font, subsetPrefix, firstChar, lastChar, shortTag)
        writer.addToBody(pobj, ref)
    }

    /**
     * If this font file is using the Compact Font File Format, then this method
     * will return the raw bytes needed for the font stream. If this method is
     * ever made public: make sure to add a test if (cff == true).

     * @return a byte array
     * *
     * @since 2.1.3
     */
    @Throws(IOException::class)
    protected fun readCffFont(): ByteArray {
        val rf2 = RandomAccessFileOrArray(rf)
        val b = ByteArray(cffLength)
        try {
            rf2.reOpen()
            rf2.seek(cffOffset.toLong())
            rf2.readFully(b)
        } finally {
            try {
                rf2.close()
            } catch (e: Exception) {
                // empty on purpose
            }

        }
        return b
    }

    /**
     * Returns a PdfStream object with the full font program.

     * @return a PdfStream with the font program
     * *
     * @since 2.1.3
     */
    open val fullFontStream: PdfStream
        @Throws(IOException::class, DocumentException::class)
        get() {
            if (cff) {
                return BaseFont.StreamFont(readCffFont(), "Type1C", compressionLevel)
            } else {
                val b = fullFont
                val lengths = intArrayOf(b.size)
                return BaseFont.StreamFont(b, lengths, compressionLevel)
            }
        }

    /**
     * Gets the font parameter identified by key. Valid values
     * for key are ASCENT, CAPHEIGHT, DESCENT
     * and ITALICANGLE.

     * @param key      the parameter to be extracted
     * *
     * @param fontSize the font size in points
     * *
     * @return the parameter in points
     */
    override fun getFontDescriptor(key: Int, fontSize: Float): Float {
        when (key) {
            BaseFont.ASCENT -> return os_2.sTypoAscender * fontSize / head.unitsPerEm
            BaseFont.CAPHEIGHT -> return os_2.sCapHeight * fontSize / head.unitsPerEm
            BaseFont.DESCENT -> return os_2.sTypoDescender * fontSize / head.unitsPerEm
            BaseFont.ITALICANGLE -> return italicAngle.toFloat()
            BaseFont.BBOXLLX -> return fontSize * head.xMin / head.unitsPerEm
            BaseFont.BBOXLLY -> return fontSize * head.yMin / head.unitsPerEm
            BaseFont.BBOXURX -> return fontSize * head.xMax / head.unitsPerEm
            BaseFont.BBOXURY -> return fontSize * head.yMax / head.unitsPerEm
            BaseFont.AWT_ASCENT -> return fontSize * hhea.Ascender / head.unitsPerEm
            BaseFont.AWT_DESCENT -> return fontSize * hhea.Descender / head.unitsPerEm
            BaseFont.AWT_LEADING -> return fontSize * hhea.LineGap / head.unitsPerEm
            BaseFont.AWT_MAXADVANCE -> return fontSize * hhea.advanceWidthMax / head.unitsPerEm
            BaseFont.UNDERLINE_POSITION -> return (underlinePosition - underlineThickness / 2) * fontSize / head.unitsPerEm
            BaseFont.UNDERLINE_THICKNESS -> return underlineThickness * fontSize / head.unitsPerEm
            BaseFont.STRIKETHROUGH_POSITION -> return os_2.yStrikeoutPosition * fontSize / head.unitsPerEm
            BaseFont.STRIKETHROUGH_THICKNESS -> return os_2.yStrikeoutSize * fontSize / head.unitsPerEm
            BaseFont.SUBSCRIPT_SIZE -> return os_2.ySubscriptYSize * fontSize / head.unitsPerEm
            BaseFont.SUBSCRIPT_OFFSET -> return -os_2.ySubscriptYOffset * fontSize / head.unitsPerEm
            BaseFont.SUPERSCRIPT_SIZE -> return os_2.ySuperscriptYSize * fontSize / head.unitsPerEm
            BaseFont.SUPERSCRIPT_OFFSET -> return os_2.ySuperscriptYOffset * fontSize / head.unitsPerEm
            BaseFont.WEIGHT_CLASS -> return os_2.usWeightClass.toFloat()
            BaseFont.WIDTH_CLASS -> return os_2.usWidthClass.toFloat()
        }
        return 0f
    }

    /**
     * Gets the glyph index and metrics for a character.

     * @param c the character
     * *
     * @return an int array with {glyph index, width}
     */
    open fun getMetricsTT(c: Int): IntArray? {
        if (cmapExt != null)
            return cmapExt!![Integer.valueOf(c)]
        if (!fontSpecific && cmap31 != null)
            return cmap31!![Integer.valueOf(c)]
        if (fontSpecific && cmap10 != null)
            return cmap10!![Integer.valueOf(c)]
        if (cmap31 != null)
            return cmap31!![Integer.valueOf(c)]
        if (cmap10 != null)
            return cmap10!![Integer.valueOf(c)]
        return null
    }

    /**
     * Gets the code pages supported by the font.

     * @return the code pages supported by the font
     */
    override val codePagesSupported: Array<String>
        get() {
            val cp = (os_2.ulCodePageRange2.toLong() shl 32) + (os_2.ulCodePageRange1 and 0xffffffffL)
            var count = 0
            var bit: Long = 1
            for (k in 0..63) {
                if (cp and bit != 0 && codePages[k] != null)
                    ++count
                bit = bit shl 1
            }
            val ret = arrayOfNulls<String>(count)
            count = 0
            bit = 1
            for (k in 0..63) {
                if (cp and bit != 0 && codePages[k] != null)
                    ret[count++] = codePages[k]
                bit = bit shl 1
            }
            return ret
        }

    /**
     * Gets the full subfamily name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, subfamily}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.

     * @return the full subfamily name of the font
     */
    override val subfamily: String
        get() {
            if (subFamily != null && subFamily!!.size > 0) {
                return subFamily!![0][3]
            }
            return super.subfamily
        }

    /**
     * Checks if the font has any kerning pairs.

     * @return true if the font has any kerning pairs
     */
    override fun hasKernPairs(): Boolean {
        return kerning.size() > 0
    }

    /**
     * Sets the kerning between two Unicode chars.

     * @param char1 the first char
     * *
     * @param char2 the second char
     * *
     * @param kern  the kerning to apply in normalized 1000 units
     * *
     * @return `true` if the kerning was applied, `false` otherwise
     */
    override fun setKerning(char1: Int, char2: Int, kern: Int): Boolean {
        var metrics: IntArray? = getMetricsTT(char1) ?: return false
        val c1 = metrics[0]
        metrics = getMetricsTT(char2)
        if (metrics == null)
            return false
        val c2 = metrics[0]
        kerning.put((c1 shl 16) + c2, kern)
        return true
    }

    override fun getRawCharBBox(c: Int, name: String?): IntArray {
        var map: HashMap<Int, IntArray>? = null
        if (name == null || cmap31 == null)
            map = cmap10
        else
            map = cmap31
        if (map == null)
            return null
        val metric = map[Integer.valueOf(c)]
        if (metric == null || bboxes == null)
            return null
        return bboxes!![metric[0]]
    }

    companion object {

        /**
         * The code pages possible for a True Type font.
         */
        val codePages = arrayOf<String>("1252 Latin 1", "1250 Latin 2: Eastern Europe", "1251 Cyrillic", "1253 Greek", "1254 Turkish", "1255 Hebrew", "1256 Arabic", "1257 Windows Baltic", "1258 Vietnamese", null, null, null, null, null, null, null, "874 Thai", "932 JIS/Japan", "936 Chinese: Simplified chars--PRC and Singapore", "949 Korean Wansung", "950 Chinese: Traditional chars--Taiwan and Hong Kong", "1361 Korean Johab", null, null, null, null, null, null, null, "Macintosh Character Set (US Roman)", "OEM Character Set", "Symbol Character Set", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "869 IBM Greek", "866 MS-DOS Russian", "865 MS-DOS Nordic", "864 Arabic", "863 MS-DOS Canadian French", "862 Hebrew", "861 MS-DOS Icelandic", "860 MS-DOS Portuguese", "857 IBM Turkish", "855 IBM Cyrillic; primarily Russian", "852 Latin 2", "775 MS-DOS Baltic", "737 Greek; former 437 G", "708 Arabic; ASMO 708", "850 WE/Latin 1", "437 US")

        /**
         * Gets the name from a composed TTC file name.
         * If I have for input "myfont.ttc,2" the return will
         * be "myfont.ttc".

         * @param name the full name
         * *
         * @return the simple file name
         */
        protected fun getTTCName(name: String): String {
            val idx = name.toLowerCase().indexOf(".ttc,")
            if (idx < 0)
                return name
            else
                return name.substring(0, idx + 4)
        }

        protected fun compactRanges(ranges: ArrayList<IntArray>): IntArray {
            val simp = ArrayList<IntArray>()
            for (k in ranges.indices) {
                val r = ranges[k]
                var j = 0
                while (j < r.size) {
                    simp.add(intArrayOf(Math.max(0, Math.min(r[j], r[j + 1])), Math.min(0xffff, Math.max(r[j], r[j + 1]))))
                    j += 2
                }
            }
            for (k1 in 0..simp.size - 1 - 1) {
                var k2 = k1 + 1
                while (k2 < simp.size) {
                    val r1 = simp[k1]
                    val r2 = simp[k2]
                    if (r1[0] >= r2[0] && r1[0] <= r2[1] || r1[1] >= r2[0] && r1[0] <= r2[1]) {
                        r1[0] = Math.min(r1[0], r2[0])
                        r1[1] = Math.max(r1[1], r2[1])
                        simp.removeAt(k2)
                        --k2
                    }
                    ++k2
                }
            }
            val s = IntArray(simp.size * 2)
            for (k in simp.indices) {
                val r = simp[k]
                s[k * 2] = r[0]
                s[k * 2 + 1] = r[1]
            }
            return s
        }
    }


}
