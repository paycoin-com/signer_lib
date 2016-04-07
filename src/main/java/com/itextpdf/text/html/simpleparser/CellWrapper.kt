/*
 * $Id: fbd7bef2396513cd5ebb5094ad38e73200845913 $
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
package com.itextpdf.text.html.simpleparser

import com.itextpdf.text.Chunk
import com.itextpdf.text.Element
import com.itextpdf.text.ElementListener
import com.itextpdf.text.Phrase
import com.itextpdf.text.TextElementArray
import com.itextpdf.text.html.HtmlTags
import com.itextpdf.text.html.HtmlUtilities
import com.itextpdf.text.pdf.PdfPCell

/**
 * We use a CellWrapper because we need some extra info
 * that isn't available in PdfPCell.
 * @author  psoares
 * *
 * @since 5.0.6 (renamed)
 * *
 */
@Deprecated("")
@Deprecated("since 5.5.2")
class CellWrapper
/**
 * Creates a new instance of IncCell.
 * @param    tag        the cell that is wrapped in this object.
 * *
 * @param    chain    properties such as width
 * *
 * @since    5.0.6
 */
(tag: String, chain: ChainedProperties) : TextElementArray {

    /** The cell that is wrapped in this stub.  */
    /**
     * Returns the PdfPCell.
     * @return the PdfPCell
     */
    val cell: PdfPCell

    /**
     * The width of the cell.
     * @since iText 5.0.6
     */
    /**
     * Getter for the cell width
     * @return the width
     * *
     * @since iText 5.0.6
     */
    var width: Float = 0.toFloat()
        private set

    /**
     * Indicates if the width is a percentage.
     * @since iText 5.0.6
     */
    /**
     * Getter for percentage
     * @return true if the width is a percentage
     * *
     * @since iText 5.0.6
     */
    var isPercentage: Boolean = false
        private set

    init {
        this.cell = createPdfPCell(tag, chain)
        var value = chain.getProperty(HtmlTags.WIDTH)
        if (value != null) {
            value = value.trim { it <= ' ' }
            if (value.endsWith("%")) {
                isPercentage = true
                value = value.substring(0, value.length - 1)
            }
            width = java.lang.Float.parseFloat(value)
        }
    }

    /**
     * Creates a PdfPCell element based on a tag and its properties.
     * @param    tag        a cell tag
     * *
     * @param    chain    the hierarchy chain
     * *
     * @return the created PdfPCell
     */
    fun createPdfPCell(tag: String, chain: ChainedProperties): PdfPCell {
        val cell = PdfPCell(null as Phrase)
        // colspan
        var value = chain.getProperty(HtmlTags.COLSPAN)
        if (value != null)
            cell.colspan = Integer.parseInt(value)
        // rowspan
        value = chain.getProperty(HtmlTags.ROWSPAN)
        if (value != null)
            cell.rowspan = Integer.parseInt(value)
        // horizontal alignment
        if (tag == HtmlTags.TH)
            cell.horizontalAlignment = Element.ALIGN_CENTER
        value = chain.getProperty(HtmlTags.ALIGN)
        if (value != null) {
            cell.horizontalAlignment = HtmlUtilities.alignmentValue(value)
        }
        // vertical alignment
        value = chain.getProperty(HtmlTags.VALIGN)
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        if (value != null) {
            cell.verticalAlignment = HtmlUtilities.alignmentValue(value)
        }
        // border
        value = chain.getProperty(HtmlTags.BORDER)
        var border = 0f
        if (value != null)
            border = java.lang.Float.parseFloat(value)
        cell.borderWidth = border
        // cellpadding
        value = chain.getProperty(HtmlTags.CELLPADDING)
        if (value != null)
            cell.setPadding(java.lang.Float.parseFloat(value))
        cell.isUseDescender = true
        // background color
        value = chain.getProperty(HtmlTags.BGCOLOR)
        cell.backgroundColor = HtmlUtilities.decodeColor(value)
        return cell
    }

    /**
     * Implements the add method of the TextElementArray interface.
     * @param    o    an element that needs to be added to the cell.
     */
    override fun add(o: Element): Boolean {
        cell.addElement(o)
        return true
    }

    // these Element methods are irrelevant for a table stub.

    /**
     * @since 5.0.1
     */
    override fun getChunks(): List<Chunk>? {
        return null
    }

    /**
     * @since 5.0.1
     */
    override fun isContent(): Boolean {
        return false
    }

    /**
     * @since 5.0.1
     */
    override fun isNestable(): Boolean {
        return false
    }

    /**
     * @since 5.0.1
     */
    override fun process(listener: ElementListener): Boolean {
        return false
    }

    /**
     * @since 5.0.1
     */
    override fun type(): Int {
        return 0
    }
}
