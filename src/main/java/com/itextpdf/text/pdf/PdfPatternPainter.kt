/*
 * $Id: abf349c05a7bfcf46d5e629f59a0936973f42200 $
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

import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.DocumentException
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.BaseColor

/**
 * Implements the pattern.
 */

class PdfPatternPainter : PdfTemplate {

    /**
     * Returns the horizontal interval when repeating the pattern.
     * @return a value
     */
    /**
     * Sets the horizontal interval of this pattern.

     * @param xstep the xstep in horizontal painting
     */

    var xStep: Float = 0.toFloat()
    /**
     * Returns the vertical interval when repeating the pattern.
     * @return a value
     */
    /**
     * Sets the vertical interval of this pattern.

     * @param ystep in vertical painting
     */

    var yStep: Float = 0.toFloat()
    /**
     * Tells you if this pattern is colored/uncolored (stencil = uncolored, you need to set a default color).
     * @return true if the pattern is an uncolored tiling pattern (stencil).
     */
    var isStencil = false
        internal set
    /**
     * Returns the default color of the pattern.
     * @return a BaseColor
     */
    var defaultColor: BaseColor
        internal set

    /**
     * Creates a PdfPattern.
     */

    private constructor() : super() {
        type = PdfTemplate.TYPE_PATTERN
    }

    /**
     * Creates new PdfPattern

     * @param wr the PdfWriter
     */

    internal constructor(wr: PdfWriter) : super(wr) {
        type = PdfTemplate.TYPE_PATTERN
    }

    internal constructor(wr: PdfWriter, defaultColor: BaseColor?) : this(wr) {
        isStencil = true
        if (defaultColor == null)
            this.defaultColor = BaseColor.GRAY
        else
            this.defaultColor = defaultColor
    }

    /**
     * Sets the transformation matrix for the pattern.
     * @param a
     * *
     * @param b
     * *
     * @param c
     * *
     * @param d
     * *
     * @param e
     * *
     * @param f
     */
    fun setPatternMatrix(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
        setMatrix(a, b, c, d, e, f)
    }

    /**
     * Gets the stream representing this pattern
     * @return the stream representing this pattern
     */
    val pattern: PdfPattern
        get() = PdfPattern(this)

    /**
     * Gets the stream representing this pattern
     * @param    compressionLevel    the compression level of the stream
     * *
     * @return the stream representing this pattern
     * *
     * @since    2.1.3
     */
    fun getPattern(compressionLevel: Int): PdfPattern {
        return PdfPattern(this, compressionLevel)
    }

    /**
     * Gets a duplicate of this PdfPatternPainter. All
     * the members are copied by reference but the buffer stays different.
     * @return a copy of this PdfPatternPainter
     */

