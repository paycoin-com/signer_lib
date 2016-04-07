/*
 * $Id: 0191e19dac5ae08eda12af87a27498a7f666a33e $
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

import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.DocumentException
import com.itextpdf.text.Image

/** Represents an imported page.

 * @author Paulo Soares
 */
class PdfImportedPage internal constructor(internal var pdfReaderInstance:

                                           PdfReaderInstance, writer: PdfWriter, pageNumber: Int) : com.itextpdf.text.pdf.PdfTemplate() {
    var pageNumber: Int = 0
        internal set
    var rotation: Int = 0
        internal set

    /**
     * True if the imported page has been copied to a writer.
     * @since iText 5.0.4
     */
    /**
     * Checks if the page has to be copied.
     * @return true if the page has to be copied.
     * *
     * @since iText 5.0.4
     */
    var isToCopy = true
        protected set

    init {
        this.pageNumber = pageNumber
        this.pdfWriter = writer
        rotation = pdfReaderInstance.reader.getPageRotation(pageNumber)
        boundingBox = pdfReaderInstance.reader.getPageSize(pageNumber)
        setMatrix(1f, 0f, 0f, 1f, -boundingBox.left, -boundingBox.bottom)
        type = PdfTemplate.TYPE_IMPORTED
    }

    /** Reads the content from this PdfImportedPage-object from a reader.

     * @return self
     */
    val fromReader: PdfImportedPage
        get() = this


    /** Always throws an error. This operation is not allowed.
     * @param image dummy
     * *
     * @param a dummy
     * *
     * @param b dummy
     * *
     * @param c dummy
     * *
     * @param d dummy
     * *
     * @param e dummy
     * *
     * @param f dummy
     * *
     * @throws DocumentException  dummy
     */
    @Throws(DocumentException::class)
    override fun addImage(image: Image, a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
        throwError()
    }

    /** Always throws an error. This operation is not allowed.
     * @param template dummy
     * *
     * @param a dummy
     * *
     * @param b dummy
     * *
     * @param c dummy
     * *
     * @param d dummy
     * *
     * @param e dummy
     * *
     * @param f  dummy
     */
    override fun addTemplate(template: PdfTemplate, a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
        throwError()
    }

    /** Always throws an error. This operation is not allowed.
     * @return  dummy
     */
    override val duplicate: PdfContentByte?
        get() {
            throwError()
            return null
        }

    /**
     * Gets the stream representing this page.

     * @param    compressionLevel    the compressionLevel
     * *
     * @return the stream representing this page
     * *
     * @since    2.1.3	(replacing the method without param compressionLevel)
     */
    @Throws(IOException::class)
    override fun getFormXObject(compressionLevel: Int): PdfStream {
        return pdfReaderInstance.getFormXObject(pageNumber, compressionLevel)
    }

    override fun setColorFill(sp: PdfSpotColor, tint: Float) {
        throwError()
    }

    override fun setColorStroke(sp: PdfSpotColor, tint: Float) {
        throwError()
    }

    internal override val resources: PdfObject
        get() = pdfReaderInstance.getResources(pageNumber)

    /** Always throws an error. This operation is not allowed.
     * @param bf dummy
     * *
     * @param size dummy
     */
    override fun setFontAndSize(bf: BaseFont, size: Float) {
        throwError()
    }

    /**
     * Always throws an error. This operation is not allowed.
     * @param group New value of property group.
     * *
     * @since    2.1.6
     */
    override var group: PdfTransparencyGroup
        get() = super.group
        set(group) = throwError()

    internal fun throwError() {
        throw RuntimeException(MessageLocalization.getComposedMessage("content.can.not.be.added.to.a.pdfimportedpage"))
    }

    /**
     * Indicate that the resources of the imported page have been copied.
     * @since iText 5.0.4
     */
    fun setCopied() {
        isToCopy = false
    }
}
