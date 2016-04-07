/*
 * $Id: 9c8f68076dc4fc60fdb096f850dd3bc988d38268 $
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
package com.itextpdf.text.pdf.events

import java.io.IOException
import java.util.HashMap

import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Rectangle
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfFormField
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPCellEvent
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfRectangle
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.TextField

/**
 * Class that can be used to position AcroForm fields.
 */
class FieldPositioningEvents : PdfPageEventHelper, PdfPCellEvent {

    /**
     * Keeps a map with fields that are to be positioned in inGenericTag.
     */
    protected var genericChunkFields = HashMap<String, PdfFormField>()

    /**
     * Keeps the form field that is to be positioned in a cellLayout event.
     */
    protected var cellField: PdfFormField? = null

    /**
     * The PdfWriter to use when a field has to added in a cell event.
     */
    protected var fieldWriter: PdfWriter? = null
    /**
     * The PdfFormField that is the parent of the field added in a cell event.
     */
    protected var parent: PdfFormField? = null

    /** Creates a new event. This constructor will be used if you need to position fields with Chunk objects.  */
    constructor() {
    }

    /** Some extra padding that will be taken into account when defining the widget.  */
    var padding: Float = 0.toFloat()

    /**
     * Add a PdfFormField that has to be tied to a generic Chunk.
     */
    fun addField(text: String, field: PdfFormField) {
        genericChunkFields.put(text, field)
    }

    /** Creates a new event. This constructor will be used if you need to position fields with a Cell Event.  */
    constructor(writer: PdfWriter, field: PdfFormField) {
        this.cellField = field
        this.fieldWriter = writer
    }

    /** Creates a new event. This constructor will be used if you need to position fields with a Cell Event.  */
    constructor(parent: PdfFormField, field: PdfFormField) {
        this.cellField = field
        this.parent = parent
    }

    /** Creates a new event. This constructor will be used if you need to position fields with a Cell Event.
     * @throws DocumentException
     * *
     * @throws IOException
     */
    @Throws(IOException::class, DocumentException::class)
    constructor(writer: PdfWriter, text: String) {
        this.fieldWriter = writer
        val tf = TextField(writer, Rectangle(0f, 0f), text)
        tf.fontSize = 14f
        cellField = tf.textField
    }

    /** Creates a new event. This constructor will be used if you need to position fields with a Cell Event.
     * @throws DocumentException
     * *
     * @throws IOException
     */
    @Throws(IOException::class, DocumentException::class)
    constructor(writer: PdfWriter, parent: PdfFormField, text: String) {
        this.parent = parent
        val tf = TextField(writer, Rectangle(0f, 0f), text)
        tf.fontSize = 14f
        cellField = tf.textField
    }

    /**
     * @param padding The padding to set.
     */
    fun setPadding(padding: Float) {
        this.padding = padding
    }

    /**
     * @param parent The parent to set.
     */
    fun setParent(parent: PdfFormField) {
        this.parent = parent
    }

    /**
     * @see com.itextpdf.text.pdf.PdfPageEvent.onGenericTag
     */
    override fun onGenericTag(writer: PdfWriter, document: Document,
                              rect: Rectangle, text: String) {
        rect.bottom = rect.bottom - 3
        var field: PdfFormField? = genericChunkFields[text]
        if (field == null) {
            val tf = TextField(writer, Rectangle(rect.getLeft(padding), rect.getBottom(padding), rect.getRight(padding), rect.getTop(padding)), text)
            tf.fontSize = 14f
            try {
                field = tf.textField
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        } else {
            field.put(PdfName.RECT, PdfRectangle(rect.getLeft(padding), rect.getBottom(padding), rect.getRight(padding), rect.getTop(padding)))
        }
        if (parent == null)
            writer.addAnnotation(field)
        else
            parent!!.addKid(field)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfPCellEvent.cellLayout
     */
    override fun cellLayout(cell: PdfPCell, rect: Rectangle, canvases: Array<PdfContentByte>) {
        if (cellField == null || fieldWriter == null && parent == null) throw IllegalArgumentException(MessageLocalization.getComposedMessage("you.have.used.the.wrong.constructor.for.this.fieldpositioningevents.class"))
        cellField!!.put(PdfName.RECT, PdfRectangle(rect.getLeft(padding), rect.getBottom(padding), rect.getRight(padding), rect.getTop(padding)))
        if (parent == null)
            fieldWriter!!.addAnnotation(cellField)
        else
            parent!!.addKid(cellField)
    }
}
