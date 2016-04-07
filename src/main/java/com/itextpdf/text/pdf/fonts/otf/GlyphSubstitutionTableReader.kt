/*
 * $Id: 8d63fe34c871ae5d1955b81ac7c5cd450a3ae3d5 $
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
package com.itextpdf.text.pdf.fonts.otf

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.LinkedHashMap

import com.itextpdf.text.pdf.Glyph
import com.itextpdf.text.pdf.RandomAccessFileOrArray

/**
 *
 *
 * Parses an OpenTypeFont file and reads the Glyph Substitution Table. This table governs how two or more Glyphs should be merged
 * to a single Glyph. This is especially useful for Asian languages like Bangla, Hindi, etc.
 *
 *
 *
 * This has been written according to the OPenTypeFont specifications. This may be found [here](http://www.microsoft.com/typography/otspec/gsub.htm).
 *

 * @author [Palash Ray](mailto:paawak@gmail.com)
 */
class GlyphSubstitutionTableReader @Throws(IOException::class)
constructor(rf: RandomAccessFileOrArray, gsubTableLocation: Int,
            private val glyphToCharacterMap: Map<Int, Char>, private val glyphWidthsByIndex: IntArray) : OpenTypeFontTableReader(rf, gsubTableLocation) {
    private var rawLigatureSubstitutionMap: MutableMap<Int, List<Int>>? = null

    @Throws(FontReadingException::class)
    fun read() {
        rawLigatureSubstitutionMap = LinkedHashMap<Int, List<Int>>()
        startReadingTable()
    }

    val glyphSubstitutionMap: Map<String, Glyph>
        @Throws(FontReadingException::class)
        get() {
            val glyphSubstitutionMap = LinkedHashMap<String, Glyph>()

            for (glyphIdToReplace in rawLigatureSubstitutionMap!!.keys) {
                val constituentGlyphs = rawLigatureSubstitutionMap!![glyphIdToReplace]
                val chars = StringBuilder(constituentGlyphs.size)

                for (constituentGlyphId in constituentGlyphs) {
                    chars.append(getTextFromGlyph(constituentGlyphId!!, glyphToCharacterMap))
                }

                val glyph = Glyph(glyphIdToReplace, glyphWidthsByIndex[glyphIdToReplace], chars.toString())

                glyphSubstitutionMap.put(glyph.chars, glyph)
            }

            return Collections.unmodifiableMap(glyphSubstitutionMap)
        }

    @Throws(FontReadingException::class)
    private fun getTextFromGlyph(glyphId: Int, glyphToCharacterMap: Map<Int, Char>): String {

        val chars = StringBuilder(1)

        val c = glyphToCharacterMap[glyphId]

        if (c == null) {
            // it means this represents a compound glyph
            val constituentGlyphs = rawLigatureSubstitutionMap!![glyphId]

            if (constituentGlyphs == null || constituentGlyphs.isEmpty()) {
                throw FontReadingException("No corresponding character or simple glyphs found for GlyphID=" + glyphId)
            }

            for (constituentGlyphId in constituentGlyphs) {
                chars.append(getTextFromGlyph(constituentGlyphId, glyphToCharacterMap))
            }

        } else {
            chars.append(c.charValue())
        }

        return chars.toString()
    }

    @Throws(IOException::class)
    override fun readSubTable(lookupType: Int, subTableLocation: Int) {

        if (lookupType == 1) {
            readSingleSubstitutionSubtable(subTableLocation)
        } else if (lookupType == 4) {
            readLigatureSubstitutionSubtable(subTableLocation)
        } else {
            System.err.println("LookupType " + lookupType + " is not yet handled for " + GlyphSubstitutionTableReader::class.java.simpleName)
        }

    }

    /**
     * LookupType 1: Single Substitution Subtable
     */
    @Throws(IOException::class)
    private fun readSingleSubstitutionSubtable(subTableLocation: Int) {
        rf.seek(subTableLocation.toLong())

        val substFormat = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("substFormat=" + substFormat)

        if (substFormat == 1) {
            val coverage = rf.readShort().toInt()
            OpenTypeFontTableReader.LOG.debug("coverage=" + coverage)

            val deltaGlyphID = rf.readShort().toInt()
            OpenTypeFontTableReader.LOG.debug("deltaGlyphID=" + deltaGlyphID)

            val coverageGlyphIds = readCoverageFormat(subTableLocation + coverage)

            for (coverageGlyphId in coverageGlyphIds) {
                val substituteGlyphId = coverageGlyphId + deltaGlyphID
                rawLigatureSubstitutionMap!!.put(substituteGlyphId, Arrays.asList(coverageGlyphId))
            }
        } else if (substFormat == 2) {
            val coverage = rf.readShort().toInt()
            OpenTypeFontTableReader.LOG.debug("coverage=" + coverage)
            val glyphCount = rf.readUnsignedShort()
            val substitute = IntArray(glyphCount)
            for (k in 0..glyphCount - 1) {
                substitute[k] = rf.readUnsignedShort()
            }
            val coverageGlyphIds = readCoverageFormat(subTableLocation + coverage)
            for (k in 0..glyphCount - 1) {
                rawLigatureSubstitutionMap!!.put(substitute[k], Arrays.asList(coverageGlyphIds[k]))
            }

        } else {
            throw IllegalArgumentException("Bad substFormat: " + substFormat)
        }
    }

    /**
     * LookupType 4: Ligature Substitution Subtable
     */
    @Throws(IOException::class)
    private fun readLigatureSubstitutionSubtable(ligatureSubstitutionSubtableLocation: Int) {
        rf.seek(ligatureSubstitutionSubtableLocation.toLong())
        val substFormat = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("substFormat=" + substFormat)

        if (substFormat != 1) {
            throw IllegalArgumentException("The expected SubstFormat is 1")
        }

        val coverage = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("coverage=" + coverage)

        val ligSetCount = rf.readShort().toInt()

        val ligatureOffsets = ArrayList<Int>(ligSetCount)

        for (i in 0..ligSetCount - 1) {
            val ligatureOffset = rf.readShort().toInt()
            ligatureOffsets.add(ligatureOffset)
        }

        val coverageGlyphIds = readCoverageFormat(ligatureSubstitutionSubtableLocation + coverage)

        if (ligSetCount != coverageGlyphIds.size) {
            throw IllegalArgumentException("According to the OpenTypeFont specifications, the coverage count should be equal to the no. of LigatureSetTables")
        }

        for (i in 0..ligSetCount - 1) {

            val coverageGlyphId = coverageGlyphIds[i]
            val ligatureOffset = ligatureOffsets[i]
            OpenTypeFontTableReader.LOG.debug("ligatureOffset=" + ligatureOffset)
            readLigatureSetTable(ligatureSubstitutionSubtableLocation + ligatureOffset, coverageGlyphId)
        }

    }

    @Throws(IOException::class)
    private fun readLigatureSetTable(ligatureSetTableLocation: Int, coverageGlyphId: Int) {
        rf.seek(ligatureSetTableLocation.toLong())
        val ligatureCount = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("ligatureCount=" + ligatureCount)

        val ligatureOffsets = ArrayList<Int>(ligatureCount)

        for (i in 0..ligatureCount - 1) {
            val ligatureOffset = rf.readShort().toInt()
            ligatureOffsets.add(ligatureOffset)
        }

        for (ligatureOffset in ligatureOffsets) {
            readLigatureTable(ligatureSetTableLocation + ligatureOffset, coverageGlyphId)
        }
    }

    @Throws(IOException::class)
    private fun readLigatureTable(ligatureTableLocation: Int, coverageGlyphId: Int) {
        rf.seek(ligatureTableLocation.toLong())
        val ligGlyph = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("ligGlyph=" + ligGlyph)

        val compCount = rf.readShort().toInt()

        val glyphIdList = ArrayList<Int>()

        glyphIdList.add(coverageGlyphId)

        for (i in 0..compCount - 1 - 1) {
            val glyphId = rf.readShort().toInt()
            glyphIdList.add(glyphId)
        }

        OpenTypeFontTableReader.LOG.debug("glyphIdList=" + glyphIdList)

        val previousValue = rawLigatureSubstitutionMap!!.put(ligGlyph, glyphIdList)

        if (previousValue != null) {
            OpenTypeFontTableReader.LOG.warn("!!!!!!!!!!glyphId=$ligGlyph,\npreviousValue=$previousValue,\ncurrentVal=$glyphIdList")
        }
    }

}
