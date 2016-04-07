/*
 * $Id: e994eca2f24563c28131e38db15691c22329b4eb $
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

import com.itextpdf.awt.geom.AffineTransform
import com.itextpdf.text.Rectangle

/**
 * PdfRectangle is the PDF Rectangle object.
 *
 * Rectangles are used to describe locations on the page and bounding boxes for several
 * objects in PDF, such as fonts. A rectangle is represented as an array of
 * four numbers, specifying the lower left x, lower left y, upper right x,
 * and upper right y coordinates of the rectangle, in that order.
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 7.1 (page 183).

 * @see com.itextpdf.text.Rectangle

 * @see PdfArray
 */

class PdfRectangle// constructors

/**
 * Constructs a PdfRectangle-object.

 * @param        llx            lower left x
 * *
 * @param        lly            lower left y
 * *
 * @param        urx            upper right x
 * *
 * @param        ury            upper right y
 * *
 * *
 * @since        rugPdf0.10
 */
@JvmOverloads constructor(llx: Float, lly: Float, urx: Float, ury: Float, rotation: Int = 0) : NumberArray() {

    // membervariables

    /** lower left x  */
    private var llx = 0f

    /** lower left y  */
    private var lly = 0f

    /** upper right x  */
    private var urx = 0f

    /** upper right y  */
    private var ury = 0f

    init {
        if (rotation == 90 || rotation == 270) {
            this.llx = lly
            this.lly = llx
            this.urx = ury
            this.ury = urx
        } else {
            this.llx = llx
            this.lly = lly
            this.urx = urx
            this.ury = ury
        }
        super.add(PdfNumber(this.llx))
        super.add(PdfNumber(this.lly))
        super.add(PdfNumber(this.urx))
        super.add(PdfNumber(this.ury))
    }

    /**
     * Constructs a PdfRectangle-object starting from the origin (0, 0).

     * @param        urx            upper right x
     * *
     * @param        ury            upper right y
     */

    constructor(urx: Float, ury: Float, rotation: Int) : this(0f, 0f, urx, ury, rotation) {
    }

    constructor(urx: Float, ury: Float) : this(0f, 0f, urx, ury, 0) {
    }

    /**
     * Constructs a PdfRectangle-object with a Rectangle-object.

     * @param    rectangle    a Rectangle
     */

    constructor(rectangle: Rectangle, rotation: Int) : this(rectangle.left, rectangle.bottom, rectangle.right, rectangle.top, rotation) {
    }

    constructor(rectangle: Rectangle) : this(rectangle.left, rectangle.bottom, rectangle.right, rectangle.top, 0) {
    }

    // methods
    /**
     * Returns the high level version of this PdfRectangle
     * @return this PdfRectangle translated to class Rectangle
     */
    val rectangle: Rectangle
        get() = Rectangle(left(), bottom(), right(), top())

    /**
     * Overrides the add-method in PdfArray in order to prevent the adding of extra object to the array.

     * @param        object            PdfObject to add (will not be added here)
     * *
     * @return        false
     */

    override fun add(`object`: PdfObject): Boolean {
        return false
    }

    /**
     * Block changes to the underlying PdfArray
     * @param values stuff we'll ignore.  Ha!
     * *
     * @return false.  You can't add anything to a PdfRectangle
     * *
     * @since 2.1.5
     */

    override fun add(values: FloatArray): Boolean {
        return false
    }

    /**
     * Block changes to the underlying PdfArray
     * @param values stuff we'll ignore.  Ha!
     * *
     * @return false.  You can't add anything to a PdfRectangle
     * *
     * @since 2.1.5
     */

    override fun add(values: IntArray): Boolean {
        return false
    }

    /**
     * Block changes to the underlying PdfArray
     * @param object Ignored.
     * *
     * @since 2.1.5
     */

    override fun addFirst(`object`: PdfObject) {
    }

    /**
     * Returns the lower left x-coordinate.

     * @return        the lower left x-coordinate
     */

    fun left(): Float {
        return llx
    }

    /**
     * Returns the upper right x-coordinate.

     * @return        the upper right x-coordinate
     */

    fun right(): Float {
        return urx
    }

    /**
     * Returns the upper right y-coordinate.

     * @return        the upper right y-coordinate
     */

    fun top(): Float {
        return ury
    }

    /**
     * Returns the lower left y-coordinate.

     * @return        the lower left y-coordinate
     */

    fun bottom(): Float {
        return lly
    }

    /**
     * Returns the lower left x-coordinate, considering a given margin.

     * @param        margin        a margin
     * *
     * @return        the lower left x-coordinate
     */

    fun left(margin: Int): Float {
        return llx + margin
    }

    /**
     * Returns the upper right x-coordinate, considering a given margin.

     * @param        margin        a margin
     * *
     * @return        the upper right x-coordinate
     */

    fun right(margin: Int): Float {
        return urx - margin
    }

    /**
     * Returns the upper right y-coordinate, considering a given margin.

     * @param        margin        a margin
     * *
     * @return        the upper right y-coordinate
     */

    fun top(margin: Int): Float {
        return ury - margin
    }

    /**
     * Returns the lower left y-coordinate, considering a given margin.

     * @param        margin        a margin
     * *
     * @return        the lower left y-coordinate
     */

    fun bottom(margin: Int): Float {
        return lly + margin
    }

    /**
     * Returns the width of the rectangle.

     * @return        a width
     */

    fun width(): Float {
        return urx - llx
    }

    /**
     * Returns the height of the rectangle.

     * @return        a height
     */

    fun height(): Float {
        return ury - lly
    }

    /**
     * Swaps the values of urx and ury and of lly and llx in order to rotate the rectangle.

     * @return        a PdfRectangle
     */

    fun rotate(): PdfRectangle {
        return PdfRectangle(lly, llx, ury, urx, 0)
    }

    fun transform(transform: AffineTransform): PdfRectangle {
        val pts = floatArrayOf(llx, lly, urx, ury)
        transform.transform(pts, 0, pts, 0, 2)
        val dstPts = floatArrayOf(pts[0], pts[1], pts[2], pts[3])
        if (pts[0] > pts[2]) {
            dstPts[0] = pts[2]
            dstPts[2] = pts[0]
        }
        if (pts[1] > pts[3]) {
            dstPts[1] = pts[3]
            dstPts[3] = pts[1]
        }
        return PdfRectangle(dstPts[0], dstPts[1], dstPts[2], dstPts[3])
    }
}
