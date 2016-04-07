/*
 * $Id: bb4159f060063b6a45ce63a3da62d0e4a08f640b $
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
import java.io.InputStream
import java.net.URL
import java.util.HashMap

import com.itextpdf.text.log.Counter
import com.itextpdf.text.log.CounterFactory

/** Reads an FDF form and makes the fields available
 * @author Paulo Soares
 */
class FdfReader : PdfReader {

    /** Gets all the fields. The map is keyed by the fully qualified
     * field name and the value is a merged PdfDictionary
     * with the field content.
     * @return all the fields
     */
    var fields: HashMap<String, PdfDictionary>
        internal set
    /** Gets the PDF file specification contained in the FDF.
     * @return the PDF file specification contained in the FDF
     */
    var fileSpec: String
        internal set
    internal var encoding: PdfName? = null

    /** Reads an FDF form.
     * @param filename the file name of the form
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(filename: String) : super(filename) {
    }

    /** Reads an FDF form.
     * @param pdfIn the byte array with the form
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(pdfIn: ByteArray) : super(pdfIn) {
    }

    /** Reads an FDF form.
     * @param url the URL of the document
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(url: URL) : super(url) {
    }

    /** Reads an FDF form.
     * @param is the InputStream containing the document. The stream is read to the
     * * end but is not closed
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(`is`: InputStream) : super(`is`) {
    }

    protected override val counter: Counter
        get() = COUNTER

    @Throws(IOException::class)
    override fun readPdf() {
        fields = HashMap<String, PdfDictionary>()
        tokens.checkFdfHeader()
        rebuildXref()
        readDocObj()
        readFields()
    }

    protected fun kidNode(merged: PdfDictionary, name: String) {
        var name = name
        val kids = merged.getAsArray(PdfName.KIDS)
        if (kids == null || kids.isEmpty) {
            if (name.length > 0)
                name = name.substring(1)
            fields.put(name, merged)
        } else {
            merged.remove(PdfName.KIDS)
            for (k in 0..kids.size() - 1) {
                val dic = PdfDictionary()
                dic.merge(merged)
                val newDic = kids.getAsDict(k)
                val t = newDic.getAsString(PdfName.T)
                var newName = name
                if (t != null)
                    newName += "." + t.toUnicodeString()
                dic.merge(newDic)
                dic.remove(PdfName.T)
                kidNode(dic, newName)
            }
        }
    }

    protected fun readFields() {
        catalog = trailer!!.getAsDict(PdfName.ROOT)
        val fdf = catalog!!.getAsDict(PdfName.FDF) ?: return
        val fs = fdf.getAsString(PdfName.F)
        if (fs != null)
            fileSpec = fs.toUnicodeString()
        val fld = fdf.getAsArray(PdfName.FIELDS) ?: return
        encoding = fdf.getAsName(PdfName.ENCODING)
        val merged = PdfDictionary()
        merged.put(PdfName.KIDS, fld)
        kidNode(merged, "")
    }

    /** Gets the field dictionary.
     * @param name the fully qualified field name
     * *
     * @return the field dictionary
     */
    fun getField(name: String): PdfDictionary {
        return fields[name]
    }

    /**
     * Gets a byte[] containing a file that is embedded in the FDF.
     * @param name the fully qualified field name
     * *
     * @return the bytes of the file
     * *
     * @throws IOException
     * *
     * @since 5.0.1
     */
    @Throws(IOException::class)
    fun getAttachedFile(name: String): ByteArray {
        val field = fields[name]
        if (field != null) {
            var ir: PdfIndirectReference = field.get(PdfName.V) as PRIndirectReference?
            val filespec = getPdfObject(ir.number) as PdfDictionary?
            val ef = filespec.getAsDict(PdfName.EF)
            ir = ef.get(PdfName.F) as PRIndirectReference?
            val stream = getPdfObject(ir.number) as PRStream?
            return PdfReader.getStreamBytes(stream)
        }
        return ByteArray(0)
    }

    /**
     * Gets the field value or null if the field does not
     * exist or has no value defined.
     * @param name the fully qualified field name
     * *
     * @return the field value or null
     */
    fun getFieldValue(name: String): String? {
        val field = fields[name] ?: return null
        val v = PdfReader.getPdfObject(field.get(PdfName.V)) ?: return null
        if (v.isName)
            return PdfName.decodeName((v as PdfName).toString())
        else if (v.isString) {
            val vs = v as PdfString?
            if (encoding == null || vs.encoding != null)
                return vs.toUnicodeString()
            val b = vs.bytes
            if (b.size >= 2 && b[0] == 254.toByte() && b[1] == 255.toByte())
                return vs.toUnicodeString()
            try {
                if (encoding == PdfName.SHIFT_JIS)
                    return String(b, "SJIS")
                else if (encoding == PdfName.UHC)
                    return String(b, "MS949")
                else if (encoding == PdfName.GBK)
                    return String(b, "GBK")
                else if (encoding == PdfName.BIGFIVE)
                    return String(b, "Big5")
                else if (encoding == PdfName.UTF_8)
                    return String(b, "UTF8")
            } catch (e: Exception) {
            }

            return vs.toUnicodeString()
        }
        return null
    }

    companion object {

        protected var COUNTER = CounterFactory.getCounter(FdfReader::class.java)
    }
}
