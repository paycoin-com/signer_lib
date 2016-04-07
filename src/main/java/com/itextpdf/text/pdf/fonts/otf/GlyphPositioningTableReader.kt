/*
 * $Id: a3f1ec59d77f8b7a6b748f08e575fb6b92ee6f04 $
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

import com.itextpdf.text.pdf.RandomAccessFileOrArray
import java.io.IOException
import java.util.ArrayList
import java.util.HashSet

/**
 * Not used at present, keeping for sometime future.

 * @author [Palash Ray](mailto:paawak@gmail.com)
 */
class GlyphPositioningTableReader @Throws(IOException::class)
constructor(rf: RandomAccessFileOrArray, gposTableLocation: Int) : OpenTypeFontTableReader(rf, gposTableLocation) {

    @Throws(FontReadingException::class)
    fun read() {
        startReadingTable()
    }

    @Throws(IOException::class)
    override fun readSubTable(lookupType: Int, subTableLocation: Int) {

        if (lookupType == 1) {
            readLookUpType_1(subTableLocation)
        } else if (lookupType == 4) {
            readLookUpType_4(subTableLocation)
        } else if (lookupType == 8) {
            readLookUpType_8(subTableLocation)
        } else {
            System.err.println("The lookupType " + lookupType + " is not yet supported by " + GlyphPositioningTableReader::class.java.simpleName)
        }

    }

    @Throws(IOException::class)
    private fun readLookUpType_1(lookupTableLocation: Int) {
        rf.seek(lookupTableLocation.toLong())
        val posFormat = rf.readShort().toInt()

        if (posFormat == 1) {
            OpenTypeFontTableReader.LOG.debug("Reading `Look Up Type 1, Format 1` ....")
            val coverageOffset = rf.readShort().toInt()
            val valueFormat = rf.readShort().toInt()
            //            LOG.debug("valueFormat=" + valueFormat); 

            //check if XPlacement should be read
            if (valueFormat and 1 == 1) {
                val xPlacement = rf.readShort().toInt()
                OpenTypeFontTableReader.LOG.debug("xPlacement=" + xPlacement)
            }

            //check if YPlacement should be read
            if (valueFormat and 2 == 2) {
                val yPlacement = rf.readShort().toInt()
                OpenTypeFontTableReader.LOG.debug("yPlacement=" + yPlacement)
            }

            val glyphCodes = readCoverageFormat(lookupTableLocation + coverageOffset)

            OpenTypeFontTableReader.LOG.debug("glyphCodes=" + glyphCodes)
        } else {
            System.err.println("The PosFormat " + posFormat + " for `LookupType 1` is not yet supported by " + GlyphPositioningTableReader::class.java.simpleName)
        }

    }

    @Throws(IOException::class)
    private fun readLookUpType_4(lookupTableLocation: Int) {
        rf.seek(lookupTableLocation.toLong())

        val posFormat = rf.readShort().toInt()

        if (posFormat == 1) {

            OpenTypeFontTableReader.LOG.debug("Reading `Look Up Type 4, Format 1` ....")

            val markCoverageOffset = rf.readShort().toInt()
            val baseCoverageOffset = rf.readShort().toInt()
            val classCount = rf.readShort().toInt()
            val markArrayOffset = rf.readShort().toInt()
            val baseArrayOffset = rf.readShort().toInt()

            val markCoverages = readCoverageFormat(lookupTableLocation + markCoverageOffset)
            OpenTypeFontTableReader.LOG.debug("markCoverages=" + markCoverages)

            val baseCoverages = readCoverageFormat(lookupTableLocation + baseCoverageOffset)
            OpenTypeFontTableReader.LOG.debug("baseCoverages=" + baseCoverages)

            readMarkArrayTable(lookupTableLocation + markArrayOffset)

            readBaseArrayTable(lookupTableLocation + baseArrayOffset, classCount)
        } else {
            System.err.println("The posFormat " + posFormat + " is not supported by " + GlyphPositioningTableReader::class.java.simpleName)
        }
    }

    @Throws(IOException::class)
    private fun readLookUpType_8(lookupTableLocation: Int) {
        rf.seek(lookupTableLocation.toLong())

        val posFormat = rf.readShort().toInt()

        if (posFormat == 3) {
            OpenTypeFontTableReader.LOG.debug("Reading `Look Up Type 8, Format 3` ....")
            readChainingContextPositioningFormat_3(lookupTableLocation)
        } else {
            System.err.println("The posFormat " + posFormat + " for `Look Up Type 8` is not supported by " + GlyphPositioningTableReader::class.java.simpleName)
        }
    }

    @Throws(IOException::class)
    private fun readChainingContextPositioningFormat_3(lookupTableLocation: Int) {
        val backtrackGlyphCount = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("backtrackGlyphCount=" + backtrackGlyphCount)
        val backtrackGlyphOffsets = ArrayList<Int>(backtrackGlyphCount)

        for (i in 0..backtrackGlyphCount - 1) {
            val backtrackGlyphOffset = rf.readShort().toInt()
            backtrackGlyphOffsets.add(backtrackGlyphOffset)
        }

        val inputGlyphCount = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("inputGlyphCount=" + inputGlyphCount)
        val inputGlyphOffsets = ArrayList<Int>(inputGlyphCount)

        for (i in 0..inputGlyphCount - 1) {
            val inputGlyphOffset = rf.readShort().toInt()
            inputGlyphOffsets.add(inputGlyphOffset)
        }

        val lookaheadGlyphCount = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("lookaheadGlyphCount=" + lookaheadGlyphCount)
        val lookaheadGlyphOffsets = ArrayList<Int>(lookaheadGlyphCount)

        for (i in 0..lookaheadGlyphCount - 1) {
            val lookaheadGlyphOffset = rf.readShort().toInt()
            lookaheadGlyphOffsets.add(lookaheadGlyphOffset)
        }

        val posCount = rf.readShort().toInt()
        OpenTypeFontTableReader.LOG.debug("posCount=" + posCount)

        val posLookupRecords = ArrayList<PosLookupRecord>(posCount)

        for (i in 0..posCount - 1) {
            val sequenceIndex = rf.readShort().toInt()
            val lookupListIndex = rf.readShort().toInt()
            OpenTypeFontTableReader.LOG.debug("sequenceIndex=$sequenceIndex, lookupListIndex=$lookupListIndex")
            posLookupRecords.add(PosLookupRecord(sequenceIndex, lookupListIndex))
        }

        for (backtrackGlyphOffset in backtrackGlyphOffsets) {
            val backtrackGlyphs = readCoverageFormat(lookupTableLocation + backtrackGlyphOffset)
            OpenTypeFontTableReader.LOG.debug("backtrackGlyphs=" + backtrackGlyphs)
        }

        for (inputGlyphOffset in inputGlyphOffsets) {
            val inputGlyphs = readCoverageFormat(lookupTableLocation + inputGlyphOffset)
            OpenTypeFontTableReader.LOG.debug("inputGlyphs=" + inputGlyphs)
        }

        for (lookaheadGlyphOffset in lookaheadGlyphOffsets) {
            val lookaheadGlyphs = readCoverageFormat(lookupTableLocation + lookaheadGlyphOffset)
            OpenTypeFontTableReader.LOG.debug("lookaheadGlyphs=" + lookaheadGlyphs)
        }

    }

    @Throws(IOException::class)
    private fun readMarkArrayTable(markArrayLocation: Int) {
        rf.seek(markArrayLocation.toLong())
        val markCount = rf.readShort().toInt()
        val markRecords = ArrayList<GlyphPositioningTableReader.MarkRecord>()

        for (i in 0..markCount - 1) {
            markRecords.add(readMarkRecord())
        }

        for (markRecord in markRecords) {
            readAnchorTable(markArrayLocation + markRecord.markAnchorOffset)
        }
    }

    @Throws(IOException::class)
    private fun readMarkRecord(): MarkRecord {
        val markClass = rf.readShort().toInt()
        val markAnchorOffset = rf.readShort().toInt()
        return MarkRecord(markClass, markAnchorOffset)
    }

    @Throws(IOException::class)
    private fun readAnchorTable(anchorTableLocation: Int) {
        rf.seek(anchorTableLocation.toLong())
        val anchorFormat = rf.readShort().toInt()

        if (anchorFormat != 1) {
            System.err.println("The extra features of the AnchorFormat $anchorFormat will not be used")
        }

        val x = rf.readShort().toInt()
        val y = rf.readShort().toInt()

    }

    @Throws(IOException::class)
    private fun readBaseArrayTable(baseArrayTableLocation: Int, classCount: Int) {
        rf.seek(baseArrayTableLocation.toLong())
        val baseCount = rf.readShort().toInt()
        val baseAnchors = HashSet<Int>()

        for (i in 0..baseCount - 1) {
            //read BaseRecord
            for (k in 0..classCount - 1) {
                val baseAnchor = rf.readShort().toInt()
                baseAnchors.add(baseAnchor)
            }
        }

        //    	LOG.debug(baseAnchors.size()); 

        for (baseAnchor in baseAnchors) {
            readAnchorTable(baseArrayTableLocation + baseAnchor)
        }

    }

    internal class MarkRecord(val markClass: Int, val markAnchorOffset: Int)

    internal class PosLookupRecord(val sequenceIndex: Int, val lookupListIndex: Int)

}
