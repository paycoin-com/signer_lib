/*
 * $Id: 48d6e326a4d0dd2ac4086c5b8cb7bc50e2910e4c $
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
import java.util.Arrays
import java.util.HashMap

/**
 * Creates a number tree.
 * @author Paulo Soares
 */
object PdfNumberTree {

    private val leafSize = 64

    /**
     * Creates a number tree.
     * @param items the item of the number tree. The key is an Integer
     * * and the value is a PdfObject.
     * *
     * @param writer the writer
     * *
     * @throws IOException on error
     * *
     * @return the dictionary with the number tree.
     */
    @Throws(IOException::class)
    fun <O : PdfObject> writeTree(items: HashMap<Int, O>, writer: PdfWriter): PdfDictionary? {
        if (items.isEmpty())
            return null
        var numbers = arrayOfNulls<Int>(items.size)
        numbers = items.keys.toArray<Int>(numbers)
        Arrays.sort(numbers)
        if (numbers.size <= leafSize) {
            val dic = PdfDictionary()
            val ar = PdfArray()
            for (k in numbers.indices) {
                ar.add(PdfNumber(numbers[k].toInt()))
                ar.add(items[numbers[k]])
            }
            dic.put(PdfName.NUMS, ar)
            return dic
        }
        var skip = leafSize
        val kids = arrayOfNulls<PdfIndirectReference>((numbers.size + leafSize - 1) / leafSize)
        for (k in kids.indices) {
            var offset = k * leafSize
            val end = Math.min(offset + leafSize, numbers.size)
            val dic = PdfDictionary()
            var arr = PdfArray()
            arr.add(PdfNumber(numbers[offset].toInt()))
            arr.add(PdfNumber(numbers[end - 1].toInt()))
            dic.put(PdfName.LIMITS, arr)
            arr = PdfArray()
            while (offset < end) {
                arr.add(PdfNumber(numbers[offset].toInt()))
                arr.add(items[numbers[offset]])
                ++offset
            }
            dic.put(PdfName.NUMS, arr)
            kids[k] = writer.addToBody(dic).indirectReference
        }
        var top = kids.size
        while (true) {
            if (top <= leafSize) {
                val arr = PdfArray()
                for (k in 0..top - 1)
                    arr.add(kids[k])
                val dic = PdfDictionary()
                dic.put(PdfName.KIDS, arr)
                return dic
            }
            skip *= leafSize
            val tt = (numbers.size + skip - 1) / skip
            for (k in 0..tt - 1) {
                var offset = k * leafSize
                val end = Math.min(offset + leafSize, top)
                val dic = PdfDictionary()
                var arr = PdfArray()
                arr.add(PdfNumber(numbers[k * skip].toInt()))
                arr.add(PdfNumber(numbers[Math.min((k + 1) * skip, numbers.size) - 1].toInt()))
                dic.put(PdfName.LIMITS, arr)
                arr = PdfArray()
                while (offset < end) {
                    arr.add(kids[offset])
                    ++offset
                }
                dic.put(PdfName.KIDS, arr)
                kids[k] = writer.addToBody(dic).indirectReference
            }
            top = tt
        }
    }

    private fun iterateItems(dic: PdfDictionary, items: HashMap<Int, PdfObject>) {
        var nn: PdfArray? = PdfReader.getPdfObjectRelease(dic.get(PdfName.NUMS)) as PdfArray?
        if (nn != null) {
            var k = 0
            while (k < nn.size()) {
                val s = PdfReader.getPdfObjectRelease(nn.getPdfObject(k++)) as PdfNumber?
                items.put(Integer.valueOf(s.intValue()), nn.getPdfObject(k))
                ++k
            }
        } else if ((nn = PdfReader.getPdfObjectRelease(dic.get(PdfName.KIDS)) as PdfArray?) != null) {
            for (k in 0..nn!!.size() - 1) {
                val kid = PdfReader.getPdfObjectRelease(nn.getPdfObject(k)) as PdfDictionary?
                iterateItems(kid, items)
            }
        }
    }

    fun readTree(dic: PdfDictionary?): HashMap<Int, PdfObject> {
        val items = HashMap<Int, PdfObject>()
        if (dic != null)
            iterateItems(dic, items)
        return items
    }
}
