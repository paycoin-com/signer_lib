/*
 * $Id: 02d55b9fbd3d0521c108364a8221f437a76b3757 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Alexander Chingarev, et al.
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
import com.itextpdf.text.log.LoggerFactory

import java.io.IOException
import java.util.Arrays
import java.util.HashMap
import java.util.HashSet

class TtfUnicodeWriter(writer: PdfWriter) {

    protected var writer: PdfWriter? = null

    init {
        this.writer = writer
    }

    @Throws(DocumentException::class, IOException::class)
    fun writeFont(font: TrueTypeFontUnicode, ref: PdfIndirectReference, params: Array<Any>, rotbits: ByteArray) {
        val longTag = params[0] as HashMap<Int, IntArray>
        font.addRangeUni(longTag, true, font.subset)
        var metrics = longTag.values.toArray<IntArray>(arrayOfNulls<IntArray>(0))
        Arrays.sort(metrics, font)
        var ind_font: PdfIndirectReference
        var pobj: PdfObject?
        var obj: PdfIndirectObject
        // sivan: cff
        if (font.cff) {
            var b = font.readCffFont()
            if (font.subset || font.subsetRanges != null) {
                val cff = CFFFontSubset(RandomAccessFileOrArray(b), longTag)
                try {
                    b = cff.Process(cff.names[0])
                    //temporary fix for cff subset failure
                } catch (e: Exception) {
                    LoggerFactory.getLogger(TtfUnicodeWriter::class.java).error("Issue in CFF font subsetting." + "Subsetting was disabled", e)
                    font.isSubset = false
                    font.addRangeUni(longTag, true, font.subset)
                    metrics = longTag.values.toArray<IntArray>(arrayOfNulls<IntArray>(0))
                    Arrays.sort(metrics, font)
                }

            }
            pobj = BaseFont.StreamFont(b, "CIDFontType0C", font.compressionLevel)
            obj = writer!!.addToBody(pobj)
            ind_font = obj.indirectReference
        } else {
            val b: ByteArray
            if (font.subset || font.directoryOffset != 0) {
                synchronized (font.rf) {
                    val sb = TrueTypeFontSubSet(font.fileName, RandomAccessFileOrArray(font.rf), HashSet(longTag.keys), font.directoryOffset, true, false)
                    b = sb.process()
                }
            } else {
                b = font.fullFont
            }
            val lengths = intArrayOf(b.size)
            pobj = BaseFont.StreamFont(b, lengths, font.compressionLevel)
            obj = writer!!.addToBody(pobj)
            ind_font = obj.indirectReference
        }
        var subsetPrefix = ""
        if (font.subset)
            subsetPrefix = font.createSubsetPrefix()
        val dic = font.getFontDescriptor(ind_font, subsetPrefix, null)
        obj = writer!!.addToBody(dic)
        ind_font = obj.indirectReference

        pobj = font.getCIDFontType2(ind_font, subsetPrefix, metrics)
        obj = writer!!.addToBody(pobj)
        ind_font = obj.indirectReference

        pobj = font.getToUnicode(metrics)
        var toUnicodeRef: PdfIndirectReference? = null

        if (pobj != null) {
            obj = writer!!.addToBody(pobj)
            toUnicodeRef = obj.indirectReference
        }

        pobj = font.getFontBaseType(ind_font, subsetPrefix, toUnicodeRef)
        writer!!.addToBody(pobj, ref)
    }
}
