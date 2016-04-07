/*
 * $Id: df1a2071ca3c247f0ad79b6aed1b322be21b0ac3 $
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

import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfEncodings
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfString

/**

 * @author psoares
 */
abstract class AbstractCMap {

    var name: String? = null
        internal set(cmapName) {
            this.name = cmapName
        }
    var registry: String? = null
        internal set(registry) {
            this.registry = registry
        }
    var ordering: String? = null
        internal set(ordering) {
            this.ordering = ordering
        }
    var supplement: Int = 0
        internal set(supplement) {
            this.supplement = supplement
        }

    internal abstract fun addChar(mark: PdfString, code: PdfObject)

    internal fun addRange(from: PdfString, to: PdfString, code: PdfObject) {
        val a1 = decodeStringToByte(from)
        val a2 = decodeStringToByte(to)
        if (a1.size != a2.size || a1.size == 0)
            throw IllegalArgumentException("Invalid map.")
        var sout: ByteArray? = null
        if (code is PdfString)
            sout = decodeStringToByte(code)
        val start = byteArrayToInt(a1)
        val end = byteArrayToInt(a2)
        for (k in start..end) {
            intToByteArray(k, a1)
            val s = PdfString(a1)
            s.isHexWriting = true
            if (code is PdfArray) {
                addChar(s, code.getPdfObject(k - start))
            } else if (code is PdfNumber) {
                val nn = code.intValue() + k - start
                addChar(s, PdfNumber(nn))
            } else if (code is PdfString) {
                val s1 = PdfString(sout)
                s1.isHexWriting = true
                ++sout!![sout.size - 1]
                addChar(s, s1)
            }
        }
    }

    fun decodeStringToUnicode(ps: PdfString): String {
        if (ps.isHexWriting)
            return PdfEncodings.convertToString(ps.bytes, "UnicodeBigUnmarked")
        else
            return ps.toUnicodeString()
    }

    companion object {

        private fun intToByteArray(v: Int, b: ByteArray) {
            var v = v
            for (k in b.indices.reversed()) {
                b[k] = v.toByte()
                v = v.ushr(8)
            }
        }

        private fun byteArrayToInt(b: ByteArray): Int {
            var v = 0
            for (k in b.indices) {
                v = v shl 8
                v = v or (b[k] and 0xff)
            }
            return v
        }

        fun decodeStringToByte(s: PdfString): ByteArray {
            val b = s.bytes
            val br = ByteArray(b.size)
            System.arraycopy(b, 0, br, 0, b.size)
            return br
        }
    }
}
