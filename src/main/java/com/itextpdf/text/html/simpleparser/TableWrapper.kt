/*
 * $Id: 491ca3201cf888ec659efd96f57921898b87506e $
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

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

import com.itextpdf.text.Chunk
import com.itextpdf.text.Element
import com.itextpdf.text.ElementListener
import com.itextpdf.text.html.HtmlTags
import com.itextpdf.text.html.HtmlUtilities
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable

/**
 * We use a TableWrapper because PdfPTable is rather complex
 * to put on the HTMLWorker stack.
 * @author  psoares
 * *
 * @since 5.0.6 (renamed)
 * *
 */
@Deprecated("")
@Deprecated("since 5.5.2")
class TableWrapper
/**
 * Creates a new instance of IncTable.
 * @param    attrs    a Map containing attributes
 */
(attrs: Map<String, String>) : Element {
    /**
     * The styles that need to be applied to the table
     * @since 5.0.6 renamed from props
     */
    private val styles = HashMap<String, String>()
    /**
     * Nested list containing the PdfPCell elements that are part of this table.
     */
    private val rows = ArrayList<List<PdfPCell>>()

    /**
     * Array containing the widths of the columns.
     * @since iText 5.0.6
     */
    private var colWidths: FloatArray? = null

    init {
        this.styles.putAll(attrs)
    }

    /**
     * Adds a new row to the table.
     * @param row a list of PdfPCell elements
     */
    fun addRow(row: List<PdfPCell>?) {
        var row = row
        if (row != null) {
            Collections.reverse(row)
            rows.add(row)
            row = null
        }
    }

    /**
     * Setter for the column widths
     * @since iText 5.0.6
     */
    fun setColWidths(colWidths: FloatArray) {
        this.colWidths = colWidths
    }

    /**
     * Creates a new PdfPTable based on the info assembled
     * in the table stub.
     * @return    a PdfPTable
     */
    fun createTable(): PdfPTable {
        // no rows = simplest table possible
        if (rows.isEmpty())
            return PdfPTable(1)
        // how many columns?
        var ncol = 0
        for (pc in rows[0]) {
            ncol += pc.colspan
        }
        val table = PdfPTable(ncol)
        // table width
        val width = styles[HtmlTags.WIDTH]
        if (width == null)
            table.widthPercentage = 100f
        else {
            if (width.endsWith("%"))
                table.widthPercentage = java.lang.Float.parseFloat(width.substring(0, width.length - 1))
            else {
                table.totalWidth = java.lang.Float.parseFloat(width)
                table.isLockedWidth = true
            }
        }
        // horizontal alignment
        val alignment = styles[HtmlTags.ALIGN]
        var align = Element.ALIGN_LEFT
        if (alignment != null) {
            align = HtmlUtilities.alignmentValue(alignment)
        }
        table.horizontalAlignment = align
        // column widths
        try {
            if (colWidths != null)
                table.setWidths(colWidths)
        } catch (e: Exception) {
            // fail silently
        }

        // add the cells
        for (col in rows) {
            for (pc in col) {
                table.addCell(pc)
            }
        }
        return table
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