    override val duplicate: PdfContentByte
        get() {
            val tpl = PdfPatternPainter()
            tpl.pdfWriter = pdfWriter
            tpl.pdfDocument = pdfDocument
            tpl.thisReference = thisReference
            tpl.pageResources = pageResources
            tpl.boundingBox = Rectangle(boundingBox)
            tpl.xStep = xStep
            tpl.yStep = yStep
            tpl.matrix = matrix
            tpl.isStencil = isStencil
            tpl.defaultColor = defaultColor
            return tpl
        }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setGrayFill
     */
    override fun setGrayFill(gray: Float) {
        checkNoColor()
        super.setGrayFill(gray)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.resetGrayFill
     */
    override fun resetGrayFill() {
        checkNoColor()
        super.resetGrayFill()
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setGrayStroke
     */
    override fun setGrayStroke(gray: Float) {
        checkNoColor()
        super.setGrayStroke(gray)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.resetGrayStroke
     */
    override fun resetGrayStroke() {
        checkNoColor()
        super.resetGrayStroke()
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setRGBColorFillF
     */
    override fun setRGBColorFillF(red: Float, green: Float, blue: Float) {
        checkNoColor()
        super.setRGBColorFillF(red, green, blue)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.resetRGBColorFill
     */
    override fun resetRGBColorFill() {
        checkNoColor()
        super.resetRGBColorFill()
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setRGBColorStrokeF
     */
    override fun setRGBColorStrokeF(red: Float, green: Float, blue: Float) {
        checkNoColor()
        super.setRGBColorStrokeF(red, green, blue)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.resetRGBColorStroke
     */
    override fun resetRGBColorStroke() {
        checkNoColor()
        super.resetRGBColorStroke()
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setCMYKColorFillF
     */
    override fun setCMYKColorFillF(cyan: Float, magenta: Float, yellow: Float, black: Float) {
        checkNoColor()
        super.setCMYKColorFillF(cyan, magenta, yellow, black)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.resetCMYKColorFill
     */
    override fun resetCMYKColorFill() {
        checkNoColor()
        super.resetCMYKColorFill()
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setCMYKColorStrokeF
     */
    override fun setCMYKColorStrokeF(cyan: Float, magenta: Float, yellow: Float, black: Float) {
        checkNoColor()
        super.setCMYKColorStrokeF(cyan, magenta, yellow, black)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.resetCMYKColorStroke
     */
    override fun resetCMYKColorStroke() {
        checkNoColor()
        super.resetCMYKColorStroke()
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.addImage
     */
    @Throws(DocumentException::class)
    override fun addImage(image: Image, a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
        if (isStencil && !image.isMask)
            checkNoColor()
        super.addImage(image, a, b, c, d, e, f)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setCMYKColorFill
     */
    override fun setCMYKColorFill(cyan: Int, magenta: Int, yellow: Int, black: Int) {
        checkNoColor()
        super.setCMYKColorFill(cyan, magenta, yellow, black)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setCMYKColorStroke
     */
    override fun setCMYKColorStroke(cyan: Int, magenta: Int, yellow: Int, black: Int) {
        checkNoColor()
        super.setCMYKColorStroke(cyan, magenta, yellow, black)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setRGBColorFill
     */
    override fun setRGBColorFill(red: Int, green: Int, blue: Int) {
        checkNoColor()
        super.setRGBColorFill(red, green, blue)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setRGBColorStroke
     */
    override fun setRGBColorStroke(red: Int, green: Int, blue: Int) {
        checkNoColor()
        super.setRGBColorStroke(red, green, blue)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setColorStroke
     */
    override fun setColorStroke(color: BaseColor) {
        checkNoColor()
        super.setColorStroke(color)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setColorFill
     */
    override fun setColorFill(color: BaseColor) {
        checkNoColor()
        super.setColorFill(color)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setColorFill
     */
    override fun setColorFill(sp: PdfSpotColor, tint: Float) {
        checkNoColor()
        super.setColorFill(sp, tint)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setColorStroke
     */
    override fun setColorStroke(sp: PdfSpotColor, tint: Float) {
        checkNoColor()
        super.setColorStroke(sp, tint)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setPatternFill
     */
    override fun setPatternFill(p: PdfPatternPainter) {
        checkNoColor()
        super.setPatternFill(p)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setPatternFill
     */
    override fun setPatternFill(p: PdfPatternPainter, color: BaseColor, tint: Float) {
        checkNoColor()
        super.setPatternFill(p, color, tint)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setPatternStroke
     */
    override fun setPatternStroke(p: PdfPatternPainter, color: BaseColor, tint: Float) {
        checkNoColor()
        super.setPatternStroke(p, color, tint)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfContentByte.setPatternStroke
     */
    override fun setPatternStroke(p: PdfPatternPainter) {
        checkNoColor()
        super.setPatternStroke(p)
    }

    internal fun checkNoColor() {
        if (isStencil)
            throw RuntimeException(MessageLocalization.getComposedMessage("colors.are.not.allowed.in.uncolored.tile.patterns"))
    }
}
