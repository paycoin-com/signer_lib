/*
 * $Id: 20794062617165bacb5e54705d7b74ab50954553 $
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
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashMap

import com.itextpdf.text.io.RandomAccessSourceFactory
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory
import com.itextpdf.text.pdf.RandomAccessFileOrArray

/**

 * @author [Palash Ray](mailto:paawak@gmail.com)
 */
abstract class OpenTypeFontTableReader @Throws(IOException::class)
constructor(protected val rf: RandomAccessFileOrArray, protected val tableLocation: Int) {

    private var supportedLanguages: List<String>? = null

    val supportedLanguage: Language
        @Throws(FontReadingException::class)
        get() {

            val allLangs = Language.values()

            for (supportedLang in supportedLanguages!!) {
                for (lang in allLangs) {
                    if (lang.isSupported(supportedLang)) {
                        return lang
                    }
                }
            }

            throw FontReadingException("Unsupported languages " + supportedLanguages!!)
        }

    /**
     * This is the starting point of the class. A sub-class must call this
     * method to start getting call backs to the [.readSubTable]
     * method.
     * @throws FontReadingException
     */
    @Throws(FontReadingException::class)
    protected fun startReadingTable() {
        try {
            val header = readHeader()
            // read the Script tables
            readScriptListTable(tableLocation + header.scriptListOffset)

            // read Feature table
            readFeatureListTable(tableLocation + header.featureListOffset)

            // read LookUpList table
            readLookupListTable(tableLocation + header.lookupListOffset)
        } catch (e: IOException) {
            throw FontReadingException("Error reading font file", e)
        }

    }

    @Throws(IOException::class)
    protected abstract fun readSubTable(lookupType: Int, subTableLocation: Int)

    @Throws(IOException::class)
    private fun readLookupListTable(lookupListTableLocation: Int) {
        rf.seek(lookupListTableLocation.toLong())
        val lookupCount = rf.readShort().toInt()

        val lookupTableOffsets = ArrayList<Int>()

        for (i in 0..lookupCount - 1) {
            val lookupTableOffset = rf.readShort().toInt()
            lookupTableOffsets.add(lookupTableOffset)
        }

        // read LookUp tables
        for (i in 0..lookupCount - 1) {
            //			LOG.debug("#############lookupIndex=" + i);
            val lookupTableOffset = lookupTableOffsets[i]
            readLookupTable(lookupListTableLocation + lookupTableOffset)
        }

    }

    @Throws(IOException::class)
    private fun readLookupTable(lookupTableLocation: Int) {
        rf.seek(lookupTableLocation.toLong())
        val lookupType = rf.readShort().toInt()
        // LOG.debug("lookupType=" + lookupType);

        // skip 2 bytes for the field `lookupFlag`
        rf.skipBytes(2)

        val subTableCount = rf.readShort().toInt()
        // LOG.debug("subTableCount=" + subTableCount);

        val subTableOffsets = ArrayList<Int>()

        for (i in 0..subTableCount - 1) {
            val subTableOffset = rf.readShort().toInt()
            subTableOffsets.add(subTableOffset)
        }

        for (subTableOffset in subTableOffsets) {
            // LOG.debug("subTableOffset=" + subTableOffset);
            readSubTable(lookupType, lookupTableLocation + subTableOffset)
        }
    }

    @Throws(IOException::class)
    protected fun readCoverageFormat(coverageLocation: Int): List<Int> {
        rf.seek(coverageLocation.toLong())
        val coverageFormat = rf.readShort().toInt()

        val glyphIds: MutableList<Int>

        if (coverageFormat == 1) {
            val glyphCount = rf.readShort().toInt()

            glyphIds = ArrayList<Int>(glyphCount)

            for (i in 0..glyphCount - 1) {
                val coverageGlyphId = rf.readShort().toInt()
                glyphIds.add(coverageGlyphId)
            }

        } else if (coverageFormat == 2) {

            val rangeCount = rf.readShort().toInt()

            glyphIds = ArrayList<Int>()

            for (i in 0..rangeCount - 1) {
                readRangeRecord(glyphIds)
            }

        } else {
            throw UnsupportedOperationException("Invalid coverage format: " + coverageFormat)
        }

        return Collections.unmodifiableList(glyphIds)
    }

    @Throws(IOException::class)
    private fun readRangeRecord(glyphIds: MutableList<Int>) {
        val startGlyphId = rf.readShort().toInt()
        val endGlyphId = rf.readShort().toInt()
        val startCoverageIndex = rf.readShort().toInt()

        for (glyphId in startGlyphId..endGlyphId) {
            glyphIds.add(glyphId)
        }

        //		LOG.debug("^^^^^^^^^Coverage Format 2.... " 
        //				+ "startGlyphId=" + startGlyphId
        //				+ ", endGlyphId=" + endGlyphId
        //				+ ", startCoverageIndex=" + startCoverageIndex 
        //				+ "\n, glyphIds" + glyphIds);

    }

    @Throws(IOException::class)
    private fun readScriptListTable(scriptListTableLocationOffset: Int) {
        rf.seek(scriptListTableLocationOffset.toLong())
        // Number of ScriptRecords
        val scriptCount = rf.readShort().toInt()

        val scriptRecords = HashMap<String, Int>(
                scriptCount)

        for (i in 0..scriptCount - 1) {
            readScriptRecord(scriptListTableLocationOffset, scriptRecords)
        }

        val supportedLanguages = ArrayList<String>(scriptCount)

        for (scriptName in scriptRecords.keys) {
            readScriptTable(scriptRecords[scriptName])
            supportedLanguages.add(scriptName)
        }

        this.supportedLanguages = Collections.unmodifiableList(supportedLanguages)
    }

    @Throws(IOException::class)
    private fun readScriptRecord(scriptListTableLocationOffset: Int,
                                 scriptRecords: MutableMap<String, Int>) {
        val scriptTag = rf.readString(4, "utf-8")

        val scriptOffset = rf.readShort().toInt()

        scriptRecords.put(scriptTag, scriptListTableLocationOffset + scriptOffset)
    }

    @Throws(IOException::class)
    private fun readScriptTable(scriptTableLocationOffset: Int) {
        rf.seek(scriptTableLocationOffset.toLong())
        val defaultLangSys = rf.readShort().toInt()
        val langSysCount = rf.readShort().toInt()

        if (langSysCount > 0) {
            val langSysRecords = LinkedHashMap<String, Int>(
                    langSysCount)

            for (i in 0..langSysCount - 1) {
                readLangSysRecord(langSysRecords)
            }

            // read LangSys tables
            for (langSysTag in langSysRecords.keys) {
                readLangSysTable(scriptTableLocationOffset + langSysRecords[langSysTag])
            }
        }

        // read default LangSys table
        readLangSysTable(scriptTableLocationOffset + defaultLangSys)
    }

    @Throws(IOException::class)
    private fun readLangSysRecord(langSysRecords: MutableMap<String, Int>) {
        val langSysTag = rf.readString(4, "utf-8")
        val langSys = rf.readShort().toInt()
        langSysRecords.put(langSysTag, langSys)
    }

    @Throws(IOException::class)
    private fun readLangSysTable(langSysTableLocationOffset: Int) {
        rf.seek(langSysTableLocationOffset.toLong())
        val lookupOrderOffset = rf.readShort().toInt()
        LOG.debug("lookupOrderOffset=" + lookupOrderOffset)
        val reqFeatureIndex = rf.readShort().toInt()
        LOG.debug("reqFeatureIndex=" + reqFeatureIndex)
        val featureCount = rf.readShort().toInt()

        val featureListIndices = ArrayList<Short>(featureCount)
        for (i in 0..featureCount - 1) {
            featureListIndices.add(rf.readShort())
        }

        LOG.debug("featureListIndices=" + featureListIndices)

    }

    @Throws(IOException::class)
    private fun readFeatureListTable(featureListTableLocationOffset: Int) {
        rf.seek(featureListTableLocationOffset.toLong())
        val featureCount = rf.readShort().toInt()
        LOG.debug("featureCount=" + featureCount)

        val featureRecords = LinkedHashMap<String, Short>(
                featureCount)
        for (i in 0..featureCount - 1) {
            featureRecords.put(rf.readString(4, "utf-8"), rf.readShort())
        }

        for (featureName in featureRecords.keys) {
            LOG.debug("*************featureName=" + featureName)
            readFeatureTable(featureListTableLocationOffset + featureRecords[featureName])
        }

    }

    @Throws(IOException::class)
    private fun readFeatureTable(featureTableLocationOffset: Int) {
        rf.seek(featureTableLocationOffset.toLong())
        val featureParamsOffset = rf.readShort().toInt()
        LOG.debug("featureParamsOffset=" + featureParamsOffset)

        val lookupCount = rf.readShort().toInt()
        LOG.debug("lookupCount=" + lookupCount)

        val lookupListIndices = ArrayList<Short>(lookupCount)
        for (i in 0..lookupCount - 1) {
            lookupListIndices.add(rf.readShort())
        }

        //		LOG.debug("lookupListIndices=" + lookupListIndices);

    }

    @Throws(IOException::class)
    private fun readHeader(): TableHeader {
        rf.seek(tableLocation.toLong())
        // 32 bit signed
        val version = rf.readInt()
        // 16 bit unsigned
        val scriptListOffset = rf.readUnsignedShort()
        val featureListOffset = rf.readUnsignedShort()
        val lookupListOffset = rf.readUnsignedShort()

        // LOG.debug("version=" + version);
        // LOG.debug("scriptListOffset=" + scriptListOffset);
        // LOG.debug("featureListOffset=" + featureListOffset);
        // LOG.debug("lookupListOffset=" + lookupListOffset);

        val header = TableHeader(version, scriptListOffset,
                featureListOffset, lookupListOffset)

        return header
    }

    companion object {

        protected val LOG = LoggerFactory.getLogger(OpenTypeFontTableReader::class.java)
    }

}
