/*
 * $Id: b10229b9480ae51f593ae6e696ee123a5010b230 $
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

import com.itextpdf.text.DocWriter
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Image
import com.itextpdf.text.log.Counter
import com.itextpdf.text.log.CounterFactory
import com.itextpdf.text.pdf.AcroFields.Item

import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.StringTokenizer

/** Writes an FDF form.
 * @author Paulo Soares
 */
class FdfWriter {
    internal var fields = HashMap<String, Any>()
    internal var wrt: Wrt? = null

    /** The PDF file associated with the FDF.  */
    /** Gets the PDF file name associated with the FDF.
     * @return the PDF file name associated with the FDF
     */
    /** Sets the PDF file name associated with the FDF.
     * @param file the PDF file name associated with the FDF
     */
    var file: String? = null
    var statusMessage: String? = null

    /** Creates a new FdfWriter.  */
    constructor() {
    }

    @Throws(IOException::class)
    constructor(os: OutputStream) {
        wrt = Wrt(os, this)
    }

    /** Writes the content to a stream.
     * @param os the stream
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun writeTo(os: OutputStream) {
        if (wrt == null)
            wrt = Wrt(os, this)
        wrt!!.write()
    }

    @Throws(IOException::class)
    fun write() {
        wrt!!.write()
    }

    @SuppressWarnings("unchecked")
    internal fun setField(field: String, value: PdfObject): Boolean {
        var map = fields
        val tk = StringTokenizer(field, ".")
        if (!tk.hasMoreTokens())
            return false
        while (true) {
            val s = tk.nextToken()
            var obj: Any? = map[s]
            if (tk.hasMoreTokens()) {
                if (obj == null) {
                    obj = HashMap<String, Any>()
                    map.put(s, obj)
                    map = obj as HashMap<String, Any>?
                    continue
                } else if (obj is HashMap<Any, Any>)
                    map = obj as HashMap<String, Any>?
                else
                    return false
            } else {
                if (obj !is HashMap<Any, Any>) {
                    map.put(s, value)
                    return true
                } else
                    return false
            }
        }
    }

    @SuppressWarnings("unchecked")
    internal fun iterateFields(values: HashMap<String, Any>, map: HashMap<String, Any>, name: String) {
        for ((s, obj) in map) {
            if (obj is HashMap<Any, Any>)
                iterateFields(values, obj as HashMap<String, Any>, name + "." + s)
            else
                values.put((name + "." + s).substring(1), obj)
        }
    }

    /** Removes the field value.
     * @param field the field name
     * *
     * @return true if the field was found and removed,
     * * false otherwise
     */
    @SuppressWarnings("unchecked")
    fun removeField(field: String): Boolean {
        var map = fields
        val tk = StringTokenizer(field, ".")
        if (!tk.hasMoreTokens())
            return false
        val hist = ArrayList<Any>()
        while (true) {
            val s = tk.nextToken()
            val obj = map[s] ?: return false
            hist.add(map)
            hist.add(s)
            if (tk.hasMoreTokens()) {
                if (obj is HashMap<Any, Any>)
                    map = obj as HashMap<String, Any>?
                else
                    return false
            } else {
                if (obj is HashMap<Any, Any>)
                    return false
                else
                    break
            }
        }
        var k = hist.size - 2
        while (k >= 0) {
            map = hist[k] as HashMap<String, Any>
            val s = hist[k + 1] as String
            map.remove(s)
            if (!map.isEmpty())
                break
            k -= 2
        }
        return true
    }

    /** Gets all the fields. The map is keyed by the fully qualified
     * field name and the values are PdfObject.
     * @return a map with all the fields
     */
    fun getFields(): HashMap<String, Any> {
        val values = HashMap<String, Any>()
        iterateFields(values, fields, "")
        return values
    }

    /** Gets the field value.
     * @param field the field name
     * *
     * @return the field value or null if not found
     */
    @SuppressWarnings("unchecked")
    fun getField(field: String): String? {
        var map = fields
        val tk = StringTokenizer(field, ".")
        if (!tk.hasMoreTokens())
            return null
        while (true) {
            val s = tk.nextToken()
            val obj = map[s] ?: return null
            if (tk.hasMoreTokens()) {
                if (obj is HashMap<Any, Any>)
                    map = obj as HashMap<String, Any>?
                else
                    return null
            } else {
                if (obj is HashMap<Any, Any>)
                    return null
                else {
                    if ((obj as PdfObject).isString)
                        return (obj as PdfString).toUnicodeString()
                    else
                        return PdfName.decodeName(obj.toString())
                }
            }
        }
    }

    /** Sets the field value as a name.
     * @param field the fully qualified field name
     * *
     * @param value the value
     * *
     * @return true if the value was inserted,
     * * false if the name is incompatible with
     * * an existing field
     */
    fun setFieldAsName(field: String, value: String): Boolean {
        return setField(field, PdfName(value))
    }

    /** Sets the field value as a string.
     * @param field the fully qualified field name
     * *
     * @param value the value
     * *
     * @return true if the value was inserted,
     * * false if the name is incompatible with
     * * an existing field
     */
    fun setFieldAsString(field: String, value: String): Boolean {
        return setField(field, PdfString(value, PdfObject.TEXT_UNICODE))
    }

    /**
     * Sets the field value as a PDFAction.
     * For example, this method allows setting a form submit button action using [PdfAction.createSubmitForm].
     * This method creates an A entry for the specified field in the underlying FDF file.
     * Method contributed by Philippe Laflamme (plaflamme)
     * @param field the fully qualified field name
     * *
     * @param action the field's action
     * *
     * @return true if the value was inserted,
     * * false if the name is incompatible with
     * * an existing field
     * *
     * @since    2.1.5
     */
    fun setFieldAsAction(field: String, action: PdfAction): Boolean {
        return setField(field, action)
    }

