/*
 * $Id: 8e0a130c21b0f3db80a550f5d28f7cf8ebded871 $
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

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet

import com.itextpdf.text.DocumentException
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.error_messages.MessageLocalization

/** Subsets a True Type font by removing the unneeded glyphs from
 * the font.

 * @author  Paulo Soares
 */
internal class TrueTypeFontSubSet
/** Creates a new TrueTypeFontSubSet
 * @param directoryOffset The offset from the start of the file to the table directory
 * *
 * @param fileName the file name of the font
 * *
 * @param glyphsUsed the glyphs used
 * *
 * @param includeCmap true if the table cmap is to be included in the generated font
 */
(
        /** The file name.
         */
        protected var fileName: String,
        /** The file in use.
         */
        protected var rf: RandomAccessFileOrArray, protected var glyphsUsed: HashSet<Int>, protected var directoryOffset: Int, protected var includeCmap: Boolean, protected var includeExtras: Boolean) {


    /** Contains the location of the several tables. The key is the name of
     * the table and the value is an int[3] where position 0
     * is the checksum, position 1 is the offset from the start of the file
     * and position 2 is the length of the table.
     */
    protected var tableDirectory: HashMap<String, IntArray>
    protected var locaShortTable: Boolean = false
    protected var locaTable: IntArray
    protected var glyphsInList: ArrayList<Int>
    protected var tableGlyphOffset: Int = 0
    protected var newLocaTable: IntArray
    protected var newLocaTableOut: ByteArray? = null
    protected var newGlyfTable: ByteArray? = null
    protected var glyfTableRealSize: Int = 0
    protected var locaTableRealSize: Int = 0
    protected var outFont: ByteArray
    protected var fontPtr: Int = 0

    init {
        glyphsInList = ArrayList(glyphsUsed)
    }

    /** Does the actual work of subsetting the font.
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return the subset font
     */
    @Throws(IOException::class, DocumentException::class)
    fun process(): ByteArray {
        try {
            rf.reOpen()
            createTableDirectory()
            readLoca()
            flatGlyphs()
            createNewGlyphTables()
            locaTobytes()
            assembleFont()
            return outFont
        } finally {
            try {
                rf.close()
            } catch (e: Exception) {
                // empty on purpose
            }

        }
    }

    @Throws(IOException::class)
    protected fun assembleFont() {
        var tableLocation: IntArray?
        var fullFontSize = 0
        val tableNames: Array<String>
        if (includeExtras)
            tableNames = tableNamesExtra
        else {
            if (includeCmap)
                tableNames = tableNamesCmap
            else
                tableNames = tableNamesSimple
        }
        var tablesUsed = 2
        var len = 0
        for (k in tableNames.indices) {
            val name = tableNames[k]
            if (name == "glyf" || name == "loca")
                continue
            tableLocation = tableDirectory[name]
            if (tableLocation == null)
                continue
            ++tablesUsed
            fullFontSize += tableLocation[TABLE_LENGTH] + 3 and 3.inv()
        }
        fullFontSize += newLocaTableOut!!.size
        fullFontSize += newGlyfTable!!.size
        var ref = 16 * tablesUsed + 12
        fullFontSize += ref
        outFont = ByteArray(fullFontSize)
        fontPtr = 0
        writeFontInt(0x00010000)
        writeFontShort(tablesUsed)
        val selector = entrySelectors[tablesUsed]
        writeFontShort((1 shl selector) * 16)
        writeFontShort(selector)
        writeFontShort((tablesUsed - (1 shl selector)) * 16)
        for (k in tableNames.indices) {
            val name = tableNames[k]
            tableLocation = tableDirectory[name]
            if (tableLocation == null)
                continue
            writeFontString(name)
            if (name == "glyf") {
                writeFontInt(calculateChecksum(newGlyfTable))
                len = glyfTableRealSize
            } else if (name == "loca") {
                writeFontInt(calculateChecksum(newLocaTableOut))
                len = locaTableRealSize
            } else {
                writeFontInt(tableLocation[TABLE_CHECKSUM])
                len = tableLocation[TABLE_LENGTH]
            }
            writeFontInt(ref)
            writeFontInt(len)
            ref += len + 3 and 3.inv()
        }
        for (k in tableNames.indices) {
            val name = tableNames[k]
            tableLocation = tableDirectory[name]
            if (tableLocation == null)
                continue
            if (name == "glyf") {
                System.arraycopy(newGlyfTable, 0, outFont, fontPtr, newGlyfTable!!.size)
                fontPtr += newGlyfTable!!.size
                newGlyfTable = null
            } else if (name == "loca") {
                System.arraycopy(newLocaTableOut, 0, outFont, fontPtr, newLocaTableOut!!.size)
                fontPtr += newLocaTableOut!!.size
                newLocaTableOut = null
            } else {
                rf.seek(tableLocation[TABLE_OFFSET].toLong())
                rf.readFully(outFont, fontPtr, tableLocation[TABLE_LENGTH])
                fontPtr += tableLocation[TABLE_LENGTH] + 3 and 3.inv()
            }
        }
    }

    @Throws(IOException::class, DocumentException::class)
    protected fun createTableDirectory() {
        tableDirectory = HashMap<String, IntArray>()
        rf.seek(directoryOffset.toLong())
        val id = rf.readInt()
        if (id != 0x00010000)
            throw DocumentException(MessageLocalization.getComposedMessage("1.is.not.a.true.type.file", fileName))
        val num_tables = rf.readUnsignedShort()
        rf.skipBytes(6)
        for (k in 0..num_tables - 1) {
            val tag = readStandardString(4)
            val tableLocation = IntArray(3)
            tableLocation[TABLE_CHECKSUM] = rf.readInt()
            tableLocation[TABLE_OFFSET] = rf.readInt()
            tableLocation[TABLE_LENGTH] = rf.readInt()
            tableDirectory.put(tag, tableLocation)
        }
    }

    @Throws(IOException::class, DocumentException::class)
    protected fun readLoca() {
        var tableLocation: IntArray?
        tableLocation = tableDirectory["head"]
        if (tableLocation == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "head", fileName))
        rf.seek((tableLocation[TABLE_OFFSET] + HEAD_LOCA_FORMAT_OFFSET).toLong())
        locaShortTable = rf.readUnsignedShort() == 0
        tableLocation = tableDirectory["loca"]
        if (tableLocation == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "loca", fileName))
        rf.seek(tableLocation[TABLE_OFFSET].toLong())
        if (locaShortTable) {
            val entries = tableLocation[TABLE_LENGTH] / 2
            locaTable = IntArray(entries)
            for (k in 0..entries - 1)
                locaTable[k] = rf.readUnsignedShort() * 2
        } else {
            val entries = tableLocation[TABLE_LENGTH] / 4
            locaTable = IntArray(entries)
            for (k in 0..entries - 1)
                locaTable[k] = rf.readInt()
        }
    }

    @Throws(IOException::class)
    protected fun createNewGlyphTables() {
        newLocaTable = IntArray(locaTable.size)
        val activeGlyphs = IntArray(glyphsInList.size)
        for (k in activeGlyphs.indices)
            activeGlyphs[k] = glyphsInList[k].toInt()
        Arrays.sort(activeGlyphs)
        var glyfSize = 0
        for (k in activeGlyphs.indices) {
            val glyph = activeGlyphs[k]
            glyfSize += locaTable[glyph + 1] - locaTable[glyph]
        }
        glyfTableRealSize = glyfSize
        glyfSize = glyfSize + 3 and 3.inv()
        newGlyfTable = ByteArray(glyfSize)
        var glyfPtr = 0
        var listGlyf = 0
        for (k in newLocaTable.indices) {
            newLocaTable[k] = glyfPtr
            if (listGlyf < activeGlyphs.size && activeGlyphs[listGlyf] == k) {
                ++listGlyf
                newLocaTable[k] = glyfPtr
                val start = locaTable[k]
                val len = locaTable[k + 1] - start
                if (len > 0) {
                    rf.seek((tableGlyphOffset + start).toLong())
                    rf.readFully(newGlyfTable, glyfPtr, len)
                    glyfPtr += len
                }
            }
        }
    }

    protected fun locaTobytes() {
        if (locaShortTable)
            locaTableRealSize = newLocaTable.size * 2
        else
            locaTableRealSize = newLocaTable.size * 4
        newLocaTableOut = ByteArray(locaTableRealSize + 3 and 3.inv())
        outFont = newLocaTableOut
        fontPtr = 0
        for (k in newLocaTable.indices) {
            if (locaShortTable)
                writeFontShort(newLocaTable[k] / 2)
            else
                writeFontInt(newLocaTable[k])
        }

    }

    @Throws(IOException::class, DocumentException::class)
    protected fun flatGlyphs() {
        val tableLocation: IntArray?
        tableLocation = tableDirectory["glyf"]
        if (tableLocation == null)
            throw DocumentException(MessageLocalization.getComposedMessage("table.1.does.not.exist.in.2", "glyf", fileName))
        val glyph0 = Integer.valueOf(0)
        if (!glyphsUsed.contains(glyph0)) {
            glyphsUsed.add(glyph0)
            glyphsInList.add(glyph0)
        }
        tableGlyphOffset = tableLocation[TABLE_OFFSET]
        for (k in glyphsInList.indices) {
            val glyph = glyphsInList[k].toInt()
            checkGlyphComposite(glyph)
        }
    }

    @Throws(IOException::class)
    protected fun checkGlyphComposite(glyph: Int) {
        val start = locaTable[glyph]
        if (start == locaTable[glyph + 1])
        // no contour
            return
        rf.seek((tableGlyphOffset + start).toLong())
        val numContours = rf.readShort().toInt()
        if (numContours >= 0)
            return
        rf.skipBytes(8)
        while (true) {
            val flags = rf.readUnsignedShort()
            val cGlyph = Integer.valueOf(rf.readUnsignedShort())
            if (!glyphsUsed.contains(cGlyph)) {
                glyphsUsed.add(cGlyph)
                glyphsInList.add(cGlyph)
            }
            if (flags and MORE_COMPONENTS == 0)
                return
            var skip: Int
            if (flags and ARG_1_AND_2_ARE_WORDS != 0)
                skip = 4
            else
                skip = 2
            if (flags and WE_HAVE_A_SCALE != 0)
                skip += 2
            else if (flags and WE_HAVE_AN_X_AND_Y_SCALE != 0)
                skip += 4
            if (flags and WE_HAVE_A_TWO_BY_TWO != 0)
                skip += 8
            rf.skipBytes(skip)
        }
    }

    /** Reads a String from the font file as bytes using the Cp1252
     * encoding.
     * @param length the length of bytes to read
     * *
     * @return the String read
     * *
     * @throws IOException the font file could not be read
     */
    @Throws(IOException::class)
    protected fun readStandardString(length: Int): String {
        val buf = ByteArray(length)
        rf.readFully(buf)
        try {
            return String(buf, BaseFont.WINANSI)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    protected fun writeFontShort(n: Int) {
        outFont[fontPtr++] = (n shr 8).toByte()
        outFont[fontPtr++] = n.toByte()
    }

    protected fun writeFontInt(n: Int) {
        outFont[fontPtr++] = (n shr 24).toByte()
        outFont[fontPtr++] = (n shr 16).toByte()
        outFont[fontPtr++] = (n shr 8).toByte()
        outFont[fontPtr++] = n.toByte()
    }

    protected fun writeFontString(s: String) {
        val b = PdfEncodings.convertToBytes(s, BaseFont.WINANSI)
        System.arraycopy(b, 0, outFont, fontPtr, b.size)
        fontPtr += b.size
    }

    protected fun calculateChecksum(b: ByteArray): Int {
        val len = b.size / 4
        var v0 = 0
        var v1 = 0
        var v2 = 0
        var v3 = 0
        var ptr = 0
        for (k in 0..len - 1) {
            v3 += b[ptr++] and 0xff
            v2 += b[ptr++] and 0xff
            v1 += b[ptr++] and 0xff
            v0 += b[ptr++] and 0xff
        }
        return v0 + (v1 shl 8) + (v2 shl 16) + (v3 shl 24)
    }

    companion object {
        val tableNamesSimple = arrayOf("cvt ", "fpgm", "glyf", "head", "hhea", "hmtx", "loca", "maxp", "prep")
        val tableNamesCmap = arrayOf("cmap", "cvt ", "fpgm", "glyf", "head", "hhea", "hmtx", "loca", "maxp", "prep")
        val tableNamesExtra = arrayOf("OS/2", "cmap", "cvt ", "fpgm", "glyf", "head", "hhea", "hmtx", "loca", "maxp", "name, prep")
        val entrySelectors = intArrayOf(0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4)
        val TABLE_CHECKSUM = 0
        val TABLE_OFFSET = 1
        val TABLE_LENGTH = 2
        val HEAD_LOCA_FORMAT_OFFSET = 51

        val ARG_1_AND_2_ARE_WORDS = 1
        val WE_HAVE_A_SCALE = 8
        val MORE_COMPONENTS = 32
        val WE_HAVE_AN_X_AND_Y_SCALE = 64
        val WE_HAVE_A_TWO_BY_TWO = 128
    }
}
