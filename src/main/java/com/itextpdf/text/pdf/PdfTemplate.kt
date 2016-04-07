/*
 * $Id: 0138cfa639b5975708326009ddf6fc28bb6887bc $
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
import java.util.HashMap

import com.itextpdf.text.AccessibleElementId
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

/**
 * Implements the form XObject.
 */

open class PdfTemplate : PdfContentByte, IAccessibleElement {
    var type: Int = 0
        protected set
    /** The indirect reference to this template  */
    protected var thisReference: PdfIndirectReference? = null

    /** The resources used by this template  */
    internal override var pageResources:

            PageResources
        protected set(value: PageResources) {
            super.pageResources = value
        }


    /** The bounding box of this template  */
    var boundingBox = Rectangle(0f, 0f)

    internal var matrix:

            PdfArray? = null
        protected set

    /** Getter for property group.
     * @return Value of property group.
     */
    /** Setter for property group.
     * @param group New value of property group.
     */
    open var group: PdfTransparencyGroup

    /**
     * Gets the layer this template belongs to.
     * @return the layer this template belongs to or `null` for no layer defined
     */
    /**
     * Sets the layer this template belongs to.
     * @param layer the layer this template belongs to
     */
    var layer: PdfOCG

    var pageReference: PdfIndirectReference? = null

    var isContentTagged = false

    /**
     * A dictionary with additional information
     * @since 5.1.0
     */
    /**
     * Getter for the dictionary with additional information.

     * @return a PdfDictionary with additional information.
     * *
     * @since 5.1.0
     */
    /**
     * Sets a dictionary with extra entries, for instance /Measure.

     * @param additional
     * *            a PdfDictionary with additional information.
     * *
     * @since 5.1.0
     */
    var additional: PdfDictionary? = null

    override var role = PdfName.FIGURE
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id: AccessibleElementId? = null
        get() {
            if (id == null)
                id = AccessibleElementId()
            return id
        }

    /**
     * Creates a PdfTemplate.
     */

    protected constructor() : super(null) {
        type = TYPE_TEMPLATE
    }

    /**
     * Creates new PdfTemplate

     * @param wr the PdfWriter
     */

    internal constructor(wr: PdfWriter) : super(wr) {
        type = TYPE_TEMPLATE
        pageResources = PageResources()
        pageResources.addDefaultColor(wr.defaultColorspace)
        thisReference = pdfWriter!!.pdfIndirectReference
    }

    override val isTagged: Boolean
        get() = super.isTagged && isContentTagged

    /**
     * Gets the bounding width of this template.

     * @return width the bounding width
     */
    /**
     * Sets the bounding width of this template.

     * @param width the bounding width
     */

    var width: Float
        get() = boundingBox.width
        set(width) {
            boundingBox.left = 0f
            boundingBox.right = width
        }

    /**
     * Gets the bounding height of this template.

     * @return height the bounding height
     */

    /**
     * Sets the bounding height of this template.

     * @param height the bounding height
     */

    var height: Float
        get() = boundingBox.height
        set(height) {
            boundingBox.bottom = 0f
            boundingBox.top = height
        }

    fun setMatrix(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
        matrix = PdfArray()
        matrix!!.add(PdfNumber(a))
        matrix!!.add(PdfNumber(b))
        matrix!!.add(PdfNumber(c))
        matrix!!.add(PdfNumber(d))
        matrix!!.add(PdfNumber(e))
        matrix!!.add(PdfNumber(f))
    }

    /**
     * Gets the indirect reference to this template.

     * @return the indirect reference to this template
     */

    // uncomment the null check as soon as we're sure all examples still work
    /* && writer != null */ val indirectReference: PdfIndirectReference
        get() {
            if (thisReference == null) {
                thisReference = pdfWriter!!.pdfIndirectReference
            }
            return thisReference
        }

    fun beginVariableText() {
        internalBuffer.append("/Tx BMC ")
    }

    fun endVariableText() {
        internalBuffer.append("EMC ")
    }

    /**
     * Constructs the resources used by this template.

     * @return the resources used by this template
     */

    internal open val resources: PdfObject
        get() = pageResources.resources

    /**
     * Gets the stream representing this template.

     * @param    compressionLevel    the compressionLevel
     * *
     * @return the stream representing this template
     * *
     * @since    2.1.3	(replacing the method without param compressionLevel)
     */
    @Throws(IOException::class)
    open fun getFormXObject(compressionLevel: Int): PdfStream {
        return PdfFormXObject(this, compressionLevel)
    }

    /**
     * Gets a duplicate of this PdfTemplate. All
     * the members are copied by reference but the buffer stays different.
     * @return a copy of this PdfTemplate
     */

    override val duplicate: PdfContentByte
        get() {
            val tpl = PdfTemplate()
            tpl.pdfWriter = pdfWriter
            tpl.pdfDocument = pdfDocument
            tpl.thisReference = thisReference
            tpl.pageResources = pageResources
            tpl.boundingBox = Rectangle(boundingBox)
            tpl.group = group
            tpl.layer = layer
            if (matrix != null) {
                tpl.matrix = PdfArray(matrix)
            }
            tpl.separator = separator
            tpl.additional = additional
            tpl.isContentTagged = isContentTagged
            tpl.duplicatedFrom = this
            return tpl
        }

    override val currentPage: PdfIndirectReference
        get() = if (pageReference == null) pdfWriter!!.currentPage else pageReference

    fun getAccessibleAttribute(key: PdfName): PdfObject? {
        if (accessibleAttributes != null)
            return accessibleAttributes!![key]
        else
            return null
    }

    fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (accessibleAttributes == null)
            accessibleAttributes = HashMap<PdfName, PdfObject>()
        accessibleAttributes!!.put(key, value)
    }

    override val isInline: Boolean
        get() = true

    companion object {
        val TYPE_TEMPLATE = 1
        val TYPE_IMPORTED = 2
        val TYPE_PATTERN = 3

        /**
         * Creates a new template.
         *
         * Creates a new template that is nothing more than a form XObject. This template can be included
         * in this template or in another template. Templates are only written
         * to the output when the document is closed permitting things like showing text in the first page
         * that is only defined in the last page.

         * @param writer the PdfWriter to use
         * *
         * @param width the bounding box width
         * *
         * @param height the bounding box height
         * *
         * @return the created template
         */
        fun createTemplate(writer: PdfWriter, width: Float, height: Float): PdfTemplate {
            return createTemplate(writer, width, height, null)
        }

        internal fun createTemplate(writer: PdfWriter, width: Float, height: Float, forcedName: PdfName?): PdfTemplate {
            val template = PdfTemplate(writer)
            template.width = width
            template.height = height
            writer.addDirectTemplateSimple(template, forcedName)
            return template
        }
    }
}
