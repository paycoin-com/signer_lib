/*
 * $Id: 839abe0d0e9583038f24d9e09ba3ec03cd330ea2 $
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
package com.itextpdf.text.pdf.fonts.cmaps

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.PRTokeniser
import com.itextpdf.text.pdf.PdfContentParser
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfString
import java.io.IOException
import java.util.ArrayList

/**

 * @author psoares
 */
object CMapParserEx {

    private val CMAPNAME = PdfName("CMapName")
    private val DEF = "def"
    private val ENDCIDRANGE = "endcidrange"
    private val ENDCIDCHAR = "endcidchar"
    private val ENDBFRANGE = "endbfrange"
    private val ENDBFCHAR = "endbfchar"
    private val USECMAP = "usecmap"
    private val MAXLEVEL = 10

    @Throws(IOException::class)
    fun parseCid(cmapName: String, cmap: AbstractCMap, location: CidLocation) {
        parseCid(cmapName, cmap, location, 0)
    }

    @Throws(IOException::class)
    private fun parseCid(cmapName: String, cmap: AbstractCMap, location: CidLocation, level: Int) {
        if (level >= MAXLEVEL)
            return
        val inp = location.getLocation(cmapName)
        try {
            val list = ArrayList<PdfObject>()
            val cp = PdfContentParser(inp)
            var maxExc = 50
            while (true) {
                try {
                    cp.parse(list)
                } catch (ex: Exception) {
                    if (--maxExc < 0)
                        break
                    continue
                }

                if (list.isEmpty())
                    break
                val last = list[list.size - 1].toString()
                if (level == 0 && list.size == 3 && last == DEF) {
                    val key = list[0]
                    if (PdfName.REGISTRY == key)
                        cmap.registry = list[1].toString()
                    else if (PdfName.ORDERING == key)
                        cmap.ordering = list[1].toString()
                    else if (CMAPNAME == key)
                        cmap.name = list[1].toString()
                    else if (PdfName.SUPPLEMENT == key) {
                        try {
                            cmap.supplement = (list[1] as PdfNumber).intValue()
                        } catch (ex: Exception) {
                        }

                    }
                } else if ((last == ENDCIDCHAR || last == ENDBFCHAR) && list.size >= 3) {
                    val lmax = list.size - 2
                    var k = 0
                    while (k < lmax) {
                        if (list[k] is PdfString) {
                            cmap.addChar(list[k] as PdfString, list[k + 1])
                        }
                        k += 2
                    }
                } else if ((last == ENDCIDRANGE || last == ENDBFRANGE) && list.size >= 4) {
                    val lmax = list.size - 3
                    var k = 0
                    while (k < lmax) {
                        if (list[k] is PdfString && list[k + 1] is PdfString) {
                            cmap.addRange(list[k] as PdfString, list[k + 1] as PdfString, list[k + 2])
                        }
                        k += 3
                    }
                } else if (last == USECMAP && list.size == 2 && list[0] is PdfName) {
                    parseCid(PdfName.decodeName(list[0].toString()), cmap, location, level + 1)
                }
            }
        } finally {
            inp.close()
        }
    }

    private fun encodeSequence(size: Int, seqs: ByteArray, cid: Char, planes: ArrayList<CharArray>) {
        var size = size
        --size
        var nextPlane = 0
        for (idx in 0..size - 1) {
            val plane = planes[nextPlane]
            val one = seqs[idx] and 0xff
            var c = plane[one]
            if (c.toInt() != 0 && c.toInt() and 0x8000 == 0)
                throw RuntimeException(MessageLocalization.getComposedMessage("inconsistent.mapping"))
            if (c.toInt() == 0) {
                planes.add(CharArray(256))
                c = (planes.size - 1 or 0x8000).toChar()
                plane[one] = c
            }
            nextPlane = c.toInt() and 0x7fff
        }
        val plane = planes[nextPlane]
        val one = seqs[size] and 0xff
        val c = plane[one]
        if (c.toInt() and 0x8000 != 0)
            throw RuntimeException(MessageLocalization.getComposedMessage("inconsistent.mapping"))
        plane[one] = cid
    }

}