    fun setFieldAsTemplate(field: String, template: PdfTemplate): Boolean {
        try {
            val d = PdfDictionary()
            if (template is PdfImportedPage)
                d.put(PdfName.N, template.indirectReference)
            else {
                val str = template.getFormXObject(PdfStream.NO_COMPRESSION)
                val ref = wrt!!.addToBody(str).indirectReference
                d.put(PdfName.N, ref)
            }
            return setField(field, d)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    fun setFieldAsImage(field: String, image: Image): Boolean {
        try {
            if (java.lang.Float.isNaN(image.absoluteX))
                image.setAbsolutePosition(0f, image.absoluteY)
            if (java.lang.Float.isNaN(image.absoluteY))
                image.setAbsolutePosition(image.absoluteY, 0f)
            val tmpl = PdfTemplate.createTemplate(wrt, image.width, image.height)
            tmpl.addImage(image)
            val str = tmpl.getFormXObject(PdfStream.NO_COMPRESSION)
            val ref = wrt!!.addToBody(str).indirectReference
            val d = PdfDictionary()
            d.put(PdfName.N, ref)
            return setField(field, d)
        } catch (de: Exception) {
            throw ExceptionConverter(de)
        }

    }

    fun setFieldAsJavascript(field: String, jsTrigName: PdfName, js: String): Boolean {
        val dict = wrt!!.createAnnotation(null, null)
        val javascript = PdfAction.javaScript(js, wrt)
        dict.put(jsTrigName, javascript)
        return setField(field, dict)
    }

    fun getImportedPage(reader: PdfReader, pageNumber: Int): PdfImportedPage {
        return wrt!!.getImportedPage(reader, pageNumber)
    }

    fun createTemplate(width: Float, height: Float): PdfTemplate {
        return PdfTemplate.createTemplate(wrt, width, height)
    }

    /** Sets all the fields from this FdfReader
     * @param fdf the FdfReader
     */
    fun setFields(fdf: FdfReader) {
        val map = fdf.fields
        for ((key, dic) in map) {
            var v = dic.get(PdfName.V)
            if (v != null) {
                setField(key, v)
            }
            v = dic.get(PdfName.A) // (plaflamme)
            if (v != null) {
                setField(key, v)
            }
        }
    }

    /** Sets all the fields from this PdfReader
     * @param pdf the PdfReader
     */
    fun setFields(pdf: PdfReader) {
        setFields(pdf.acroFields)
    }

    /** Sets all the fields from this AcroFields
     * @param af the AcroFields
     */
    fun setFields(af: AcroFields) {
        for ((fn, item) in af.getFields()) {
            val dic = item.getMerged(0)
            val v = PdfReader.getPdfObjectRelease(dic.get(PdfName.V)) ?: continue
            val ft = PdfReader.getPdfObjectRelease(dic.get(PdfName.FT))
            if (ft == null || PdfName.SIG == ft)
                continue
            setField(fn, v)
        }
    }

    internal class Wrt @Throws(IOException::class)
    constructor(os: OutputStream, private val fdf: FdfWriter) : PdfWriter(PdfDocument(), os) {

        init {
            this.os.write(HEADER_FDF)
            body = PdfWriter.PdfBody(this)
        }

        @Throws(IOException::class)
        fun write() {
            for (element in readerInstances.values) {
                currentPdfReaderInstance = element
                currentPdfReaderInstance!!.writeAllPages()
            }

            val dic = PdfDictionary()
            dic.put(PdfName.FIELDS, calculate(fdf.fields))
            if (fdf.file != null)
                dic.put(PdfName.F, PdfString(fdf.file, PdfObject.TEXT_UNICODE))
            if (fdf.statusMessage != null && fdf.statusMessage!!.trim { it <= ' ' }.length != 0)
                dic.put(PdfName.STATUS, PdfString(fdf.statusMessage))
            val fd = PdfDictionary()
            fd.put(PdfName.FDF, dic)
            val ref = addToBody(fd).indirectReference
            os.write(DocWriter.getISOBytes("trailer\n"))
            val trailer = PdfDictionary()
            trailer.put(PdfName.ROOT, ref)
            trailer.toPdf(null, os)
            os.write(DocWriter.getISOBytes("\n%%EOF\n"))
            os.close()
        }


        @SuppressWarnings("unchecked")
        @Throws(IOException::class)
        fun calculate(map: HashMap<String, Any>): PdfArray {
            val ar = PdfArray()
            for ((key, v) in map) {
                val dic = PdfDictionary()
                dic.put(PdfName.T, PdfString(key, PdfObject.TEXT_UNICODE))
                if (v is HashMap<Any, Any>) {
                    dic.put(PdfName.KIDS, calculate(v as HashMap<String, Any>))
                } else if (v is PdfAction) {
                    // (plaflamme)
                    dic.put(PdfName.A, v)
                } else if (v is PdfAnnotation) {
                    dic.put(PdfName.AA, v)
                } else if (v is PdfDictionary && v.size() == 1 && v.contains(PdfName.N)) {
                    dic.put(PdfName.AP, v)
                } else {
                    dic.put(PdfName.V, v as PdfObject)
                }
                ar.add(dic)
            }
            return ar
        }
    }

    protected var counter = CounterFactory.getCounter(FdfWriter::class.java)

    companion object {
        private val HEADER_FDF = DocWriter.getISOBytes("%FDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n")
    }
}
