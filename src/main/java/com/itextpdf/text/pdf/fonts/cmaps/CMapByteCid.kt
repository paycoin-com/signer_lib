/*
 * $Id: a22c97fe40366f2b90b50d917a1faa007c1db531 $
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
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfString
import java.util.ArrayList

class CMapByteCid : AbstractCMap() {
    private val planes = ArrayList<CharArray>()

    init {
        planes.add(CharArray(256))
    }

    internal override fun addChar(mark: PdfString, code: PdfObject) {
        if (code !is PdfNumber)
            return
        encodeSequence(AbstractCMap.decodeStringToByte(mark), code.intValue().toChar())
    }

    private fun encodeSequence(seqs: ByteArray, cid: Char) {
        val size = seqs.size - 1
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

    /**

     * @param seq
     * *
     * @return the cid code or -1 for end
     */
    fun decodeSingle(seq: CMapSequence): Int {
        val end = seq.off + seq.len
        var currentPlane = 0
        while (seq.off < end) {
            val one = seq.seq[seq.off++] and 0xff
            --seq.len
            val plane = planes[currentPlane]
            val cid = plane[one].toInt()
            if (cid and 0x8000 == 0) {
                return cid
            } else
                currentPlane = cid and 0x7fff
        }
        return -1
    }

    fun decodeSequence(seq: CMapSequence): String {
        val sb = StringBuilder()
        var cid = 0
        while ((cid = decodeSingle(seq)) >= 0) {
            sb.append(cid.toChar())
        }
        return sb.toString()
    }
}
