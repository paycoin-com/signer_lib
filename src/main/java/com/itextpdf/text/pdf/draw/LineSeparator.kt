/*
 * $Id: 6f592b20464fd240488d6de9876bcceccc4cde09 $
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
package com.itextpdf.text.pdf.draw

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.pdf.PdfChunk
import com.itextpdf.text.pdf.PdfContentByte

/**
 * Element that draws a solid line from left to right.
 * Can be added directly to a document or column.
 * Can also be used to create a separator chunk.
 * @author    Paulo Soares
 * *
 * @since    2.1.2
 */
open class LineSeparator : VerticalPositionMark {

    /** The thickness of the line.  */
    /**
     * Getter for the line width.
     * @return    the thickness of the line that will be drawn.
     */
    /**
     * Setter for the line width.
     * @param lineWidth    the thickness of the line that will be drawn.
     */
    var lineWidth = 1f
    /** The width of the line as a percentage of the available page width.  */
    /**
     * Setter for the width as a percentage of the available width.
     * @return    a width percentage
     */
    /**
     * Setter for the width as a percentage of the available width.
     * @param percentage    a width percentage
     */
    var percentage = 100f
    /** The color of the line.  */
    /**
     * Getter for the color of the line that will be drawn.
     * @return    a color
     */
    /**
     * Setter for the color of the line that will be drawn.
     * @param color    a color
     */
    var lineColor: BaseColor? = null
    /** The alignment of the line.  */
    /**
     * Getter for the alignment of the line.
     * @return    an alignment value
     */
    /**
     * Setter for the alignment of the line.
     * @param align    an alignment value
     */
    var alignment = Element.ALIGN_BOTTOM

    /**
     * Creates a new instance of the LineSeparator class.
     * @param lineWidth        the thickness of the line
     * *
     * @param percentage    the width of the line as a percentage of the available page width
     * *
     * @param lineColor            the color of the line
     * *
     * @param align            the alignment
     * *
     * @param offset        the offset of the line relative to the current baseline (negative = under the baseline)
     */
    constructor(lineWidth: Float, percentage: Float, lineColor: BaseColor, align: Int, offset: Float) {
        this.lineWidth = lineWidth
        this.percentage = percentage
        this.lineColor = lineColor
        this.alignment = align
        this.offset = offset
    }

    /**
     * Creates a new instance of the LineSeparator class.
     * @param font            the font
     */
    constructor(font: Font) {
        this.lineWidth = PdfChunk.UNDERLINE_THICKNESS * font.size
        this.offset = PdfChunk.UNDERLINE_OFFSET * font.size
        this.percentage = 100f
        this.lineColor = font.color
    }

    /**
     * Creates a new instance of the LineSeparator class with
     * default values: lineWidth 1 user unit, width 100%, centered with offset 0.
     */
    constructor() {
    }

    /**
     * @see com.itextpdf.text.pdf.draw.DrawInterface.draw
     */
    override fun draw(canvas: PdfContentByte, llx: Float, lly: Float, urx: Float, ury: Float, y: Float) {
        canvas.saveState()
        drawLine(canvas, llx, urx, y)
        canvas.restoreState()
    }

    /**
     * Draws a horizontal line.
     * @param canvas    the canvas to draw on
     * *
     * @param leftX        the left x coordinate
     * *
     * @param rightX    the right x coordindate
     * *
     * @param y            the y coordinate
     */
    fun drawLine(canvas: PdfContentByte, leftX: Float, rightX: Float, y: Float) {
        val w: Float
        if (percentage < 0)
            w = -percentage
        else
            w = (rightX - leftX) * percentage / 100.0f
        val s: Float
        when (alignment) {
            Element.ALIGN_LEFT -> s = 0f
            Element.ALIGN_RIGHT -> s = rightX - leftX - w
            else -> s = (rightX - leftX - w) / 2
        }
        canvas.setLineWidth(lineWidth)
        if (lineColor != null)
            canvas.setColorStroke(lineColor)
        canvas.moveTo(s + leftX, y + offset)
        canvas.lineTo(s + w + leftX, y + offset)
        canvas.stroke()
    }
}
