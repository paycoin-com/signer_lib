/*
 * $Id: a00153548a3c06479770f85b0c373cb705fd2520 $
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
 * Creates a name tree.
 * @author Paulo Soares
 */
object PdfNameTree {

    private val leafSize = 64

    /**
     * Writes a name tree to a PdfWriter.
     * @param items the item of the name tree. The key is a String
     * * and the value is a PdfObject. Note that although the
     * * keys are strings only the lower byte is used and no check is made for chars
     * * with the same lower byte and different upper byte. This will generate a wrong
     * * tree name.
     * *
     * @param writer the writer
     * *
     * @throws IOException on error
     * *
     * @return the dictionary with the name tree. This dictionary is the one
     * * generally pointed to by the key /Dests, for example
     */
    @Throws(IOException::class)
    fun writeTree(items: HashMap<String, out PdfObject>, writer: PdfWriter): PdfDictionary? {
        if (items.isEmpty())
            return null
        var names = arrayOfNulls<String>(items.size)
        names = items.keys.toArray<String>(names)
        Arrays.sort(names)
        if (names.size <= leafSize) {
            val dic = PdfDictionary()
            val ar = PdfArray()
            for (k in names.indices) {
                ar.add(PdfString(names[k], null))
                ar.add(items[names[k]])
            }
            dic.put(PdfName.NAMES, ar)
            return dic
        }
        var skip = leafSize
        val kids = arrayOfNulls<PdfIndirectReference>((names.size + leafSize - 1) / leafSize)
        for (k in kids.indices) {
            var offset = k * leafSize
            val end = Math.min(offset + leafSize, names.size)
            val dic = PdfDictionary()
            var arr = PdfArray()
            arr.add(PdfString(names[offset], null))
            arr.add(PdfString(names[end - 1], null))
            dic.put(PdfName.LIMITS, arr)
            arr = PdfArray()
            while (offset < end) {
                arr.add(PdfString(names[offset], null))
                arr.add(items[names[offset]])
                ++offset
            }
            dic.put(PdfName.NAMES, arr)
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
            val tt = (names.size + skip - 1) / skip
            for (k in 0..tt - 1) {
                var offset = k * leafSize
                val end = Math.min(offset + leafSize, top)
                val dic = PdfDictionary()
                var arr = PdfArray()
                arr.add(PdfString(names[k * skip], null))
                arr.add(PdfString(names[Math.min((k + 1) * skip, names.size) - 1], null))
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

    private fun iterateItems(dic: PdfDictionary, items: HashMap<String, PdfObject>, leftOverString: PdfString?): PdfString? {
        var leftOverString = leftOverString
        var nn: PdfArray? = PdfReader.getPdfObjectRelease(dic.get(PdfName.NAMES)) as PdfArray?
        if (nn != null) {
            var k = 0
            while (k < nn.size()) {
                val s: PdfString
                if (leftOverString == null)
                    s = PdfReader.getPdfObjectRelease(nn.getPdfObject(k++)) as PdfString?
                else {
                    // this is the leftover string from the previous loop
                    s = leftOverString
                    leftOverString = null
                }
                if (k < nn.size())
                // could have a mistake int the pdf file
                    items.put(PdfEncodings.convertToString(s.bytes, null), nn.getPdfObject(k))
                else
                    return s
                ++k
            }
        } else if ((nn = PdfReader.getPdfObjectRelease(dic.get(PdfName.KIDS)) as PdfArray?) != null) {
            for (k in 0..nn!!.size() - 1) {
                val kid = PdfReader.getPdfObjectRelease(nn.getPdfObject(k)) as PdfDictionary?
                leftOverString = iterateItems(kid, items, leftOverString)
            }
        }
        return null
    }

    fun readTree(dic: PdfDictionary?): HashMap<String, PdfObject> {
        val items = HashMap<String, PdfObject>()
        if (dic != null)
            iterateItems(dic, items, null)
        return items
    }
}
